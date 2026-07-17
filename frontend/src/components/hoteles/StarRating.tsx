import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface StarRatingProps {
  value: number;
  max?: number;
  readonly?: boolean;
  onChange?: (value: number) => void;
  className?: string;
}

export function StarRating({
  value,
  max = 5,
  readonly = true,
  onChange,
  className,
}: StarRatingProps) {
  return (
    <div className={cn("flex items-center gap-0.5", className)}>
      {Array.from({ length: max }, (_, i) => (
        <button
          key={i}
          type="button"
          disabled={readonly}
          onClick={() => !readonly && onChange?.(i + 1)}
          aria-label={`${i + 1} estrella${i === 0 ? "" : "s"}`}
          aria-pressed={i < value}
          className={cn(
            "disabled:cursor-default",
            !readonly && "cursor-pointer hover:scale-110 transition-transform"
          )}
        >
          <Star
            className={cn(
              "size-4",
              i < value
                ? "fill-yellow-500 text-yellow-500"
                : "fill-none text-muted-foreground/40"
            )}
          />
        </button>
      ))}
    </div>
  );
}
