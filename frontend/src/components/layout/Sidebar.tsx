import {
  CalendarCheck,
  Building2,
  Receipt,
  Users,
  Landmark,
} from "lucide-react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SidebarItem } from "./SidebarItem";

const navigation = [
  { to: "/", icon: CalendarCheck, label: "Reservas" },
  { to: "/hoteles", icon: Building2, label: "Hoteles & Habitaciones" },
  { to: "/tarifas", icon: Receipt, label: "Tarifas" },
  { to: "/usuarios", icon: Users, label: "Propietarios & Huéspedes" },
  { to: "/bancos", icon: Landmark, label: "Bancos" },
];

interface SidebarProps {
  collapsed?: boolean;
}

export function Sidebar({ collapsed }: SidebarProps) {
  return (
    <aside
      className={`flex h-full flex-col border-r border-border bg-card transition-[width] duration-200 ease-in-out ${
        collapsed ? "w-16" : "w-60"
      }`}
    >
      <div className={`flex items-center gap-3 px-4 py-5 ${collapsed ? "justify-center" : ""}`}>
        <div className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <Building2 className="size-5" />
        </div>
        {!collapsed && (
          <div className="flex flex-col">
            <span className="text-sm font-bold leading-tight text-foreground">
              DAN Hotel
            </span>
            <span className="text-xs text-muted-foreground">
              SaaS Management
            </span>
          </div>
        )}
      </div>

      <Separator />

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navigation.map((item) => (
          <SidebarItem key={item.to} {...item} collapsed={collapsed} />
        ))}
      </nav>

      <Separator />

      <div className="px-3 py-4">
        <DropdownMenu>
          <DropdownMenuTrigger
            aria-label="Menú de usuario"
            className={`flex w-full items-center rounded-lg px-3 py-2.5 text-sm font-medium text-muted-foreground outline-none transition-colors hover:bg-muted hover:text-foreground data-open:bg-muted data-open:text-foreground ${
              collapsed ? "justify-center" : "gap-3"
            }`}
          >
            <Avatar className="size-9 shrink-0">
              <AvatarFallback className="bg-secondary text-xs font-medium text-secondary-foreground">
                AD
              </AvatarFallback>
            </Avatar>
            {!collapsed && (
              <div className="flex min-w-0 flex-col items-start">
                <span className="truncate text-sm font-medium text-foreground">
                  Admin User
                </span>
                <span className="truncate text-xs text-muted-foreground">
                  admin@danhotel.com
                </span>
              </div>
            )}
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" side="top" className="w-56">
            <DropdownMenuItem>Mi Perfil</DropdownMenuItem>
            <DropdownMenuItem>Configuración</DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem>Cerrar Sesión</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </aside>
  );
}
