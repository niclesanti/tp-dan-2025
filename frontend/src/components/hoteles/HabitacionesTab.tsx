import { useState, useMemo } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Pencil, Trash2, DoorOpen, DollarSign } from "lucide-react";
import { useBuscarHabitaciones, useCrearHabitacion, useActualizarHabitacion, useEliminarHabitacion, useTiposHabitacion, useTarifaVigente } from "@/hooks/useHoteles";
import { HabitacionFormDialog } from "./HabitacionFormDialog";
import { DeleteConfirmDialog } from "@/components/usuarios/DeleteConfirmDialog";
import type { Habitacion } from "@/types/hotel";
import type { HabitacionCreateFormValues, HabitacionUpdateFormValues } from "@/lib/validators/habitacion";

export function HabitacionesTab() {
  const [page, setPage] = useState(0);
  const [capacidadFilter, setCapacidadFilter] = useState("");
  const [tipoFilter, setTipoFilter] = useState<string>("");
  const [precioMinFilter, setPrecioMinFilter] = useState("");
  const [precioMaxFilter, setPrecioMaxFilter] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Habitacion | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Habitacion | null>(null);

  const { data: tipos = [] } = useTiposHabitacion();

  const params = useMemo(() => ({
    page,
    size: 10,
    ...(capacidadFilter ? { cantidadHuespedes: Number(capacidadFilter) } : {}),
    ...(tipoFilter ? { idTipoHabitacion: Number(tipoFilter) } : {}),
    ...(precioMinFilter ? { precioMinimo: Number(precioMinFilter) } : {}),
    ...(precioMaxFilter ? { precioMaximo: Number(precioMaxFilter) } : {}),
  }), [page, capacidadFilter, tipoFilter, precioMinFilter, precioMaxFilter]);

  const { data: result, isLoading } = useBuscarHabitaciones(params);

  const crearHabitacion = useCrearHabitacion();
  const actualizarHabitacion = useActualizarHabitacion();
  const eliminarHabitacion = useEliminarHabitacion();

  const habitaciones = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  const handleCreate = (data: HabitacionCreateFormValues | HabitacionUpdateFormValues) => {
    crearHabitacion.mutate(data as HabitacionCreateFormValues, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  const handleEdit = (data: HabitacionCreateFormValues | HabitacionUpdateFormValues) => {
    if (!editTarget) return;
    actualizarHabitacion.mutate(
      { id: editTarget.id, data: data as HabitacionUpdateFormValues },
      { onSuccess: () => setEditTarget(null) }
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    eliminarHabitacion.mutate(deleteTarget.id, {
      onSuccess: () => setDeleteTarget(null),
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex flex-1 items-center gap-2">
          <Input
            placeholder="Capacidad"
            type="number"
            min={1}
            value={capacidadFilter}
            onChange={(e) => { setCapacidadFilter(e.target.value); setPage(0); }}
            className="w-[110px]"
          />
          <Select
            value={tipoFilter}
            onValueChange={(val) => { setTipoFilter(val === "all" ? "" : (val ?? "")); setPage(0); }}
          >
            <SelectTrigger className="w-[160px]">
              <SelectValue placeholder="Tipo" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todos</SelectItem>
              {tipos.map((t) => (
                <SelectItem key={t.id} value={t.id.toString()}>
                  {t.nombre}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Input
            placeholder="Precio mín"
            type="number"
            min={0}
            value={precioMinFilter}
            onChange={(e) => { setPrecioMinFilter(e.target.value); setPage(0); }}
            className="w-[110px]"
          />
          <Input
            placeholder="Precio máx"
            type="number"
            min={0}
            value={precioMaxFilter}
            onChange={(e) => { setPrecioMaxFilter(e.target.value); setPage(0); }}
            className="w-[110px]"
          />
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nueva Habitación</Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : habitaciones.length === 0 ? (
        <div className="py-12 text-center">
          <DoorOpen className="mx-auto mb-3 size-10 text-muted-foreground/40" />
          <p className="text-sm text-muted-foreground">No se encontraron habitaciones</p>
        </div>
      ) : (
        <>
          <div className="rounded-xl border border-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[50px]">ID</TableHead>
                  <TableHead>Nro.</TableHead>
                  <TableHead>Piso</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Cap.</TableHead>
                  <TableHead>Hotel</TableHead>
                  <TableHead className="w-[120px]">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {habitaciones.map((hab) => (
                  <TableRow key={hab.id}>
                    <TableCell>
                      <Badge variant="secondary">{hab.id}</Badge>
                    </TableCell>
                    <TableCell className="font-medium">{hab.numero}</TableCell>
                    <TableCell className="text-muted-foreground">{hab.piso}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{hab.tipoHabitacion.nombre}</Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">{hab.tipoHabitacion.capacidad}</TableCell>
                    <TableCell className="text-muted-foreground">{hab.nombreHotel}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <TarifaPopover habitacionId={hab.id} />
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          onClick={() => setEditTarget(hab)}
                          aria-label="Editar"
                          title="Editar"
                        >
                          <Pencil className="size-3" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          onClick={() => setDeleteTarget(hab)}
                          aria-label="Eliminar"
                          title="Eliminar"
                        >
                          <Trash2 className="size-3 text-destructive" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {totalPages > 1 && (
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    text="Anterior"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    aria-disabled={page === 0}
                    className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                  />
                </PaginationItem>
                {Array.from({ length: totalPages }, (_, i) => (
                  <PaginationItem key={i}>
                    <PaginationLink
                      isActive={i === page}
                      onClick={() => setPage(i)}
                      className="cursor-pointer"
                    >
                      {i + 1}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                <PaginationItem>
                  <PaginationNext
                    text="Siguiente"
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    aria-disabled={page >= totalPages - 1}
                    className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </>
      )}

      <HabitacionFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearHabitacion.isPending}
      />

      <HabitacionFormDialog
        open={editTarget !== null}
        onOpenChange={(open) => { if (!open) setEditTarget(null); }}
        habitacion={editTarget}
        onSubmit={handleEdit}
        isLoading={actualizarHabitacion.isPending}
      />

      <DeleteConfirmDialog
        open={deleteTarget !== null}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Eliminar Habitación"
        description={`¿Estás seguro de que deseas eliminar la habitación ${deleteTarget?.numero} del piso ${deleteTarget?.piso}?`}
        onConfirm={handleDelete}
        isLoading={eliminarHabitacion.isPending}
      />
    </div>
  );
}

function TarifaPopover({ habitacionId }: { habitacionId: number }) {
  const [open, setOpen] = useState(false);
  const { data: tarifa, isLoading } = useTarifaVigente(open ? habitacionId : 0);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        render={
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label="Ver tarifa vigente"
            title="Tarifa vigente"
          />
        }
      >
        <DollarSign className="size-3" />
      </PopoverTrigger>
      <PopoverContent className="w-64">
        {isLoading ? (
          <div className="flex justify-center py-2">
            <Spinner />
          </div>
        ) : tarifa ? (
          <div className="space-y-1">
            <p className="text-xs font-medium text-muted-foreground">Tarifa Vigente</p>
            <p className="text-lg font-bold text-foreground">
              ${tarifa.precioNoche.toLocaleString("es-AR")} <span className="text-xs font-normal text-muted-foreground">/ noche</span>
            </p>
            <p className="text-xs text-muted-foreground">
              Válida: {tarifa.fechaInicio} — {tarifa.fechaFin}
            </p>
            <p className="text-xs text-muted-foreground">
              Tipo: {tarifa.tipoHabitacion.nombre}
            </p>
          </div>
        ) : (
          <p className="text-xs text-muted-foreground">No hay tarifa vigente</p>
        )}
      </PopoverContent>
    </Popover>
  );
}
