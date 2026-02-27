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
        { nombre: 'Carlos Ríos', gafete: 'EMP001', area: 'Producción' },
        { nombre: 'María López', gafete: 'EMP002', area: 'Producción' },
        { nombre: 'Pedro Sánchez', gafete: 'EMP003', area: 'Control de Calidad' },
        { nombre: 'Ana García', gafete: 'EMP004', area: 'Almacén' },
        { nombre: 'Luis Torres', gafete: 'EMP005', area: 'Producción' },
    ];

    for (const emp of empleados) {
        await prisma.empleado.upsert({
            where: { gafete: emp.gafete },
            update: {},
            create: emp,
        });
    }
    console.log(`✅ ${empleados.length} empleados creados`);

    // Crear una hoja de tiempo de ejemplo
    const hoja = await prisma.hojaTiempo.upsert({
        where: { numeroHoja: 'HT-2024-001' },
        update: {},
        create: {
            numeroHoja: 'HT-2024-001',
            tomadorId: tomador.id,
            turno: 'mañana',
            estado: 'abierta',
        },
    });
    console.log(`✅ Hoja tiempo creada: ${hoja.numeroHoja}`);

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
