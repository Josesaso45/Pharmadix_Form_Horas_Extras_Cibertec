import Fastify from 'fastify';
import fastifyJwt from '@fastify/jwt';
import { authRoutes } from './routes/auth';
import { hojasRoutes } from './routes/hojas';
import { registrosRoutes } from './routes/registros';

const PORT = parseInt(process.env.PORT ?? '3000');
const HOST = process.env.HOST ?? '0.0.0.0';
const JWT_SECRET = process.env.JWT_SECRET ?? 'pharmadix_dev_secret';

const fastify = Fastify({
    logger: {
        transport: {
            target: 'pino-pretty',
            options: {
                translateTime: 'HH:MM:ss Z',
                ignore: 'pid,hostname',
            },
        },
    },
});

// ─── Plugins ────────────────────────────────────────────────────────────────

fastify.register(fastifyJwt, { secret: JWT_SECRET });

// Decorador para verificar JWT en rutas protegidas
fastify.decorate('authenticate', async function (request: any, reply: any) {
    try {
        await request.jwtVerify();
    } catch (err) {
        reply.status(401).send({ error: 'Token inválido o expirado' });
    }
});

// ─── Rutas ──────────────────────────────────────────────────────────────────

fastify.register(authRoutes);
fastify.register(hojasRoutes);
fastify.register(registrosRoutes);

// Health check
fastify.get('/health', async () => {
    return { status: 'ok', timestamp: new Date().toISOString(), version: '1.0.0' };
});

// ─── Arranque ───────────────────────────────────────────────────────────────

const start = async () => {
    try {
        await fastify.listen({ port: PORT, host: HOST });
        fastify.log.info(`🚀 Pharmadix Backend corriendo en http://${HOST}:${PORT}`);
        fastify.log.info(`📋 Health check: http://localhost:${PORT}/health`);
    } catch (err) {
        fastify.log.error(err);
        process.exit(1);
    }
};

start();
