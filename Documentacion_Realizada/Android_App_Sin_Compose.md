# Guía de Desarrollo – App Android (sin Jetpack Compose)

Este documento describe una posible implementación **nativa Android (Views XML, sin Jetpack Compose)** de Pharmadix Times, alineada con la arquitectura y procesos ya definidos para la PWA.

Se apoya en los siguientes documentos de referencia:

- `Documentacion_Realizada/Arquitectura_Diseno_Tecnico_v2.md`
- `Documentacion_Realizada/Flujo_Procesos_Pharmadix.md`
- Portal HTML: `Documentacion_Realizada/Referencia_Documentacion/index.html`

La versión ejecutiva y resumida de esta guía se encuentra en:  
`Documentacion_Realizada/Android_App_Sin_Compose.md`.

---

## 1. Alcance (MVP: Registro de Operarios)

Este documento se centra en el **primer MVP nativo Android**, cuyo alcance es exclusivamente:

- Registro de operarios mediante **QR** para una hoja de tiempo ya creada en el sistema.
- Visualización del listado de registros de tiempo de una hoja activa.
- Registro de **entrada** y **salida** de operarios (cálculo de horas).
- Sincronización de estos registros con el backend Fastify/Node.js existente.

Quedan **fuera del alcance de este MVP** (planificados para fases posteriores):

- Gestión completa de lotes y creación de hojas (se asume que la hoja existe).
- Pantalla de “Mis Hojas” con filtros avanzados.
- Flujo de cierre y aprobación de hoja con doble confirmación.
- Pantallas de administración y reportes avanzados.

> **Enfoque:** Network-first. La app requiere conexión para uso normal y solo usa Room como caché ligera y apoyo ante reconexiones breves.

---

## 2. Arquitectura propuesta (resumen, centrada en registro de operarios)

- **Presentación (UI)**: Activities + Fragments con layouts XML y Navigation Component.
- **Dominio (opcional)**: Casos de uso centrados en:
  - Seleccionar una hoja de tiempo activa.
  - Registrar entrada y salida de operarios.
  - Consultar registros de tiempo de una hoja.
- **Datos**:
  - API REST (Retrofit + OkHttp) para:
    - Obtener la hoja de tiempo y estado actual de registros.
    - Enviar altas/actualizaciones de registros de tiempo.
  - Room (SQLite) para almacenar:
    - Empleados (cacheados por gafete).
    - Registros de tiempo locales de la hoja activa.

La estructura de tablas y estados debe ser consistente con el modelo descrito en:

- Secciones de modelo de datos en `Arquitectura_Diseno_Tecnico_v2.md`.
- Estados y flujos operativos en `Flujo_Procesos_Pharmadix.md`.

---

## 3. Diseño de base de datos local (Room)

Tablas sugeridas:

- `empleados(id, gafete, nombre, puesto, foto, activo, ...)`
- `lotes(id, numero, producto, presentacion, proceso, area, cantidadOrdenada, estado, fechaInicio, ...)`
- `hojas_tiempo(id, numeroHoja, loteId, tomadorId, fechaEmision, turno, estado, sincronizada, ...)`
- `registros_tiempo(id, hojaId, empleadoId, actividad, horaEntrada, horaSalida, horasTotales, estado, ...)`
- `usuarios(id, nombre, email, rol, ...)`

Los DAOs (`EmpleadoDao`, `LoteDao`, `HojaTiempoDao`, `RegistroTiempoDao`, `UsuarioDao`) deben permitir:

- Consultas por estado (BORRADOR, CERRADA, APROBADA).
- Filtrado por hoja, lote y empleado.
- Marcado de registros/hojas pendientes de sincronizar si se modela una cola de reintentos.

---

## 4. Flujo de pantallas (MVP registro de operarios)

Tomando como base `Flujo_Procesos_Pharmadix.md`, el MVP incluye solo estas pantallas:

1. **LoginActivity**
   - Autenticación contra la API.
   - Tras login exitoso, navega a selección de hoja.

2. **SeleccionHojaFragment** (puede ser parte del Dashboard)
   - Lista de hojas activas asignadas al tomador (o ingreso manual de ID de hoja).
   - Al seleccionar una hoja, navega a `RegistroOperariosFragment`.

3. **RegistroOperariosFragment** (pantalla principal del MVP)
   - Muestra:
     - Datos básicos de la hoja seleccionada (lote, turno, fecha).
     - Lista de registros de tiempo de los operarios (estado PENDIENTE / EN_PROCESO / FINALIZADO).
   - Funcionalidad:
     - Botón **Escanear QR** para leer gafete del operario.
     - Si el operario no tiene registro en la hoja → mostrar acción de **Entrada**.
     - Si el operario está EN_PROCESO → mostrar acción de **Salida**.
     - Después de registrar salida → calcular horas totales y marcar como FINALIZADO.

4. **(Opcional) DetalleOperarioFragment**
   - Pantalla simple con el detalle del registro de un operario (historial de entradas/salidas si se desea).

Pantallas como “MisHojasFragment” general o flujos de cierre/aprobación quedan fuera de este MVP y se documentarán en iteraciones futuras.

---

## 5. Sincronización y ALCOA+

Aunque la app es network-first, deben respetarse los principios **ALCOA+** descritos en la arquitectura:

- Registrar quién realiza cada acción (ID de usuario autenticado).
- Mantener marcas de tiempo confiables (idealmente UTC).
- Conservar trazabilidad de cambios de estado (BORRADOR → CERRADA → APROBADA).
- Mantener, cuando sea necesario, logs de auditoría compatibles con lo descrito para PostgreSQL (por ejemplo, enviando al backend suficiente información para poblar las tablas de auditoría).

Room actúa como apoyo para:

- Acelerar cargas frecuentes (hojas, lotes, empleados).
- Reducir impacto de cortes breves de red (guardar temporalmente hasta reintentar).

---

## 6. Desarrollo paso a paso (overview MVP)

1. **Crear proyecto** en Android Studio usando plantilla *Empty Views Activity* (sin Compose).
2. **Configurar Gradle** con:
   - AppCompat, Material, ConstraintLayout.
   - Navigation Component.
   - Lifecycle (ViewModel + LiveData/StateFlow).
   - Room.
   - Retrofit + OkHttp.
   - Librería de QR (ZXing u otra).
3. **Definir entidades y DAOs Room** para:
   - `Empleado`.
   - `RegistroTiempo` (solo campos necesarios para el MVP).
4. **Definir servicios Retrofit** para:
   - Login.
   - Obtener hoja de tiempo y registros asociados.
   - Crear/actualizar registros de tiempo (entrada/salida).
5. **Implementar ViewModels y casos de uso** para:
   - Seleccionar hoja.
   - Registrar entrada de operario.
   - Registrar salida y cálculo de horas.
6. **Conectar UI XML** (Activities/Fragments) con ViewModels, aplicando el flujo descrito en `Flujo_Procesos_Pharmadix.md` para el bloque de “Registro Masivo”.
7. **Validar con usuarios clave** que la experiencia de registro de operarios (QR + entrada/salida) es equivalente o mejor que la PWA actual.

Para más detalle técnico y ejemplos de código, consultar el documento completo en `docs/ANDROID_APP_SIN_COMPOSE.md`.

