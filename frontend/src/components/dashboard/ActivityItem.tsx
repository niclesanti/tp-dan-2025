import type { LucideIcon } from "lucide-react";

interface ActivityItemProps {
  icon: LucideIcon;
  iconColor?: string;
  title: string;
  subtitle: string;
}

export function ActivityItem({ icon: Icon, iconColor = "text-muted-foreground", title, subtitle }: ActivityItemProps) {
  return (
    <div className="flex items-start gap-3 py-3">
      <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-muted">
        <Icon className={`size-4 ${iconColor}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-foreground truncate">{title}</p>
        <p className="text-xs text-muted-foreground">{subtitle}</p>
      </div>
    </div>
  );
}
