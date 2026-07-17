import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Search, CalendarCheck } from "lucide-react";
import { BuscarHabitacionesTab } from "./BuscarHabitacionesTab";
import { GestionReservasTab } from "./GestionReservasTab";

export function ReservasPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-foreground">
          Reservas
        </h1>
        <p className="text-sm text-muted-foreground">
          Busca habitaciones disponibles y gestiona las reservas del sistema
        </p>
      </div>

      <Tabs defaultValue="buscar">
        <TabsList variant="line">
          <TabsTrigger value="buscar">
            <Search className="mr-1.5 size-4" />
            Buscar Habitaciones
          </TabsTrigger>
          <TabsTrigger value="gestion">
            <CalendarCheck className="mr-1.5 size-4" />
            Gestión de Reservas
          </TabsTrigger>
        </TabsList>

        <TabsContent value="buscar">
          <BuscarHabitacionesTab />
        </TabsContent>

        <TabsContent value="gestion">
          <GestionReservasTab />
        </TabsContent>
      </Tabs>
    </div>
  );
}
