import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Spinner } from "@/components/ui/spinner";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Plus, X } from "lucide-react";
import { useAgregarAmenities, useEliminarAmenity } from "@/hooks/useHoteles";
import { AMENITY_LABELS } from "@/types/hotel";
import type { Amenity, AmenityHotel } from "@/types/hotel";

interface AmenitiesManagerProps {
  hotelId: number;
  amenities: AmenityHotel[];
}

const ALL_AMENITIES: Amenity[] = Object.keys(AMENITY_LABELS) as Amenity[];

export function AmenitiesManager({ hotelId, amenities }: AmenitiesManagerProps) {
  const [open, setOpen] = useState(false);
  const agregarAmenities = useAgregarAmenities();
  const eliminarAmenity = useEliminarAmenity();

  const currentAmenityTypes = new Set(amenities.map((a) => a.amenity));
  const availableToAdd = ALL_AMENITIES.filter((a) => !currentAmenityTypes.has(a));

  const handleRemove = (amenityId: number) => {
    eliminarAmenity.mutate({ id: hotelId, amenityId });
  };

  const handleAddSelected = (selected: Amenity[]) => {
    if (selected.length === 0) return;
    agregarAmenities.mutate(
      { id: hotelId, amenities: selected },
      { onSuccess: () => setOpen(false) }
    );
  };

  return (
    <div className="flex flex-wrap items-center gap-1.5">
      {amenities.map((a) => (
        <Badge
          key={a.id}
          variant="secondary"
          className="gap-1 pr-1"
        >
          {AMENITY_LABELS[a.amenity] ?? a.amenity}
          <button
            type="button"
            onClick={() => handleRemove(a.id)}
            disabled={eliminarAmenity.isPending}
            className="ml-0.5 rounded-full p-0.5 hover:bg-muted-foreground/20"
          >
            <X className="size-3" />
          </button>
        </Badge>
      ))}

      {availableToAdd.length > 0 && (
        <AddAmenityPopover
          open={open}
          onOpenChange={setOpen}
          available={availableToAdd}
          onAdd={handleAddSelected}
          isLoading={agregarAmenities.isPending}
        />
      )}
    </div>
  );
}

function AddAmenityPopover({
  open,
  onOpenChange,
  available,
  onAdd,
  isLoading,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  available: Amenity[];
  onAdd: (selected: Amenity[]) => void;
  isLoading: boolean;
}) {
  const [selected, setSelected] = useState<Set<Amenity>>(new Set());

  const toggle = (amenity: Amenity) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(amenity)) {
        next.delete(amenity);
      } else {
        next.add(amenity);
      }
      return next;
    });
  };

  const handleConfirm = () => {
    onAdd(Array.from(selected));
    setSelected(new Set());
  };

  return (
    <Popover open={open} onOpenChange={onOpenChange}>
      <PopoverTrigger
        render={
          <Badge
            variant="outline"
            className="cursor-pointer gap-1 hover:bg-muted"
          />
        }
      >
        <Plus className="size-3" />
        Agregar
      </PopoverTrigger>
      <PopoverContent className="w-72 p-3">
        <div className="space-y-2">
          <p className="text-xs font-medium text-muted-foreground">Seleccioná amenities para agregar:</p>
          <div className="max-h-48 space-y-1 overflow-y-auto">
            {available.map((amenity) => (
              <label
                key={amenity}
                className="flex items-center gap-2 rounded-sm px-1 py-1 text-sm hover:bg-muted cursor-pointer"
              >
                <Checkbox
                  checked={selected.has(amenity)}
                  onCheckedChange={() => toggle(amenity)}
                />
                {AMENITY_LABELS[amenity]}
              </label>
            ))}
          </div>
          <div className="flex justify-end gap-2 pt-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
            >
              Cancelar
            </Button>
            <Button
              size="sm"
              onClick={handleConfirm}
              disabled={selected.size === 0 || isLoading}
            >
              {isLoading && <Spinner className="mr-1 size-3" />}
              Agregar ({selected.size})
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}
