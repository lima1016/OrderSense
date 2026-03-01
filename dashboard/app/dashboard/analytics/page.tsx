"use client";

import { DemandChart } from "@/components/analytics/demand-chart";
import { AnomalyTimeline } from "@/components/analytics/anomaly-timeline";
import { ChurnTable } from "@/components/analytics/churn-table";

export default function AnalyticsPage() {
  return (
    <div className="space-y-6">
      <DemandChart />
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <AnomalyTimeline />
        <ChurnTable />
      </div>
    </div>
  );
}
