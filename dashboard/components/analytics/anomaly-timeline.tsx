"use client";

import useSWR from "swr";
import {
  ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
} from "recharts";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import { formatAnomalyType } from "@/lib/utils";
import type { AnomaliesResponse } from "@/lib/types";

const TYPE_COLORS: Record<string, string> = {
  ORDER_DROP: "#f43f5e",
  DELIVERY_DELAY: "#f59e0b",
  CUSTOMER_CHURN: "#8b5cf6",
  REJECTION_SPIKE: "#f97316",
};

function CustomTooltip({ active, payload }: { active?: boolean; payload?: Array<{ payload: { type: string; severity: number; description: string; region: string; time: number } }> }) {
  if (!active || !payload?.[0]) return null;
  const d = payload[0].payload;
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-lg shadow-slate-200/50 max-w-[240px]">
      <div className="flex items-center gap-2 mb-1.5">
        <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: TYPE_COLORS[d.type] || "#94a3b8" }} />
        <span className="text-[13px] font-semibold text-slate-900">{formatAnomalyType(d.type)}</span>
      </div>
      <p className="text-[11px] text-slate-500 mb-1">{d.description}</p>
      <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-100">
        <span>Severity: {d.severity.toFixed(2)}</span>
        <span>{d.region || "—"}</span>
      </div>
    </div>
  );
}

export function AnomalyTimeline() {
  const { data } = useSWR<AnomaliesResponse>(apiUrls.anomalies(50), fetcher, {
    refreshInterval: 15000,
  });

  const anomalies = data?.anomalies ?? [];

  const chartData = anomalies.map((a) => ({
    time: new Date(a.detectedAt).getTime(),
    severity: a.severity,
    type: a.anomalyType,
    description: a.description,
    region: a.region,
  }));

  const types = [...new Set(anomalies.map((a) => a.anomalyType))];

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>Anomaly Timeline</CardTitle>
      </CardHeader>
      <CardContent>
        {chartData.length === 0 ? (
          <div className="h-64 flex flex-col items-center justify-center gap-2">
            <div className="w-8 h-8 rounded-full bg-slate-100 animate-pulse" />
            <p className="text-sm text-slate-400">이상 탐지 이력 없음</p>
          </div>
        ) : (
          <>
            <ResponsiveContainer width="100%" height={250}>
              <ScatterChart>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis
                  dataKey="time"
                  type="number"
                  domain={["dataMin", "dataMax"]}
                  tickFormatter={(v) =>
                    new Date(v).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })
                  }
                  tick={{ fontSize: 11, fill: "#64748b" }}
                  axisLine={{ stroke: "#e2e8f0" }}
                  tickLine={false}
                />
                <YAxis
                  dataKey="severity"
                  domain={[0, 1]}
                  tick={{ fontSize: 11, fill: "#64748b" }}
                  axisLine={false}
                  tickLine={false}
                  label={{ value: "Severity", angle: -90, position: "insideLeft", fontSize: 11, fill: "#94a3b8" }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Scatter data={chartData}>
                  {chartData.map((d, i) => (
                    <Cell key={i} fill={TYPE_COLORS[d.type] || "#94a3b8"} />
                  ))}
                </Scatter>
              </ScatterChart>
            </ResponsiveContainer>
            <div className="flex flex-wrap gap-3 mt-3 pt-3 border-t border-slate-100">
              {types.map((t) => (
                <div key={t} className="flex items-center gap-1.5 text-[11px] text-slate-500">
                  <div
                    className="w-2 h-2 rounded-full"
                    style={{ backgroundColor: TYPE_COLORS[t] || "#94a3b8" }}
                  />
                  {formatAnomalyType(t)}
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
