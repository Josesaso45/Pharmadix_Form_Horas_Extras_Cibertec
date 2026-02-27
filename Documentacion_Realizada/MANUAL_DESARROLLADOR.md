# Manual del Desarrollador - Pharmadix Times

## 🛠️ Stack Tecnológico
- **Frontend**: React 18 + TypeScript + Vite.
- **Estilos**: Tailwind CSS + shadcn/ui.
- **Iconografía**: Lucide React.
- **Escaneo QR**: html5-qrcode.
- **Persistencia Local**: Browser LocalStorage. PWA network-first (requiere conexión para uso completo).
- **Gestión de Estados**: React Hooks (useState, useEffect).

## 📂 Estructura del Proyecto
```
frontend/
├── src/
│   ├── components/       # Componentes reutilizables
│   │   ├── layout/       # Header, PageContainer
│   │   ├── operators/    # QrScannerModal, OperatorCard, OperatorSearch
│   │   └── ui/           # Componentes de shadcn (botón, diálogo, etc.)
│   ├── data/             # mockData.ts (Data maestra temporal)
│   ├── pages/            # Pantallas principales (Login, Dashboard, etc.)
│   ├── types/            # Definiciones de interfaces TypeScript
│   └── App.tsx           # Configuración de rutas (React Router)
└── public/               # Activos estáticos (logo, favicon)
```

## ⚙️ Configuración del Entorno
1. Instalar dependencias:
   ```bash
   npm install
   ```
2. Iniciar servidor de desarrollo:
   ```bash
   npm run dev
   ```
3. Abrir en el navegador: `http://localhost:8080`.

## 🔄 Lógica de la Aplicación

### Máquina de Estados de Operarios
El registro de cada operario sigue este flujo de estados definido en `RegistroOperarios.tsx`:
1. **PENDIENTE**: Operario identificado pero sin entrada.
2. **EN_PROCESO**: Entrada registrada, esperando salida.
3. **FINALIZADO**: Entrada y salida registradas, horas calculadas.

### Implementación del QR (`QrScannerModal.tsx`)
Utiliza la API de bajo nivel de `html5-qrcode`. Es crítico el manejo del ciclo de vida:
- `start()`: Inicia la cámara con `facingMode: "environment"`.
- `stop()`: Debe llamarse al cerrar el modal para liberar el hardware.
- Se implementó un delay de 300ms antes de iniciar para asegurar la carga del elemento en el DOM.

## 🚀 Próximos Pasos (Hoja de Ruta)
1. **Migración a Backend Real**: Reemplazar `mockData.ts` con llamadas a una API REST (Fastify).
2. **Sincronización Batch**: Implementar lógica para enviar hojas guardadas en el almacenamiento local al servidor mediante una cola de tareas.
3. **PWA Avanzada**: (Opcional) evaluar uso de estrategias de caché más avanzadas si se vuelve a un enfoque offline-first.

## 📝 Convenciones de Código
- Mantener componentes pequeños y enfocados.
- Tipar todas las interfaces en `types/pharmadix.ts`.
- Usar colores de la paleta institucional (`--primary`, `--success`, etc.) definidos en `index.css`.

