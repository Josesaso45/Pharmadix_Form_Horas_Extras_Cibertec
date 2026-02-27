# Changelog - Pharmadix Times

Todas las modificaciones notables a este proyecto serán documentadas en este archivo.  
Formato: [Keep a Changelog](https://keepachangelog.com/es/1.0.0/) · Versiones: [Semver](https://semver.org/lang/es/)

---

## [3.0.0] - 2026-02-27 🚀 — App Android + Backend Node.js + Documentación Completa

### 🆕 Añadido

#### App Android Nativa (Kotlin)
- **`LoginActivity`** con View Binding, Material Design 3 y conexión REST vía Retrofit
- **`DashboardActivity`** con Navigation Component (sin Jetpack Compose)
- **`RegistroOperariosFragment`** con RecyclerView personalizado + ZXing QR Scanner
- **`RegistroOperarioAdapter`** con Chip de estado dinámico (PENDIENTE/EN_PROCESO/FINALIZADO)
- **Base de datos Room (SQLite)** con 5 entidades: `Empleado`, `Lote`, `HojaTiempo`, `RegistroTiempo`, `Usuario`
- **3 DAOs completos**: `EmpleadoDao`, `HojaTiempoDao`, `RegistroTiempoDao` (CRUD + queries especializados)
- **`PharmadixDatabase`** singleton con patrón `@Volatile` + `synchronized`
- **`ApiService`** con 5 endpoints Retrofit: login, hojas (x2), registros entrada, registros salida
- **`RetrofitClient`** con `OkHttpClient` + JWT Interceptor + Logging Interceptor
- **`ApiModels.kt`** con data classes Request/Response y `@SerializedName` para Gson
- **Íconos vectoriales** para estados: pendiente, en proceso, finalizado, QR, sync
- **Logo Pharmadix** integrado en `activity_login.xml` con tarjeta 160×72dp
- **Permisos**: `INTERNET` + `CAMERA` en `AndroidManifest.xml`
- **Soporte Offline-First**: `HojaTiempoDao.obtenerPendientesSincronizacion()` + campo `sincronizada`

#### Backend API REST (Node.js + Fastify)
- **`src/server.ts`**: servidor Fastify con JWT, CORS y registro de rutas
- **`routes/auth.ts`**: `POST /auth/login` + `GET /auth/me` con JWT de 8h
- **`routes/hojas.ts`**: CRUD de hojas de tiempo con validación de lotes
- **`routes/registros.ts`**: registro de entrada/salida con cálculo automático de horas
- **Prisma ORM** con schema completo: `Usuario`, `Empleado`, `HojaTiempo`, `DetalleRegistro`
- **Migración inicial** (`prisma/migrations/20260227212332_init/migration.sql`)
- **Seed de datos** (`prisma/seed.ts`) con 2 usuarios + 5 empleados + 1 hoja de ejemplo
- **Autenticación JWT** con bcryptjs para hash de contraseñas
- **Validación de esquemas** con Zod en todos los endpoints
- **`Dockerfile`** optimizado para Node.js 18 slim

#### Infraestructura Docker
- **`docker-compose.yml`** con 3 servicios: `database` (Postgres 15), `backend` (Node.js), `frontend` (React+Nginx)
- **Red interna** `pharmadix-network` para comunicación entre servicios
- **Volumen persistente** `postgres_data` para datos de la base de datos
- **`frontend/Dockerfile`** con build multi-stage (Vite build + Nginx serve)

#### Documentación Académica (Cibertec)
- **`Pharmadix_Times_Informe_Proyecto.md`** expandido a ~1740 líneas cubriendo:
  - Secciones 6.2.1–6.2.3: Entidades Room `@Entity` + DAOs con CRUD + `PharmadixDatabase` (4 pts rúbrica)
  - Secciones 6.4.1–6.4.4: `ApiService` + `RetrofitClient` + modelos + 3 ejemplos JSON (4 pts rúbrica)
  - Secciones 7.2.1–7.2.2: XML completo `item_registro_operario.xml` + lógica Chip de estado (4 pts rúbrica)
  - Sección E: Modelo BD completo (ER diagram como PNG + tablas + script DDL PostgreSQL + triggers)
  - Sección F: 5 casos de uso + 8 RF + 6 RNF + 5 reglas de negocio + diagrama de flujo
  - Sección G: Portal Admin (7 módulos + 11 endpoints REST)
- **5 diagramas PNG** generados con mcp-mermaid (compatibles con PDF/Word):
  - `diagrama_c4_flujo_online_offline.png`
  - `diagrama_c5_arquitectura_sistema.png`
  - `diagrama_c6_maquina_estados.png`
  - `diagrama_e1_er.png`
  - `diagrama_f5_flujo_app.png`
- **2 mockups Stitch** exportados: `mockup_login.png`, `mockup_registro_operarios.png`

#### Mockups y Diseño (Google Stitch MCP)
- Proyecto Stitch "Pharmadix Times – App Android MVP" con 8 screens:
  - Login (2 variantes con logo)
  - Registro de Operarios (4 variantes con RecyclerView)
  - Dashboard / Selección de Hoja (nuevo)
- Tema: azul corporativo `#1A237E`, fuente Lexend, corner radius 8dp

### 🔧 Modificado
- **`android_app/build.gradle.kts`**: AGP 9.0.1, KSP, Room 2.7.0, Retrofit 2.11.0, Navigation 2.8.9
- **`mipmap-anydpi/`** → **`mipmap-anydpi-v26/`**: fix para `<adaptive-icon>` en SDK 26+ (resuelve error de linking AGP 9.0.1)
- **`colors.xml`**: agregado `ic_launcher_background`, `estado_pendiente`, `estado_en_proceso`, `estado_finalizado`
- **`README.md`**: reescrito completamente con stack actualizado, instrucciones Docker, credenciales, estructura de proyecto
- **`docker-compose.yml`**: corregida variable `DATABASE_URL` para conexión interna entre contenedores

### 🐛 Corregido
- Error al compilar: `<adaptive-icon> elements require a sdk version of at least 26` → resuelto moviendo XMLs a `mipmap-anydpi-v26/`
- Error al compilar: `@font/roboto_bold not found` → eliminada referencia al font no declarado en `activity_login.xml`
- Error de runtime: `Error de conexión: failed to connect to /10.0.2.2 (port 3000)` → el backend ahora corre en Docker y el seed crea usuarios
- Diagramas Mermaid con `Syntax error in graph (mermaid version 8.8.0)` → reemplazados por PNGs generados + fuente en `<details>` colapsable
- `subgraph id [label]` no soportado en Mermaid 8.8 → sintaxis `subgraph id` sin brackets
- `flowchart TD` → reemplazado por `graph TD` para compatibilidad 8.8
- `stateDiagram-v2` → reemplazado por `stateDiagram`

---

## [2.2.0] - 2026-02-03

### Añadido
- **README.md**: guía principal del proyecto con instrucciones de instalación.

### Modificado
- **Nueva Identidad Visual**: paleta de colores Gigas, Minsk y Lavender Purple.
- **Diagramas Mermaid.js**: integración en portal de documentación HTML.

### Corregido
- **NuevaHoja.tsx**: error `Badge is not defined` al buscar lotes.
- **Flujo de Navegación**: estabilidad mejorada en creación de hojas.

### Mejorado
- **Registro de Operarios**: botón "Escanear QR" con escaneo automático.
- **Identificación Automática**: búsqueda por ID/gafete tras escaneo.

---

## [2.1.0] - 2026-01-27

### Mejorado (UX/UI)
- Formulario unificado de ingreso de lotes con campos obligatorios.
- Máquina de estados (Pendiente → En Proceso → Finalizado) para registros.
- Botón contextual (Entrada/Salida) según estado del operario.

### Añadido (Backend/DB)
- Flujo de aprobación secuencial (Firma Tomador → Firma Jefe de Manufactura).
- Campos de auditoría para estado de aprobación y firmas digitales.
- Índices compuestos en tablas transaccionales.

---

## [2.0.0] - 2026-01-27

### Modificado
- **Arquitectura**: migración a PWA (React 18 + Vite + Tailwind).
- **Backend**: Node.js + Fastify + Prisma + PostgreSQL 15.
- **Capacidad Offline**: Service Workers + IndexedDB (Dexie.js).

### Añadido
- Dockerización completa.
- Controles de auditoría ALCOA+.
- Doble método de identificación (QR + búsqueda manual).

---

## [1.0.0] - 2026-01-15

### Inicial
- Análisis de requisitos funcionales y no funcionales.
- Diseño inicial de base de datos relacional.
- Definición de flujo AS-IS y TO-BE.
- Informe ejecutivo de digitalización.
