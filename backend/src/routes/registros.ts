import { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';

const prisma = new PrismaClient();

const registroEntradaSchema = z.object({
    hojaId: z.number().int().positive(),
    empleadoId: z.number().int().positive(),
    actividad: z.string().min(1),
    horaEntrada: z.string().datetime({ message: 'horaEntrada debe ser ISO 8601' }),
});

const registroSalidaSchema = z.object({
    horaSalida: z.string().datetime({ message: 'horaSalida debe ser ISO 8601' }),
});

function calcularHorasTotales(entrada: Date, salida: Date): number {
    const ms = salida.getTime() - entrada.getTime();
    return Math.round((ms / 3_600_000) * 100) / 100; // redondear a 2 decimales
}

export async function registrosRoutes(fastify: FastifyInstance) {
    const authOptions = { preHandler: [fastify.authenticate] };

    // POST /registros — Registrar entrada
    fastify.post('/registros', authOptions, async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const body = registroEntradaSchema.parse(request.body);

            // Verificar que la hoja exista y esté abierta
            const hoja = await prisma.hojaTiempo.findUnique({ where: { id: body.hojaId } });
            if (!hoja) {
                return reply.status(404).send({ error: 'Hoja no encontrada' });
            }
            if (hoja.estado !== 'abierta') {
                return reply.status(400).send({ error: 'La hoja ya está cerrada' });
            }

            // Verificar que el empleado exista
            const empleado = await prisma.empleado.findUnique({ where: { id: body.empleadoId } });
            if (!empleado) {
                return reply.status(404).send({ error: 'Empleado no encontrado' });
            }

            const registro = await prisma.registroTiempo.create({
                data: {
                    hojaId: body.hojaId,
                    empleadoId: body.empleadoId,
                    actividad: body.actividad,
                    horaEntrada: new Date(body.horaEntrada),
                    estado: 'en_proceso',
                },
            });

            return reply.status(201).send({
                id: registro.id,
                hojaId: registro.hojaId,
                empleadoId: registro.empleadoId,
                horaEntrada: registro.horaEntrada.toISOString(),
                horaSalida: null,
                horasTotales: null,
                estado: registro.estado,
            });
        } catch (error) {
            if (error instanceof z.ZodError) {
                return reply.status(400).send({ error: 'Datos inválidos', detalles: error.errors });
            }
            fastify.log.error(error);
            return reply.status(500).send({ error: 'Error registrando entrada' });
        }
    });

    // PUT /registros/:id/salida — Registrar salida
    fastify.put(
        '/registros/:id/salida',
        authOptions,
        async (request: FastifyRequest, reply: FastifyReply) => {
            try {
                const params = request.params as { id: string };
                const id = parseInt(params.id);

                if (isNaN(id)) {
                    return reply.status(400).send({ error: 'ID inválido' });
                }

                const body = registroSalidaSchema.parse(request.body);

                // Buscar el registro
                const registro = await prisma.registroTiempo.findUnique({ where: { id } });
                if (!registro) {
                    return reply.status(404).send({ error: 'Registro no encontrado' });
                }
                if (registro.estado === 'finalizado') {
                    return reply.status(400).send({ error: 'El registro ya tiene salida registrada' });
                }

                const horaSalida = new Date(body.horaSalida);
                const horasTotales = calcularHorasTotales(registro.horaEntrada, horaSalida);

                const registroActualizado = await prisma.registroTiempo.update({
                    where: { id },
                    data: {
                        horaSalida,
                        horasTotales,
                        estado: 'finalizado',
                    },
                });

                return reply.send({
                    id: registroActualizado.id,
                    hojaId: registroActualizado.hojaId,
                    empleadoId: registroActualizado.empleadoId,
                    horaEntrada: registroActualizado.horaEntrada.toISOString(),
                    horaSalida: registroActualizado.horaSalida!.toISOString(),
                    horasTotales: registroActualizado.horasTotales,
                    estado: registroActualizado.estado,
                });
            } catch (error) {
                if (error instanceof z.ZodError) {
                    return reply.status(400).send({ error: 'Datos inválidos', detalles: error.errors });
                }
                fastify.log.error(error);
                return reply.status(500).send({ error: 'Error registrando salida' });
            }
        }
    );

    // GET /registros — listar registros de una hoja
    fastify.get(
        '/registros',
        authOptions,
        async (request: FastifyRequest, reply: FastifyReply) => {
            try {
                const query = request.query as { hojaId?: string };
                const hojaId = query.hojaId ? parseInt(query.hojaId) : undefined;

                const registros = await prisma.registroTiempo.findMany({
                    where: hojaId ? { hojaId } : {},
                    include: {
                        empleado: { select: { nombre: true, gafete: true } },
                    },
                    orderBy: { horaEntrada: 'asc' },
                });

                return reply.send(
                    registros.map((r) => ({
                        id: r.id,
                        hojaId: r.hojaId,
                        empleadoId: r.empleadoId,
                        horaEntrada: r.horaEntrada.toISOString(),
                        horaSalida: r.horaSalida?.toISOString() ?? null,
                        horasTotales: r.horasTotales,
                        estado: r.estado,
                    }))
                );
            } catch (error) {
                fastify.log.error(error);
                return reply.status(500).send({ error: 'Error obteniendo registros' });
            }
        }
    );
}
