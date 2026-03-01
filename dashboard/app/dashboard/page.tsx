"use client";

import { StatsCards } from "@/components/dashboard/stats-cards";
import { RegionChart } from "@/components/dashboard/region-chart";
import { AnomalyList } from "@/components/dashboard/anomaly-list";

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <StatsCards />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <RegionChart />
        </div>
        <div>
          <AnomalyList />
        </div>
      </div>
    </div>
  );
}
