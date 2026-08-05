import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Plus, X } from "lucide-react";
import { AMENITY_LABELS } from "@/types/hotel";
import type { Amenity } from "@/types/hotel";

interface AmenityFilterProps {
  selected: Set<Amenity>;
  onChange: (next: Set<Amenity>) => void;
}

const ALL_AMENITIES: Amenity[] = Object.keys(AMENITY_LABELS) as Amenity[];

export function AmenityFilter({ selected, onChange }: AmenityFilterProps) {
  const [open, setOpen] = useState(false);

  const remove = (amenity: Amenity) => {
    const next = new Set(selected);
    next.delete(amenity);
    onChange(next);
  };

  const toggle = (amenity: Amenity) => {
    const next = new Set(selected);
    if (next.has(amenity)) {
      next.delete(amenity);
    } else {
      next.add(amenity);
    }
    onChange(next);
  };

  return (
    <div className="flex flex-wrap items-center gap-1.5">
      {Array.from(selected).map((amenity) => (
        <Badge key={amenity} variant="secondary" className="gap-1 pr-1">
          {AMENITY_LABELS[amenity] ?? amenity}
          <button
            type="button"
            onClick={() => remove(amenity)}
            className="ml-0.5 rounded-full p-0.5 hover:bg-muted-foreground/20"
            aria-label={`Quitar ${AMENITY_LABELS[amenity]}`}
          >
            <X className="size-3" />
          </button>
        </Badge>
      ))}

      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger
          render={
            <Badge
              variant="outline"
              className="cursor-pointer gap-1 hover:bg-muted"
            />
          }
        >
          <Plus className="size-3" />
          Amenidades
        </PopoverTrigger>
        <PopoverContent className="w-72 p-3">
          <div className="space-y-2">
            <p className="text-xs font-medium text-muted-foreground">
              Seleccioná las aménities:
            </p>
            <div className="max-h-48 space-y-1 overflow-y-auto">
              {ALL_AMENITIES.map((amenity) => (
                <label
                  key={amenity}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-1 py-1 text-sm hover:bg-muted"
                >
                  <Checkbox
                    checked={selected.has(amenity)}
                    onCheckedChange={() => toggle(amenity)}
                  />
                  {AMENITY_LABELS[amenity]}
                </label>
              ))}
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}