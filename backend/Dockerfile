FROM node:18-slim

WORKDIR /app

# Instalar OpenSSL (requerido por Prisma)
RUN apt-get update -y && apt-get install -y openssl && rm -rf /var/lib/apt/lists/*

# Instalar TODAS las dependencias (incluyendo devDeps para tsc y prisma)
COPY package*.json ./
RUN npm ci

# Copiar código fuente y Prisma schema
COPY . .

# Generar Prisma Client y compilar TypeScript
RUN npx prisma generate
RUN npx tsc

# Exponer puerto
EXPOSE 3000

# Ejecutar migraciones y arrancar servidor compilado
CMD ["sh", "-c", "npx prisma migrate deploy && node dist/server.js"]
