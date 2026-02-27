import { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';

const prisma = new PrismaClient();

const crearHojaSchema = z.object({
    numeroHoja: z.string().min(1),
    loteId: z.number().int().optional(),
    turno: z.enum(['mañana', 'tarde', 'noche']),
});

export async function hojasRoutes(fastify: FastifyInstance) {
    // Todas las rutas requieren autenticación JWT
    const authOptions = { preHandler: [fastify.authenticate] };

    // GET /hojas?tomadorId=1
    fastify.get('/hojas', authOptions, async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const query = request.query as { tomadorId?: string };
            const tomadorId = query.tomadorId ? parseInt(query.tomadorId) : undefined;

            const hojas = await prisma.hojaTiempo.findMany({
                where: tomadorId ? { tomadorId } : {},
                orderBy: { createdAt: 'desc' },
                include: {
                    tomador: { select: { nombre: true } },
                    _count: { select: { registros: true } },
                },
            });

            const respuesta = hojas.map((h) => ({
                id: h.id,
                numeroHoja: h.numeroHoja,
                loteId: h.loteId,
                tomadorId: h.tomadorId,
                fechaEmision: h.fechaEmision.toISOString(),
                turno: h.turno,
                estado: h.estado,
                totalRegistros: h._count.registros,
            }));

            return reply.send(respuesta);
        } catch (error) {
            fastify.log.error(error);
            return reply.status(500).send({ error: 'Error obteniendo hojas' });
        }
    });

    // GET /hojas/:id
    fastify.get('/hojas/:id', authOptions, async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const params = request.params as { id: string };
            const id = parseInt(params.id);

            if (isNaN(id)) {
                return reply.status(400).send({ error: 'ID inválido' });
            }

            const hoja = await prisma.hojaTiempo.findUnique({
                where: { id },
                include: {
                    tomador: { select: { nombre: true } },
                    registros: {
                        include: {
                            empleado: { select: { nombre: true, gafete: true } },
                        },
                        orderBy: { horaEntrada: 'asc' },
                    },
                },
            });

            if (!hoja) {
                return reply.status(404).send({ error: 'Hoja no encontrada' });
            }

            return reply.send({
                id: hoja.id,
                numeroHoja: hoja.numeroHoja,
                loteId: hoja.loteId,
                tomadorId: hoja.tomadorId,
                fechaEmision: hoja.fechaEmision.toISOString(),
                turno: hoja.turno,
                estado: hoja.estado,
                registros: hoja.registros.map((r) => ({
                    id: r.id,
                    hojaId: r.hojaId,
                    empleadoId: r.empleadoId,
                    empleadoNombre: r.empleado.nombre,
                    actividad: r.actividad,
                    horaEntrada: r.horaEntrada.toISOString(),
                    horaSalida: r.horaSalida?.toISOString() ?? null,
                    horasTotales: r.horasTotales,
                    estado: r.estado,
                })),
            });
        } catch (error) {
            fastify.log.error(error);
            return reply.status(500).send({ error: 'Error obteniendo hoja' });
        }
    });

    // POST /hojas
    fastify.post('/hojas', authOptions, async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const payload = request.user as { id: number };
            const body = crearHojaSchema.parse(request.body);

            const hoja = await prisma.hojaTiempo.create({
                data: {
                    numeroHoja: body.numeroHoja,
                    loteId: body.loteId,
                    tomadorId: payload.id,
                    turno: body.turno,
                },
            });

            return reply.status(201).send({
                id: hoja.id,
                numeroHoja: hoja.numeroHoja,
                loteId: hoja.loteId,
                tomadorId: hoja.tomadorId,
                fechaEmision: hoja.fechaEmision.toISOString(),
                turno: hoja.turno,
                estado: hoja.estado,
            });
        } catch (error) {
            if (error instanceof z.ZodError) {
                return reply.status(400).send({ error: 'Datos inválidos', detalles: error.errors });
            }
            fastify.log.error(error);
            return reply.status(500).send({ error: 'Error creando hoja' });
        }
    });
}
