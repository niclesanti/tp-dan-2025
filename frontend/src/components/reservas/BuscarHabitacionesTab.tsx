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
import { BedDouble, Star, MapPin } from "lucide-react";
import { useBuscarHabitacionesDisponibles } from "@/hooks/useReservas";
import { CrearReservaDialog } from "./CrearReservaDialog";
import type { HabitacionDisponibleDTO } from "@/types/reserva";

export function BuscarHabitacionesTab() {
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [capacidadFilter, setCapacidadFilter] = useState("");
  const [precioMinFilter, setPrecioMinFilter] = useState("");
  const [precioMaxFilter, setPrecioMaxFilter] = useState("");
  const [categoriaFilter, setCategoriaFilter] = useState<string>("");
  const [page, setPage] = useState(0);
  const [hasSearched, setHasSearched] = useState(false);
  const [reservarTarget, setReservarTarget] = useState<HabitacionDisponibleDTO | null>(null);

  const params = useMemo(() => {
    // Convert date strings (YYYY-MM-DD) to ISO date-time format for backend
    const formatToISODateTime = (dateStr: string) => {
      if (!dateStr) return dateStr;
      // If already contains time component, return as-is
      if (dateStr.includes("T")) return dateStr;
      // Add time component for ISO date-time format
      return `${dateStr}T00:00:00.000Z`;
    };

    return {
      checkIn: formatToISODateTime(checkIn),
      checkOut: formatToISODateTime(checkOut),
      page,
      size: 10,
      ...(capacidadFilter ? { capacidad: Number(capacidadFilter) } : {}),
      ...(precioMinFilter ? { precioMin: Number(precioMinFilter) } : {}),
      ...(precioMaxFilter ? { precioMax: Number(precioMaxFilter) } : {}),
      ...(categoriaFilter ? { categoriaHotel: Number(categoriaFilter) } : {}),
    };
  }, [checkIn, checkOut, page, capacidadFilter, precioMinFilter, precioMaxFilter, categoriaFilter]);

  const searchEnabled = hasSearched && !!checkIn && !!checkOut && checkIn < checkOut;
  const { data: result, isLoading } = useBuscarHabitacionesDisponibles(params, searchEnabled);

  const habitaciones = result?.content ?? [];
  const totalPages = result?.totalPages ?? 0;

  const handleSearch = () => {
    if (!checkIn || !checkOut) return;
    setHasSearched(true);
    setPage(0);
  };

  const canSearch = checkIn && checkOut && checkIn < checkOut;

  return (
    <div className="space-y-4">
      {/* Filtros de búsqueda */}
      <div className="rounded-xl border border-border p-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Check-in *
            </label>
            <Input
              type="date"
              value={checkIn}
              onChange={(e) => setCheckIn(e.target.value)}
              min={new Date().toISOString().split("T")[0]}
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Check-out *
            </label>
            <Input
              type="date"
              value={checkOut}
              onChange={(e) => setCheckOut(e.target.value)}
              min={checkIn || new Date().toISOString().split("T")[0]}
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Capacidad mín.
            </label>
            <Input
              type="number"
              placeholder="Ej: 2"
              min={1}
              value={capacidadFilter}
              onChange={(e) => setCapacidadFilter(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Categoría hotel
            </label>
            <Select
              value={categoriaFilter}
              onValueChange={(val) =>
                setCategoriaFilter(val === "all" ? "" : (val ?? ""))
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Cualquiera" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Cualquiera</SelectItem>
                {[1, 2, 3, 4, 5].map((c) => (
                  <SelectItem key={c} value={c.toString()}>
                    {c} {c === 1 ? "estrella" : "estrellas"}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Precio mínimo
            </label>
            <Input
              type="number"
              placeholder="USD"
              min={0}
              value={precioMinFilter}
              onChange={(e) => setPrecioMinFilter(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">
              Precio máximo
            </label>
            <Input
              type="number"
              placeholder="USD"
              min={0}
              value={precioMaxFilter}
              onChange={(e) => setPrecioMaxFilter(e.target.value)}
            />
          </div>
          <div className="flex items-end">
            <Button
              onClick={handleSearch}
              disabled={!canSearch}
              className="w-full"
            >
              Buscar Habitaciones
            </Button>
          </div>
        </div>
      </div>

      {/* Resultados */}
      {!hasSearched ? (
        <div className="py-16 text-center">
          <BedDouble className="mx-auto mb-3 size-12 text-muted-foreground/30" />
          <p className="text-sm text-muted-foreground">
            Seleccioná las fechas y hacé clic en "Buscar Habitaciones" para ver la disponibilidad
          </p>
        </div>
      ) : isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      ) : habitaciones.length === 0 ? (
        <div className="py-12 text-center">
          <BedDouble className="mx-auto mb-3 size-10 text-muted-foreground/40" />
          <p className="text-sm text-muted-foreground">
            No se encontraron habitaciones disponibles para las fechas seleccionadas
          </p>
        </div>
      ) : (
        <>
          <div className="rounded-xl border border-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Hotel</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Cap.</TableHead>
                  <TableHead>Cat.</TableHead>
                  <TableHead>Precio / noche</TableHead>
                  <TableHead className="w-[100px]">Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {habitaciones.map((hab) => (
                  <TableRow key={hab.id}>
                    <TableCell>
                      <div className="flex flex-col">
                        <span className="font-medium">{hab.hotel.nombre}</span>
                        <span className="flex items-center gap-1 text-xs text-muted-foreground">
                          <MapPin className="size-3" />
                          {hab.hotel.domicilio}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{hab.tipoHabitacion}</Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {hab.capacidad}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-0.5">
                        {Array.from({ length: hab.hotel.categoria }, (_, i) => (
                          <Star
                            key={i}
                            className="size-3 fill-yellow-500 text-yellow-500"
                          />
                        ))}
                      </div>
                    </TableCell>
                    <TableCell className="font-medium">
                      USD {hab.precioNoche.toLocaleString("es-AR")}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setReservarTarget(hab)}
                      >
                        Reservar
                      </Button>
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
                    className={
                      page === 0
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
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
                    onClick={() =>
                      setPage((p) => Math.min(totalPages - 1, p + 1))
                    }
                    aria-disabled={page >= totalPages - 1}
                    className={
                      page >= totalPages - 1
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          )}
        </>
      )}

      <CrearReservaDialog
        open={reservarTarget !== null}
        onOpenChange={(open) => {
          if (!open) setReservarTarget(null);
        }}
        habitacion={reservarTarget}
        checkInDefault={checkIn}
        checkOutDefault={checkOut}
      />
    </div>
  );
}
