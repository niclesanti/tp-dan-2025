import { NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface SidebarItemProps {
  to: string;
  icon: LucideIcon;
  label: string;
  collapsed?: boolean;
}

export function SidebarItem({ to, icon: Icon, label, collapsed }: SidebarItemProps) {
  return (
    <NavLink
      to={to}
      end={to === "/"}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
          "hover:bg-muted hover:text-foreground",
          isActive
            ? "bg-muted text-foreground"
            : "text-muted-foreground",
          collapsed && "justify-center px-2"
        )
      }
    >
      <Icon className="size-5 shrink-0" />
      {!collapsed && <span>{label}</span>}
    </NavLink>
  );
}
