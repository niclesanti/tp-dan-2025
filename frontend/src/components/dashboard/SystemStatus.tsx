import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

interface SystemMetric {
  label: string;
  value: string;
}

interface SystemStatusProps {
  metrics?: SystemMetric[];
  isOnline?: boolean;
}

const defaultMetrics: SystemMetric[] = [
  { label: "Uptime Global", value: "99.9%" },
  { label: "Latencia del Servidor", value: "42ms" },
];

export function SystemStatus({ metrics = defaultMetrics, isOnline = true }: SystemStatusProps) {
  return (
    <Card className="border border-border">
      <CardHeader className="flex flex-row items-center justify-between pb-4">
        <CardTitle className="text-lg font-semibold text-foreground">
          Estado del Sistema
        </CardTitle>
        <Badge variant="outline" className={`rounded-full text-xs ${isOnline ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-500" : "border-red-500/30 bg-red-500/10 text-red-500"}`}>
          <span className={`mr-1.5 size-1.5 rounded-full ${isOnline ? "bg-emerald-500" : "bg-red-500"}`} />
          {isOnline ? "En línea" : "Offline"}
        </Badge>
      </CardHeader>
      <CardContent className="space-y-4">
        {metrics.map((metric) => (
          <div key={metric.label} className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">{metric.label}</span>
            <span className="text-sm font-semibold text-foreground">{metric.value}</span>
          </div>
        ))}
        <Button variant="outline" className="w-full mt-4">
          Ver Métricas Detalladas
        </Button>
      </CardContent>
    </Card>
  );
}
