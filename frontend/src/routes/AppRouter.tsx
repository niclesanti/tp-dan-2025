import { BrowserRouter, Routes, Route } from "react-router-dom";
import { TooltipProvider } from "@/components/ui/tooltip";
import { AppLayout } from "@/components/layout/AppLayout";
import { UsuariosPage } from "@/components/usuarios/UsuariosPage";
import { BancosPage } from "@/components/bancos/BancosPage";
import { HotelesPage } from "@/components/hoteles/HotelesPage";
import { TarifasPage } from "@/components/tarifas/TarifasPage";
import { ReservasPage } from "@/components/reservas/ReservasPage";
import { LoginPage } from "@/pages/LoginPage";

export function AppRouter() {
  return (
    <TooltipProvider>
      <BrowserRouter>
        <Routes>
          {/* Rutas sin layout (página completa) */}
          <Route path="/login" element={<LoginPage />} />

          {/* Rutas con layout principal */}
          <Route element={<AppLayout />}>
            <Route index element={<ReservasPage />} />
            <Route path="/usuarios" element={<UsuariosPage />} />
            <Route path="/bancos" element={<BancosPage />} />
            <Route path="/hoteles" element={<HotelesPage />} />
            <Route path="/tarifas" element={<TarifasPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  );
}
