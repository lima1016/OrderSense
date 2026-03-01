"use client";

import useSWR from "swr";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from "recharts";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { AnalyticsStats } from "@/lib/types";

function CustomTooltip({ active, payload, label }: { active?: boolean; payload?: Array<{ name: string; value: number; color: string }>; label?: string }) {
  if (!active || !payload) return null;
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-lg shadow-slate-200/50">
      <p className="text-[13px] font-semibold text-slate-900 mb-1.5">{label}</p>
      {payload.map((entry) => (
        <div key={entry.name} className="flex items-center gap-2 text-[12px]">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
          <span className="text-slate-500">{entry.name}</span>
          <span className="font-semibold text-slate-900 ml-auto">{entry.value.toLocaleString()}</span>
        </div>
      ))}
    </div>
  );
}

export function RegionChart() {
  const { data: stats } = useSWR<AnalyticsStats>(apiUrls.analyticsStats, fetcher, {
    refreshInterval: 15000,
  });

  const chartData = stats
    ? Object.keys(stats.currentOrdersByRegion).map((region) => ({
        region,
        orders: stats.currentOrdersByRegion[region] || 0,
        riders: stats.availableRidersByRegion[region] || 0,
        cancels: stats.cancelsByRegion[region] || 0,
      }))
    : [];

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Region Overview</CardTitle>
          <span className="text-[11px] text-slate-400 font-medium">Real-time</span>
        </div>
      </CardHeader>
      <CardContent>
        {chartData.length === 0 ? (
          <div className="h-64 flex flex-col items-center justify-center gap-2">
            <div className="w-8 h-8 rounded-full bg-slate-100 animate-pulse" />
            <p className="text-sm text-slate-400">데이터 수집 중...</p>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} barCategoryGap="20%">
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis
                dataKey="region"
                tick={{ fontSize: 12, fill: "#64748b" }}
                axisLine={{ stroke: "#e2e8f0" }}
                tickLine={false}
              />
              <YAxis
                tick={{ fontSize: 12, fill: "#64748b" }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} cursor={{ fill: "#f1f5f9" }} />
              <Legend
                iconType="circle"
                iconSize={8}
                wrapperStyle={{ fontSize: 12, color: "#64748b" }}
              />
              <Bar dataKey="orders" name="Orders" fill="#3b82f6" radius={[6, 6, 0, 0]} />
              <Bar dataKey="riders" name="Riders" fill="#10b981" radius={[6, 6, 0, 0]} />
              <Bar dataKey="cancels" name="Cancels" fill="#f43f5e" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
