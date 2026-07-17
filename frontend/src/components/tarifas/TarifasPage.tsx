import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useBuscarTarifas, useCrearTarifa, useEliminarTarifa } from "@/hooks/useTarifas";
import { TarifasTable } from "./TarifasTable";
import { TarifaFormDialog } from "./TarifaFormDialog";
import { DeleteConfirmDialog } from "@/components/usuarios/DeleteConfirmDialog";
import type { Tarifa } from "@/types/hotel";
import type { TarifaCreateFormValues } from "@/lib/validators/tarifa";

export function TarifasPage() {
  const [page, setPage] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Tarifa | null>(null);

  const { data: result, isLoading } = useBuscarTarifas({ page, size: 10 });
  const crearTarifa = useCrearTarifa();
  const eliminarTarifa = useEliminarTarifa();

  const tarifas = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  const handleCreate = (data: TarifaCreateFormValues) => {
    crearTarifa.mutate(
      {
        idTipoHabitacion: data.idTipoHabitacion,
        precioNoche: data.precioNoche,
        fechaInicio: data.esPromocional ? data.fechaInicio : null,
        fechaFin: data.esPromocional ? data.fechaFin : null,
      },
      { onSuccess: () => setCreateOpen(false) }
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    eliminarTarifa.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-foreground">Tarifas</h1>
          <p className="text-sm text-muted-foreground">
            Gestión de tarifas del sistema
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nueva Tarifa</Button>
      </div>

      <TarifasTable
        tarifas={tarifas}
        isLoading={isLoading}
        totalPages={totalPages}
        page={page}
        onPageChange={setPage}
        onDelete={setDeleteTarget}
        isDeleting={eliminarTarifa.isPending}
      />

      <TarifaFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearTarifa.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar Tarifa"
        description={`¿Estás seguro de que deseas eliminar la tarifa #${deleteTarget?.id} del tipo "${deleteTarget?.tipoHabitacion?.nombre}"?`}
        onConfirm={handleDelete}
        isLoading={eliminarTarifa.isPending}
      />
    </div>
  );
}
