import { ChevronRight, Bell, PanelLeftClose, PanelLeftOpen, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useLocation } from "react-router-dom";

const breadcrumbLabels: Record<string, string> = {
  "/": "Reservas",
  "/hoteles": "Hoteles & Habitaciones",
  "/tarifas": "Tarifas",
  "/usuarios": "Propietarios & Huéspedes",
  "/bancos": "Bancos",
};

interface HeaderProps {
  collapsed?: boolean;
  onToggle?: () => void;
  onMobileOpen?: () => void;
}

export function Header({ collapsed, onToggle, onMobileOpen }: HeaderProps) {
  const location = useLocation();
  const segment = "/" + (location.pathname.split("/")[1] || "");
  const currentLabel = breadcrumbLabels[segment] ?? "Reservas";

  return (
    <header className="flex h-14 items-center gap-2 border-b border-border bg-card px-4 md:px-6">
      {/* Mobile menu trigger */}
      <Button
        variant="ghost"
        size="icon"
        aria-label="Abrir menú de navegación"
        className="shrink-0 md:hidden"
        onClick={onMobileOpen}
      >
        <Menu className="size-5" />
      </Button>

      {/* Desktop collapse toggle */}
      <Button
        variant="ghost"
        size="icon"
        aria-label={collapsed ? "Expandir menú" : "Colapsar menú"}
        className="hidden shrink-0 md:flex"
        onClick={onToggle}
      >
        {collapsed ? (
          <PanelLeftOpen className="size-5" />
        ) : (
          <PanelLeftClose className="size-5" />
        )}
      </Button>

      {/* Breadcrumb */}
      <nav className="flex items-center gap-1.5 text-sm text-muted-foreground">
        <span>App</span>
        <ChevronRight className="size-3.5" />
        <span className="text-foreground">{currentLabel}</span>
      </nav>

      <div className="flex-1" />

      {/* Notifications */}
      <Popover>
        <PopoverTrigger
          render={
            <Button
              variant="ghost"
              size="icon"
              aria-label="Notificaciones"
            />
          }
        >
          <Bell className="size-5" />
        </PopoverTrigger>
        <PopoverContent align="end" className="w-72">
          <div className="space-y-2">
            <h4 className="text-sm font-medium text-foreground">Notificaciones</h4>
            <p className="text-sm text-muted-foreground">Sin notificaciones</p>
          </div>
        </PopoverContent>
      </Popover>
    </header>
  );
}
