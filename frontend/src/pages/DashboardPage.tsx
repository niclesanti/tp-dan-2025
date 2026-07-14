import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { MetricCard } from "@/components/dashboard/MetricCard";
import { ActivityItem } from "@/components/dashboard/ActivityItem";
import { SystemStatus } from "@/components/dashboard/SystemStatus";
import { dashboardMetrics, recentActivity } from "@/components/dashboard/mock-data";

export function DashboardPage() {
  return (
    <div className="space-y-6">
      {/* Metric Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {dashboardMetrics.map((metric) => (
          <MetricCard key={metric.title} {...metric} />
        ))}
      </div>

      {/* Content Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Recent Activity */}
        <Card className="border border-border lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-lg font-semibold text-foreground">
              Actividad Reciente
            </CardTitle>
            <Button variant="ghost" size="sm">
              Ver todo
            </Button>
          </CardHeader>
          <CardContent className="divide-y divide-border">
            {recentActivity.map((activity) => (
              <ActivityItem
                key={activity.id}
                icon={activity.icon}
                iconColor={activity.iconColor}
                title={activity.title}
                subtitle={activity.subtitle}
              />
            ))}
          </CardContent>
        </Card>

        {/* System Status */}
        <SystemStatus />
      </div>
    </div>
  );
}
