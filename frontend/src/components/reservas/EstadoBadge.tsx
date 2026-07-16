import { Badge } from "@/components/ui/badge";
import type { EstadoReserva } from "@/types/reserva";
import { ESTADO_RESERVA_LABELS } from "@/types/reserva";

const ESTADO_VARIANT_MAP: Record<EstadoReserva, "default" | "secondary" | "destructive" | "outline"> = {
  RESERVADA: "secondary",
  CONFIRMADA: "outline",
  EFECTUADA: "outline",
  FINALIZADA: "default",
  CANCELADA: "destructive",
  BLOQUEADA: "secondary",
  CERRADA: "secondary",
  ADEUDADA: "secondary",
};

const ESTADO_COLOR_MAP: Record<EstadoReserva, string> = {
  RESERVADA: "",
  CONFIRMADA: "border-blue-500/50 text-blue-400",
  EFECTUADA: "border-emerald-500/50 text-emerald-400",
  FINALIZADA: "",
  CANCELADA: "",
  BLOQUEADA: "",
  CERRADA: "",
  ADEUDADA: "border-yellow-500/50 text-yellow-400",
};

interface EstadoBadgeProps {
  estado: EstadoReserva;
}

export function EstadoBadge({ estado }: EstadoBadgeProps) {
  const variant = ESTADO_VARIANT_MAP[estado] ?? "secondary";
  const colorClass = ESTADO_COLOR_MAP[estado] ?? "";

  return (
    <Badge variant={variant} className={colorClass}>
      {ESTADO_RESERVA_LABELS[estado] ?? estado}
    </Badge>
  );
}
