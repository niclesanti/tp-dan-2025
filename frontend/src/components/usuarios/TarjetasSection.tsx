import { useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Spinner } from "@/components/ui/spinner";
import { CreditCard, Plus, Star, Trash2 } from "lucide-react";
import { useListarTarjetas, useEliminarTarjeta, useCambiarPrincipal } from "@/hooks/useTarjetas";
import { DeleteConfirmDialog } from "./DeleteConfirmDialog";
import { TarjetaFormDialog } from "./TarjetaFormDialog";
import { useAgregarTarjeta } from "@/hooks/useTarjetas";
import type { TarjetaCredito } from "@/types/usuario";
import type { TarjetaFormValues } from "@/lib/validators/huesped";

interface TarjetasSectionProps {
  huespedId: number;
}

export function TarjetasSection({ huespedId }: TarjetasSectionProps) {
  const [addOpen, setAddOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  const { data: tarjetasData, isLoading } = useListarTarjetas(huespedId);
  const agregarTarjeta = useAgregarTarjeta();
  const eliminarTarjeta = useEliminarTarjeta();
  const cambiarPrincipal = useCambiarPrincipal();

  const tarjetas: TarjetaCredito[] = tarjetasData?.content ?? [];

  const handleAdd = (data: TarjetaFormValues) => {
    agregarTarjeta.mutate(
      { huespedId, data },
      { onSuccess: () => setAddOpen(false) }
    );
  };

  const handleDelete = () => {
    if (deleteTarget === null) return;
    eliminarTarjeta.mutate(
      { huespedId, tarjetaId: deleteTarget },
      { onSuccess: () => setDeleteTarget(null) }
    );
  };

  const handleSetPrimary = (tarjetaId: number) => {
    cambiarPrincipal.mutate({ huespedId, tarjetaId });
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h4 className="text-sm font-medium text-foreground">Tarjetas de Crédito</h4>
        <Button
          variant="outline"
          size="sm"
          onClick={() => setAddOpen(true)}
        >
          <Plus className="mr-1 size-3" />
          Agregar
        </Button>
      </div>

      {tarjetas.length === 0 ? (
        <p className="text-sm text-muted-foreground">No tiene tarjetas registradas</p>
      ) : (
        <div className="space-y-2">
          {tarjetas.map((t) => (
            <Card key={t.id} className="bg-background border-border">
              <CardContent className="flex items-center justify-between p-3">
                <div className="flex items-center gap-3">
                  <CreditCard className="size-4 text-muted-foreground" />
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-foreground">
                        **** **** **** {t.numero.slice(-4)}
                      </span>
                      {t.esPrincipal && (
                        <Badge variant="secondary" className="text-xs">
                          <Star className="mr-1 size-3" />
                          Principal
                        </Badge>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {t.nombreTitular} · {t.nombreBanco} · Vence {t.fechaVencimiento}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  {!t.esPrincipal && (
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      onClick={() => handleSetPrimary(t.id)}
                      aria-label="Establecer como principal" title="Establecer como principal"
                    >
                      <Star className="size-3" />
                    </Button>
                  )}
                  {!t.esPrincipal && (
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      onClick={() => setDeleteTarget(t.id)}
                      aria-label="Eliminar tarjeta" title="Eliminar tarjeta"
                    >
                      <Trash2 className="size-3 text-destructive" />
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <TarjetaFormDialog
        open={addOpen}
        onOpenChange={setAddOpen}
        onSubmit={handleAdd}
        isLoading={agregarTarjeta.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar tarjeta"
        description="¿Estás seguro de que deseas eliminar esta tarjeta de crédito?"
        onConfirm={handleDelete}
        isLoading={eliminarTarjeta.isPending}
      />
    </div>
  );
}