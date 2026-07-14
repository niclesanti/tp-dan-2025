import {
  LogIn,
  CalendarPlus,
  CreditCard,
  Sparkles,
  CalendarCheck,
  Building2,
  Receipt,
  Users,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

export interface Activity {
  id: number;
  icon: LucideIcon;
  iconColor: string;
  title: string;
  subtitle: string;
}

export const recentActivity: Activity[] = [
  {
    id: 1,
    icon: LogIn,
    iconColor: "text-emerald-500",
    title: "Check-in realizado en Habitación 204",
    subtitle: "Hace 10 min • Hotel Central",
  },
  {
    id: 2,
    icon: CalendarPlus,
    iconColor: "text-blue-500",
    title: "Nueva reserva: Juan Pérez (Hab. 301)",
    subtitle: "Hace 45 min • Booking.com",
  },
  {
    id: 3,
    icon: CreditCard,
    iconColor: "text-violet-500",
    title: "Pago confirmado: Reserva #10492",
    subtitle: "Hace 2 horas • Tarjeta de Crédito",
  },
  {
    id: 4,
    icon: Sparkles,
    iconColor: "text-amber-500",
    title: "Limpieza finalizada: Habitación 102",
    subtitle: "Hace 3 horas • Equipo B",
  },
];

export interface MetricData {
  title: string;
  value: string;
  change?: string;
  changeType?: "positive" | "neutral" | "negative";
  icon: LucideIcon;
}

export const dashboardMetrics: MetricData[] = [
  {
    title: "Total Reservas",
    value: "1,284",
    change: "+12%",
    changeType: "positive",
    icon: CalendarCheck,
  },
  {
    title: "Ocupación Hoy",
    value: "88%",
    change: "+5%",
    changeType: "positive",
    icon: Building2,
  },
  {
    title: "Ingresos Mensuales",
    value: "$45,200",
    change: "+8%",
    changeType: "positive",
    icon: Receipt,
  },
  {
    title: "Hoteles Activos",
    value: "12",
    change: "Estable",
    changeType: "neutral",
    icon: Users,
  },
];
