"use client";

import useSWR from "swr";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from "recharts";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { DemandResponse } from "@/lib/types";

const TrendIcon = ({ trend }: { trend: string }) => {
  if (trend === "increasing") return <TrendingUp size={12} className="text-emerald-500" />;
  if (trend === "decreasing") return <TrendingDown size={12} className="text-rose-500" />;
  return <Minus size={12} className="text-slate-400" />;
};

function CustomTooltip({ active, payload, label }: { active?: boolean; payload?: Array<{ name: string; value: number; color: string }>; label?: string }) {
  if (!active || !payload) return null;
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-lg shadow-slate-200/50">
      <p className="text-[13px] font-semibold text-slate-900 mb-1.5">{label}</p>
      {payload.map((entry) => (
        <div key={entry.name} className="flex items-center gap-2 text-[12px]">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
          <span className="text-slate-500">{entry.name === "current" ? "Current" : "Predicted"}</span>
          <span className="font-semibold text-slate-900 ml-auto">{entry.value.toLocaleString()}</span>
        </div>
      ))}
    </div>
  );
}

export function DemandChart() {
  const { data } = useSWR<DemandResponse>(apiUrls.demand(), fetcher, {
    refreshInterval: 30000,
  });

  const forecasts = data?.forecasts ?? [];

  const chartData = forecasts.map((f) => ({
    region: f.region,
    current: f.current_hour_orders,
    predicted: f.predicted_next_hour_orders,
    trend: f.trend,
    confidence: Math.round(f.confidence * 100),
  }));

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Demand Forecast</CardTitle>
          <span className="text-[11px] text-slate-400 font-medium">Next hour prediction</span>
        </div>
      </CardHeader>
      <CardContent>
        {chartData.length === 0 ? (
          <div className="h-64 flex flex-col items-center justify-center gap-2">
            <div className="w-8 h-8 rounded-full bg-slate-100 animate-pulse" />
            <p className="text-sm text-slate-400">수요 예측 데이터 수집 중...</p>
          </div>
        ) : (
          <>
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
                  formatter={(v) => (v === "current" ? "Current" : "Predicted")}
                  wrapperStyle={{ fontSize: 12, color: "#64748b" }}
                />
                <Bar dataKey="current" fill="#3b82f6" radius={[6, 6, 0, 0]} />
                <Bar dataKey="predicted" fill="#93c5fd" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
            <div className="mt-4 grid grid-cols-2 sm:grid-cols-3 gap-2">
              {forecasts.map((f) => (
                <div
                  key={f.region}
                  className="flex items-center gap-2.5 p-2.5 rounded-xl bg-slate-50 border border-slate-100"
                >
                  <div className={`w-6 h-6 rounded-lg flex items-center justify-center ${
                    f.trend === "increasing" ? "bg-emerald-50" : f.trend === "decreasing" ? "bg-rose-50" : "bg-slate-100"
                  }`}>
                    <TrendIcon trend={f.trend} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <span className="text-[12px] font-semibold text-slate-900">{f.region}</span>
                  </div>
                  <span className="text-[11px] font-medium text-slate-400">
                    {Math.round(f.confidence * 100)}%
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
