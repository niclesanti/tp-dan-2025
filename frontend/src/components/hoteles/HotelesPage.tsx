import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Building2, DoorOpen } from "lucide-react";
import { HotelesTab } from "./HotelesTab";
import { HabitacionesTab } from "./HabitacionesTab";

export function HotelesPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-foreground">
          Hoteles & Habitaciones
        </h1>
        <p className="text-sm text-muted-foreground">
          Gestiona los hoteles y habitaciones del sistema
        </p>
      </div>

      <Tabs defaultValue="hoteles">
        <TabsList variant="line">
          <TabsTrigger value="hoteles">
            <Building2 className="mr-1.5 size-4" />
            Hoteles
          </TabsTrigger>
          <TabsTrigger value="habitaciones">
            <DoorOpen className="mr-1.5 size-4" />
            Habitaciones
          </TabsTrigger>
        </TabsList>

        <TabsContent value="hoteles">
          <HotelesTab />
        </TabsContent>

        <TabsContent value="habitaciones">
          <HabitacionesTab />
        </TabsContent>
      </Tabs>
    </div>
  );
}
