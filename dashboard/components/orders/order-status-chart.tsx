"use client";

import useSWR from "swr";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { AnalyticsStats } from "@/lib/types";

const STATUS_COLORS: Record<string, string> = {
  orders: "#3b82f6",
  deliveries: "#8b5cf6",
  riders: "#10b981",
  restaurants: "#f59e0b",
  payments: "#f43f5e",
};

const STATUS_LABELS: Record<string, string> = {
  orders: "Orders",
  deliveries: "Deliveries",
  riders: "Riders",
  restaurants: "Restaurants",
  payments: "Payments",
};

function CustomTooltip({ active, payload }: { active?: boolean; payload?: Array<{ name: string; value: number; payload: { name: string } }> }) {
  if (!active || !payload?.[0]) return null;
  const item = payload[0];
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 shadow-lg shadow-slate-200/50">
      <div className="flex items-center gap-2">
        <div
          className="w-2.5 h-2.5 rounded-full"
          style={{ backgroundColor: STATUS_COLORS[item.payload.name] || "#94a3b8" }}
        />
        <span className="text-[12px] text-slate-500">
          {STATUS_LABELS[item.payload.name] || item.payload.name}
        </span>
        <span className="text-[13px] font-bold text-slate-900 ml-2">
          {item.value.toLocaleString()}
        </span>
      </div>
    </div>
  );
}

export function OrderStatusChart() {
  const { data: stats } = useSWR<AnalyticsStats>(apiUrls.analyticsStats, fetcher, {
    refreshInterval: 30000,
  });

  const chartData = stats
    ? Object.entries(stats.eventCounts).map(([name, value]) => ({
        name,
        value,
      }))
    : [];

  const totalEvents = chartData.reduce((sum, d) => sum + d.value, 0);

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>Event Distribution</CardTitle>
      </CardHeader>
      <CardContent>
        {totalEvents === 0 ? (
          <div className="h-64 flex flex-col items-center justify-center gap-2">
            <div className="w-8 h-8 rounded-full bg-slate-100 animate-pulse" />
            <p className="text-sm text-slate-400">이벤트 수집 중...</p>
          </div>
        ) : (
          <>
            <div className="relative">
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie
                    data={chartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={3}
                    dataKey="value"
                    strokeWidth={0}
                  >
                    {chartData.map((d) => (
                      <Cell key={d.name} fill={STATUS_COLORS[d.name] || "#94a3b8"} />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="text-center">
                  <p className="text-2xl font-bold text-slate-900">{totalEvents.toLocaleString()}</p>
                  <p className="text-[11px] text-slate-400">Total Events</p>
                </div>
              </div>
            </div>
            <div className="space-y-2 mt-2">
              {chartData.map((d) => (
                <div key={d.name} className="flex items-center gap-3 text-[12px]">
                  <div
                    className="w-2 h-2 rounded-full shrink-0"
                    style={{ backgroundColor: STATUS_COLORS[d.name] || "#94a3b8" }}
                  />
                  <span className="text-slate-600 flex-1">{STATUS_LABELS[d.name] || d.name}</span>
                  <span className="font-semibold text-slate-900">{d.value.toLocaleString()}</span>
                  <span className="text-slate-400 w-10 text-right">
                    {totalEvents > 0 ? Math.round((d.value / totalEvents) * 100) : 0}%
                  </span>
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
