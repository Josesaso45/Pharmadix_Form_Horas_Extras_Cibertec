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

    // POST /auth/register — registro de nuevo usuario (tomador)
    const registerSchema = z.object({
        usuario: z.string().min(3, 'El usuario debe tener al menos 3 caracteres'),
        nombre: z.string().min(2, 'El nombre es requerido'),
        password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
    });

    fastify.post('/auth/register', async (request: FastifyRequest, reply: FastifyReply) => {
        try {
            const body = registerSchema.parse(request.body);

            // Verificar si el usuario ya existe
            const existente = await prisma.usuario.findUnique({
                where: { usuario: body.usuario },
            });

            if (existente) {
                return reply.status(409).send({ error: 'El usuario ya existe' });
            }

            // Hash de la contraseña
            const hashedPassword = await bcrypt.hash(body.password, 10);

            // Crear usuario con rol TOMADOR
            const nuevoUsuario = await prisma.usuario.create({
                data: {
                    usuario: body.usuario,
                    nombre: body.nombre,
                    email: `${body.usuario}@pharmadix.com`,
                    password: hashedPassword,
                    rol: 'TOMADOR',
                    activo: true,
                },
            });

            return reply.status(201).send({
                mensaje: 'Usuario creado exitosamente',
                usuario: {
                    id: nuevoUsuario.id,
                    nombre: nuevoUsuario.nombre,
                    usuario: nuevoUsuario.usuario,
                    rol: nuevoUsuario.rol,
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
