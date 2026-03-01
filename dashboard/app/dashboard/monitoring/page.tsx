"use client";

import { ServiceHealth } from "@/components/monitoring/service-health";
import { ActionLogs } from "@/components/monitoring/action-logs";

export default function MonitoringPage() {
  return (
    <div className="space-y-6">
      <ServiceHealth />
      <ActionLogs />
    </div>
  );
}
