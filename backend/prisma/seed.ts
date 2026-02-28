import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
    console.log('🌱 Iniciando seed de base de datos...');

    // Crear usuario tomador de tiempos
    const passwordHash = await bcrypt.hash('password123', 10);

    const tomador = await prisma.usuario.upsert({
        where: { usuario: 'tomador1' },
        update: {},
        create: {
            nombre: 'Juan Pérez',
            email: 'tomador1@pharmadix.com',
            usuario: 'tomador1',
            password: passwordHash,
            rol: 'tomador',
        },
    });
    console.log(`✅ Usuario creado: ${tomador.usuario} (ID: ${tomador.id})`);

    // Crear usuario admin
    const adminHash = await bcrypt.hash('admin123', 10);
    const admin = await prisma.usuario.upsert({
        where: { usuario: 'admin' },
        update: {},
        create: {
            nombre: 'Administrador',
            email: 'admin@pharmadix.com',
            usuario: 'admin',
            password: adminHash,
            rol: 'admin',
        },
    });
    console.log(`✅ Usuario creado: ${admin.usuario} (ID: ${admin.id})`);

    // Crear empleados de ejemplo
    const empleados = [
        { nombre: 'Carlos Ríos', gafete: 'EMP001', area: 'Producción', puesto: 'Operario A' },
        { nombre: 'María López', gafete: 'EMP002', area: 'Producción', puesto: 'Operario B' },
        { nombre: 'Pedro Sánchez', gafete: 'EMP003', area: 'Control de Calidad', puesto: 'Técnico' },
        { nombre: 'Ana García', gafete: 'EMP004', area: 'Almacén', puesto: 'Logística' },
        { nombre: 'Luis Torres', gafete: 'EMP005', area: 'Producción', puesto: 'Supervisor' },
        { nombre: 'Elena Vega', gafete: 'GAF-012', area: 'Producción', puesto: 'Operario Línea A' },
        { nombre: 'Juan García López', gafete: 'GAF-045', area: 'Producción', puesto: 'Operario Línea B' },
        { nombre: 'Lucia Mendez', gafete: 'GAF-092', area: 'Calidad', puesto: 'Técnico Control' },
        { nombre: 'Roberto Gomez', gafete: 'GAF-105', area: 'Producción', puesto: 'Operario C' },
        { nombre: 'Sofia Castro', gafete: 'GAF-112', area: 'Limpieza', puesto: 'Auxiliar' },
        { nombre: 'Ricardo Palma', gafete: 'GAF-120', area: 'Mantenimiento', puesto: 'Técnico' },
        { nombre: 'Carmen Ruiz', gafete: 'GAF-135', area: 'Producción', puesto: 'Operario D' },
        { nombre: 'Daniela Salas', gafete: 'GAF-142', area: 'Producción', puesto: 'Especialista' },
        { nombre: 'Miguel Angel', gafete: 'GAF-155', area: 'Seguridad', puesto: 'Vigilante' },
        { nombre: 'Laura Benitez', gafete: 'GAF-168', area: 'Producción', puesto: 'Operario E' },
    ];

    const dbEmpleados = [];
    for (const emp of empleados) {
        const e = await prisma.empleado.upsert({
            where: { gafete: emp.gafete },
            update: { puesto: emp.puesto },
            create: emp,
        });
        dbEmpleados.push(e);
    }
    console.log(`✅ ${empleados.length} empleados creados`);

    // Crear una hoja de tiempo de ejemplo
    const hoja = await prisma.hojaTiempo.upsert({
        where: { numeroHoja: 'HT-2026-XQ1' },
        update: {},
        create: {
            numeroHoja: 'HT-2026-XQ1',
            tomadorId: tomador.id,
            turno: 'primero',
            estado: 'abierta',
        },
    });
    console.log(`✅ Hoja tiempo creada: ${hoja.numeroHoja}`);

    // Crear algunos registros de tiempo para la hoja
    console.log('⏳ Creando registros de prueba...');

    // 1. Registro Finalizado
    await prisma.registroTiempo.create({
        data: {
            hojaId: hoja.id,
            empleadoId: dbEmpleados[0].id,
            horaEntrada: '08:00:00',
            horaSalida: '16:00:00',
            horasTotales: 8.0,
            estado: 'finalizado'
        }
    });

    // 2. Registro En Proceso
    await prisma.registroTiempo.create({
        data: {
            hojaId: hoja.id,
            empleadoId: dbEmpleados[1].id,
            horaEntrada: '08:15:00',
            estado: 'en_proceso'
        }
    });

    // 3. Otro En Proceso
    await prisma.registroTiempo.create({
        data: {
            hojaId: hoja.id,
            empleadoId: dbEmpleados[5].id,
            horaEntrada: '09:00:00',
            estado: 'en_proceso'
        }
    });

    console.log('\n🎉 Seed completado exitosamente!');
    console.log('📋 Credenciales de prueba:');
    console.log('   usuario: tomador1  |  password: password123');
    console.log('   usuario: admin     |  password: admin123');
}

main()
    .catch((e) => {
        console.error('❌ Error en seed:', e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
