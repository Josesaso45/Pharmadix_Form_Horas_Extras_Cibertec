import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, Outlet } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import NuevaHoja from "./pages/NuevaHoja";
import RegistroOperarios from "./pages/RegistroOperarios";
import MisHojas from "./pages/MisHojas";
import NotFound from "./pages/NotFound";
import AdminLayout from "./components/layout/AdminLayout";
import CalculoHH from "./pages/admin/CalculoHH";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner position="top-center" richColors />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/nueva-hoja" element={<NuevaHoja />} />
          <Route path="/registro-operarios" element={<RegistroOperarios />} />
          <Route path="/mis-hojas" element={<MisHojas />} />

          {/* Admin Routes */}
          <Route path="/admin" element={<AdminLayout><Outlet /></AdminLayout>}>
            <Route path="calculo-hh" element={<CalculoHH />} />
            {/* Otras rutas admin... */}
          </Route>

          {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
