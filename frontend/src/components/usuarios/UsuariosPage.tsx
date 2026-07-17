import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Users, Building2 } from "lucide-react";
import { HuespedesTab } from "./HuespedesTab";
import { PropietariosTab } from "./PropietariosTab";

export function UsuariosPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-foreground">
          Propietarios & Huéspedes
        </h1>
        <p className="text-sm text-muted-foreground">
          Gestiona los usuarios del sistema
        </p>
      </div>

      <Tabs defaultValue="huespedes">
        <TabsList variant="line">
          <TabsTrigger value="huespedes">
            <Users className="mr-1.5 size-4" />
            Huéspedes
          </TabsTrigger>
          <TabsTrigger value="propietarios">
            <Building2 className="mr-1.5 size-4" />
            Propietarios
          </TabsTrigger>
        </TabsList>

        <TabsContent value="huespedes">
          <HuespedesTab />
        </TabsContent>

        <TabsContent value="propietarios">
          <PropietariosTab />
        </TabsContent>
      </Tabs>
    </div>
  );
}