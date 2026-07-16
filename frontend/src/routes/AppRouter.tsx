import { BrowserRouter, Routes, Route } from "react-router-dom";
import { TooltipProvider } from "@/components/ui/tooltip";
import { AppLayout } from "@/components/layout/AppLayout";
import { DashboardPage } from "@/pages/DashboardPage";
import { UsuariosPage } from "@/components/usuarios/UsuariosPage";
import { BancosPage } from "@/components/bancos/BancosPage";
import { HotelesPage } from "@/components/hoteles/HotelesPage";
import { TarifasPage } from "@/components/tarifas/TarifasPage";
import { ReservasPage } from "@/components/reservas/ReservasPage";

export function AppRouter() {
  return (
    <TooltipProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/usuarios" element={<UsuariosPage />} />
            <Route path="/bancos" element={<BancosPage />} />
            <Route path="/hoteles" element={<HotelesPage />} />
            <Route path="/reservas" element={<ReservasPage />} />
            <Route path="/tarifas" element={<TarifasPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  );
}
