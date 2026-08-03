import { Fragment, useState, useMemo } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
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
import { Pencil, ChevronDown, ChevronUp, Building2, AlertTriangle } from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useBuscarHoteles, useCrearHotel, useActualizarHotel, useCerrarHotel } from "@/hooks/useHoteles";
import { HotelFormDialog } from "./HotelFormDialog";
import { AmenitiesManager } from "./AmenitiesManager";
import { StarRating } from "./StarRating";
import type { Hotel } from "@/types/hotel";
import type { Amenity } from "@/types/hotel";
import { AMENITY_LABELS } from "@/types/hotel";
import type { HotelCreateFormValues, HotelUpdateFormValues } from "@/lib/validators/hotel";

const AMENITY_FILTER_KEYS: Amenity[] = [
  "WIFI", "PILETA", "GIMNASIO", "RESTAURANTE", "SPA",
  "ESTACIONAMIENTO", "BAR", "PISCINA_CUBIERTA", "PISCINA_DESCUBIERTA",
];
const AMENITY_FILTER_OPTIONS = AMENITY_FILTER_KEYS.map((value) => ({
  value,
  label: AMENITY_LABELS[value],
}));

export function HotelesTab() {
  const [page, setPage] = useState(0);
  const [nombreFilter, setNombreFilter] = useState("");
  const [categoriaFilter, setCategoriaFilter] = useState<string>("");
  const [amenityFilter, setAmenityFilter] = useState<string>("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Hotel | null>(null);
  const [cerrarTarget, setCerrarTarget] = useState<Hotel | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const params = useMemo(() => ({
    page,
    size: 10,
    ...(nombreFilter ? { nombre: nombreFilter } : {}),
    ...(categoriaFilter ? { categoria: Number(categoriaFilter) } : {}),
    ...(amenityFilter ? { amenity: amenityFilter as Amenity } : {}),
  }), [page, nombreFilter, categoriaFilter, amenityFilter]);

  const { data: result, isLoading } = useBuscarHoteles(params);

  const crearHotel = useCrearHotel();
  const actualizarHotel = useActualizarHotel();
  const cerrarHotel = useCerrarHotel();

  const hoteles = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  const handleCreate = (data: HotelCreateFormValues | HotelUpdateFormValues) => {
    crearHotel.mutate(data as HotelCreateFormValues, {
      onSuccess: () => setCreateOpen(false),
    });
  };

  const handleEdit = (data: HotelCreateFormValues | HotelUpdateFormValues) => {
    if (!editTarget) return;
    actualizarHotel.mutate(
      { id: editTarget.id, data: data as HotelUpdateFormValues },
      { onSuccess: () => setEditTarget(null) }
    );
  };

  const handleCerrar = () => {
    if (!cerrarTarget) return;
    cerrarHotel.mutate(cerrarTarget.id, {
      onSuccess: () => setCerrarTarget(null),
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex flex-1 items-center gap-2">
          <Input
            placeholder="Buscar por nombre..."
            value={nombreFilter}
            onChange={(e) => { setNombreFilter(e.target.value); setPage(0); }}
            className="max-w-xs"
          />
          <Select
            value={categoriaFilter}
            onValueChange={(val) => { setCategoriaFilter(val === "all" ? "" : (val ?? "")); setPage(0); }}
          >
            <SelectTrigger className="w-[140px]">
              <SelectValue placeholder="Categoría" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todas</SelectItem>
              {[1, 2, 3, 4, 5].map((c) => (
                <SelectItem key={c} value={c.toString()}>
                  {c} {c === 1 ? "estrella" : "estrellas"}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select
            value={amenityFilter}
            onValueChange={(val) => { setAmenityFilter(val === "all" ? "" : (val ?? "")); setPage(0); }}
          >
            <SelectTrigger className="w-[160px]">
              <SelectValue placeholder="Amenity" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todas</SelectItem>
              {AMENITY_FILTER_OPTIONS.map((a) => (
                <SelectItem key={a.value} value={a.value}>
                  {a.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <Button onClick={() => setCreateOpen(true)}>Nuevo Hotel</Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : hoteles.length === 0 ? (
        <div className="py-12 text-center">
          <Building2 className="mx-auto mb-3 size-10 text-muted-foreground/40" />
          <p className="text-sm text-muted-foreground">No se encontraron hoteles</p>
        </div>
      ) : (
        <>
          <div className="rounded-xl border border-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nombre</TableHead>
                  <TableHead>CUIT</TableHead>
                  <TableHead>Domicilio</TableHead>
                  <TableHead>Cat.</TableHead>
                  <TableHead>Teléfono</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead className="w-[120px]">Acciones</TableHead>
                  <TableHead className="w-[40px]"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {hoteles.map((h) => (
                  <Fragment key={h.id}>
                    <TableRow>
                      <TableCell className="font-medium">{h.nombre}</TableCell>
                      <TableCell className="text-muted-foreground">{h.cuit}</TableCell>
                      <TableCell className="text-muted-foreground">{h.domicilio}</TableCell>
                      <TableCell>
                        <StarRating value={h.categoria} />
                      </TableCell>
                      <TableCell className="text-muted-foreground">{h.telefono}</TableCell>
                      <TableCell className="text-muted-foreground">{h.correoContacto}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          <Button
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setEditTarget(h)}
                            aria-label="Editar"
                            title="Editar"
                          >
                            <Pencil className="size-3" />
                          </Button>
                          {!h.fechaCierre && (
                            <Button
                              variant="ghost"
                              size="icon-sm"
                              onClick={() => setCerrarTarget(h)}
                              aria-label="Cerrar hotel"
                              title="Cerrar hotel"
                            >
                              <AlertTriangle className="size-3 text-yellow-500" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          onClick={() =>
                            setExpandedId(expandedId === h.id ? null : h.id)
                          }
                          aria-label="Ver amenities"
                          title="Amenities"
                        >
                          {expandedId === h.id ? (
                            <ChevronUp className="size-3" />
                          ) : (
                            <ChevronDown className="size-3" />
                          )}
                        </Button>
                      </TableCell>
                    </TableRow>
                    {expandedId === h.id && (
                      <TableRow key={`${h.id}-amenities`}>
                        <TableCell colSpan={8} className="bg-muted/30 p-4">
                          <div className="space-y-2">
                            <p className="text-xs font-medium text-muted-foreground">Amenities del hotel</p>
                            <AmenitiesManager hotelId={h.id} amenities={h.amenities} />
                          </div>
                        </TableCell>
                      </TableRow>
                    )}
                  </Fragment>
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

      <HotelFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSubmit={handleCreate}
        isLoading={crearHotel.isPending}
      />

      <HotelFormDialog
        open={editTarget !== null}
        onOpenChange={(open) => { if (!open) setEditTarget(null); }}
        hotel={editTarget}
        onSubmit={handleEdit}
        isLoading={actualizarHotel.isPending}
      />

      <AlertDialog open={cerrarTarget !== null} onOpenChange={(open) => { if (!open) setCerrarTarget(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cerrar Hotel</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas cerrar <strong>{cerrarTarget?.nombre}</strong>?
              Esta acción es irreversible y todas las habitaciones quedarán no disponibles.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={cerrarHotel.isPending}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCerrar}
              disabled={cerrarHotel.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {cerrarHotel.isPending ? "Cerrando..." : "Cerrar Hotel"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
