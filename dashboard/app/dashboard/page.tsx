"use client";

import { StatsCards } from "@/components/dashboard/stats-cards";
import { RegionChart } from "@/components/dashboard/region-chart";
import { AnomalyList } from "@/components/dashboard/anomaly-list";
import { WelcomeBanner } from "@/components/dashboard/welcome-banner";
import { EventPipeline } from "@/components/dashboard/event-pipeline";
import { RecentOrders } from "@/components/dashboard/recent-orders";
import { ChurnPreview } from "@/components/dashboard/churn-preview";

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <WelcomeBanner />
      <StatsCards />
      <EventPipeline />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <RegionChart />
        </div>
        <div>
          <AnomalyList />
        </div>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <RecentOrders />
        </div>
        <div>
          <ChurnPreview />
        </div>
      </div>
    </div>
  );
}
