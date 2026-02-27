-- CreateTable
CREATE TABLE "usuarios" (
    "id" SERIAL NOT NULL,
    "nombre" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "usuario" TEXT NOT NULL,
    "password" TEXT NOT NULL,
    "rol" TEXT NOT NULL DEFAULT 'tomador',
    "activo" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "usuarios_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "empleados" (
    "id" SERIAL NOT NULL,
    "nombre" TEXT NOT NULL,
    "gafete" TEXT NOT NULL,
    "area" TEXT,
    "activo" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "empleados_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "hojas_tiempo" (
    "id" SERIAL NOT NULL,
    "numeroHoja" TEXT NOT NULL,
    "loteId" INTEGER,
    "tomadorId" INTEGER NOT NULL,
    "fechaEmision" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "turno" TEXT NOT NULL,
    "estado" TEXT NOT NULL DEFAULT 'abierta',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "hojas_tiempo_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "registros_tiempo" (
    "id" SERIAL NOT NULL,
    "hojaId" INTEGER NOT NULL,
    "empleadoId" INTEGER NOT NULL,
    "actividad" TEXT NOT NULL,
    "horaEntrada" TIMESTAMP(3) NOT NULL,
    "horaSalida" TIMESTAMP(3),
    "horasTotales" DOUBLE PRECISION,
    "estado" TEXT NOT NULL DEFAULT 'en_proceso',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "registros_tiempo_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "usuarios_email_key" ON "usuarios"("email");

-- CreateIndex
CREATE UNIQUE INDEX "usuarios_usuario_key" ON "usuarios"("usuario");

-- CreateIndex
CREATE UNIQUE INDEX "empleados_gafete_key" ON "empleados"("gafete");

-- CreateIndex
CREATE UNIQUE INDEX "hojas_tiempo_numeroHoja_key" ON "hojas_tiempo"("numeroHoja");

-- AddForeignKey
ALTER TABLE "hojas_tiempo" ADD CONSTRAINT "hojas_tiempo_tomadorId_fkey" FOREIGN KEY ("tomadorId") REFERENCES "usuarios"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "registros_tiempo" ADD CONSTRAINT "registros_tiempo_hojaId_fkey" FOREIGN KEY ("hojaId") REFERENCES "hojas_tiempo"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "registros_tiempo" ADD CONSTRAINT "registros_tiempo_empleadoId_fkey" FOREIGN KEY ("empleadoId") REFERENCES "empleados"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
