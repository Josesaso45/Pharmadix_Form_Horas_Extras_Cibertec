# Pharmadix Times 🏭📱

**Sistema de Control de Tiempos de Producción Farmacéutica**  
App Android nativa + Backend Node.js + Portal Web React PWA

[![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow)](https://github.com/Josesaso45/Pharmadix_Form_Horas_Extras_Cibertec)
[![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Room%20%2B%20Retrofit-green)](./android_app)
[![Backend](https://img.shields.io/badge/Backend-Node.js%20%2B%20Fastify%20%2B%20Prisma-blue)](./backend)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](./docker-compose.yml)

---

## 📋 Descripción del Proyecto

**Pharmadix Times** digitaliza el control de tiempos de operarios en planta farmacéutica. Reemplaza hojas de papel por:

- 📱 **App Android nativa** (sin Jetpack Compose) para el Tomador de Tiempos
- 🌐 **Portal Web React PWA** para Jefes de Manufactura y Administradores
- 🔧 **API REST Node.js/Fastify** como backend unificado
- 🗄️ **PostgreSQL** para persistencia central
- 📦 **SQLite/Room** para operación offline en la app

---

## 🛠️ Stack Tecnológico

### App Android (Principal)
| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.0.21 | Lenguaje principal |
| Android Gradle Plugin | 9.0.1 | Build system |
| Room + KSP | 2.7.0 | Base de datos local SQLite |
| Retrofit + OkHttp | 2.11.0 | Consumo de API REST |
| Material Design 3 | 1.13.0 | UI/UX |
| Navigation Component | 2.8.9 | Navegación entre fragments |
| ZXing (QR) | 4.3.0 | Escaneo de gafetes |
| Coroutines | 1.10.1 | Programación asíncrona |

### Backend API
| Tecnología | Uso |
|---|---|
| Node.js 18 + Fastify | Servidor HTTP de alto rendimiento |
| Prisma ORM | Acceso a base de datos |
| PostgreSQL 15 | Base de datos principal |
| JWT (jsonwebtoken) | Autenticación |
| bcryptjs | Hash de contraseñas |
| Zod | Validación de esquemas |

### Frontend Portal Web
| Tecnología | Uso |
|---|---|
| React 18 + TypeScript | Framework UI |
| Vite | Build tool |
| Tailwind CSS + shadcn/ui | Estilos y componentes |
| html5-qrcode | Escaneo QR en navegador |

---

## 🚀 Inicio Rápido (Docker)

### Requisitos
- Docker Desktop instalado y corriendo
- Android Studio (para la app móvil)

### Levantar el sistema completo

```bash
# Clonar el repositorio
git clone https://github.com/Josesaso45/Pharmadix_Form_Horas_Extras_Cibertec.git
cd Pharmadix_Form_Horas_Extras_Cibertec

# Levantar backend + base de datos + frontend web
docker-compose up -d --build

# Poblar la base de datos con datos de prueba
docker exec pharmadix-backend npx ts-node prisma/seed.ts
```

### Servicios disponibles

| Servicio | URL | Descripción |
|---|---|---|
| **API REST** | `http://localhost:3000` | Backend Fastify |
| **Portal Web** | `http://localhost:80` | React PWA |
| **PostgreSQL** | `localhost:5432` | Base de datos |

### Credenciales de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `tomador1` | `password123` | Tomador de Tiempos (app Android) |
| `admin` | `admin123` | Administrador (portal web) |

---

## 📱 App Android

### Configuración rápida

1. Abrir `android_app/` en **Android Studio**
2. Hacer **Sync Now** (Gradle)
3. El backend URL ya está configurado para emulador en `RetrofitClient.kt`:
   ```kotlin
   // Para emulador (10.0.2.2 = localhost del PC)
   private const val BASE_URL = "http://10.0.2.2:3000/"
   
   // Para dispositivo físico (cambiar por tu IP local)
   // private const val BASE_URL = "http://192.168.1.XXX:3000/"
   ```
4. Asegúrate que Docker esté corriendo, luego **▶️ Run**

### Pantallas implementadas

| Pantalla | Archivo | Estado |
|---|---|---|
| Login | `LoginActivity` | ✅ Completo |
| Dashboard | `DashboardActivity` | ✅ Completo |
| Registro de Operarios | `RegistroOperariosFragment` | ✅ Completo |
| Selección de Hoja | `SeleccionHojaFragment` | 🔄 En desarrollo |

---

## 📂 Estructura del Proyecto

```
Pharmadix_Form_Horas_Extras_Cibertec/
├── android_app/                    # App Android nativa (Kotlin)
│   └── app/src/main/
│       ├── java/.../
│       │   ├── data/local/         # Room entities + DAOs + Database
│       │   ├── data/remote/        # RetrofitClient + ApiService + Models
│       │   └── ui/                 # Activities + Fragments + ViewModels
│       └── res/                    # Layouts XML + recursos
├── backend/                        # API REST Node.js/Fastify
│   ├── src/routes/                 # auth.ts | hojas.ts | registros.ts
│   ├── prisma/                     # Schema + Migrations + Seed
│   └── Dockerfile
├── frontend/                       # Portal Web React PWA
│   └── Dockerfile
├── Documentacion_Proyecto_Cibertec/  # Informe académico Cibertec
│   ├── Pharmadix_Times_Informe_Proyecto.md
│   └── diagrama_*.png              # Diagramas exportados
├── Documentacion_Realizada/        # Documentación técnica complementaria
├── docker-compose.yml              # Orquestación de servicios
└── README.md
```

---

## 📖 Documentación

| Documento | Ubicación |
|---|---|
| **Informe Académico Cibertec** | `Documentacion_Proyecto_Cibertec/Pharmadix_Times_Informe_Proyecto.md` |
| **Manual de Usuario** | `Documentacion_Realizada/MANUAL_USUARIO.md` |
| **Manual del Desarrollador** | `Documentacion_Realizada/MANUAL_DESARROLLADOR.md` |
| **Arquitectura Técnica** | `Documentacion_Realizada/Arquitectura_Diseno_Tecnico_v2.md` |

---

## 🔌 Endpoints API REST

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `POST` | `/auth/login` | ❌ | Login → JWT |
| `GET` | `/auth/me` | ✅ JWT | Info del usuario actual |
| `GET` | `/hojas` | ✅ JWT | Lista hojas de tiempo |
| `GET` | `/hojas/:id` | ✅ JWT | Detalle de hoja |
| `POST` | `/registros` | ✅ JWT | Registrar entrada |
| `PUT` | `/registros/:id/salida` | ✅ JWT | Registrar salida |

---

## 👥 Contribuidores

Proyecto académico - **Cibertec** · Desarrollo de Aplicaciones Móviles I (4693) · 2026

© 2026 Pharmadix Corp. S.A. | Todos los derechos reservados.
