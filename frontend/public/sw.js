/**
 * Service worker mínimo para PWA (network-first).
 * No cachea datos ni recursos; solo permite instalación como PWA.
 * Todas las peticiones se sirven desde la red.
 */
self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

// Sin interceptar fetch: todo va a la red (comportamiento por defecto).
