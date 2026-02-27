import 'fastify';

// Extender el tipo de FastifyInstance para incluir el decorador 'authenticate'
declare module 'fastify' {
    interface FastifyInstance {
        authenticate: (request: import('fastify').FastifyRequest, reply: import('fastify').FastifyReply) => Promise<void>;
    }
}
