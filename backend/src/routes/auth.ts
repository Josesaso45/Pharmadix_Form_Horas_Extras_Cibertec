import { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify';
import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';
import { z } from 'zod';

const prisma = new PrismaClient();

const loginSchema = z.object({
    usuario: z.string().min(1, 'El usuario es requerido'),
    password: z.string().min(1, 'La contraseña es requerida'),
});

export async function authRoutes(fastify: FastifyInstance) {
    // POST /auth/login
    fastify.post('/auth/login', async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const body = loginSchema.parse(request.body);

            // Buscar usuario en BD
            const usuario = await prisma.usuario.findUnique({
                where: { usuario: body.usuario },
            });

            if (!usuario || !usuario.activo) {
                return reply.status(401).send({ error: 'Usuario o contraseña incorrectos' });
            }

            // Verificar contraseña
            const passwordValida = await bcrypt.compare(body.password, usuario.password);
            if (!passwordValida) {
                return reply.status(401).send({ error: 'Usuario o contraseña incorrectos' });
            }

            // Generar JWT
            const token = fastify.jwt.sign(
                { id: usuario.id, usuario: usuario.usuario, rol: usuario.rol },
                { expiresIn: '8h' }
            );

            return reply.status(200).send({
                token,
                usuario: {
                    id: usuario.id,
                    nombre: usuario.nombre,
                    email: usuario.email,
                    rol: usuario.rol,
                },
            });
        } catch (error) {
            if (error instanceof z.ZodError) {
                return reply.status(400).send({ error: 'Datos inválidos', detalles: error.errors });
            }
            fastify.log.error(error);
            return reply.status(500).send({ error: 'Error interno del servidor' });
        }
    });

    // GET /auth/me — verificar token (útil para la app)
    fastify.get(
        '/auth/me',
        { preHandler: [fastify.authenticate] },
        async (request: FastifyRequest, reply: FastifyReply) => {
            try {
                const payload = request.user as { id: number };
                const usuario = await prisma.usuario.findUnique({
                    where: { id: payload.id },
                    select: { id: true, nombre: true, email: true, rol: true },
                });

                if (!usuario) {
                    return reply.status(404).send({ error: 'Usuario no encontrado' });
                }

                return reply.send(usuario);
            } catch (error) {
                fastify.log.error(error);
                return reply.status(500).send({ error: 'Error interno del servidor' });
            }
        }
    );
}
