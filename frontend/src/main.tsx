import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";

// Registrar service worker mínimo (PWA network-first: solo instalable, sin caché offline)
if ("serviceWorker" in navigator && import.meta.env.PROD) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/sw.js").catch(() => {});
  });
}

createRoot(document.getElementById("root")!).render(<App />);
