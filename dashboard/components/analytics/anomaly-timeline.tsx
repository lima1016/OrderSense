"use client";

import { useState, useMemo } from "react";
import useSWR from "swr";
import {
  ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine, Legend,
} from "recharts";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import { formatAnomalyType, formatDateForInput, toLocalISOString } from "@/lib/utils";
import type { AnomaliesResponse, AnomalyItem } from "@/lib/types";

const TYPE_COLORS: Record<string, string> = {
  ORDER_DROP: "#f43f5e",
  DELIVERY_DELAY: "#f59e0b",
  CUSTOMER_CHURN: "#8b5cf6",
  REJECTION_SPIKE: "#f97316",
};

const TYPE_BG: Record<string, string> = {
  ORDER_DROP: "bg-rose-50 ring-rose-200",
  DELIVERY_DELAY: "bg-amber-50 ring-amber-200",
  CUSTOMER_CHURN: "bg-violet-50 ring-violet-200",
  REJECTION_SPIKE: "bg-orange-50 ring-orange-200",
};

const TYPE_TEXT: Record<string, string> = {
  ORDER_DROP: "text-rose-600",
  DELIVERY_DELAY: "text-amber-600",
  CUSTOMER_CHURN: "text-violet-600",
  REJECTION_SPIKE: "text-orange-600",
};

/** 시간대별 버킷으로 그룹핑 */
function bucketAnomalies(anomalies: AnomalyItem[], bucketMinutes: number = 60) {
  if (anomalies.length === 0) return [];

  const sorted = [...anomalies].sort(
    (a, b) => new Date(a.detectedAt).getTime() - new Date(b.detectedAt).getTime(),
  );

  const minTime = new Date(sorted[0].detectedAt).getTime();
  const maxTime = new Date(sorted[sorted.length - 1].detectedAt).getTime();
  const bucketMs = bucketMinutes * 60 * 1000;

  // 빈 버킷도 포함해서 연속 타임라인 생성
  const buckets: Record<number, AnomalyItem[]> = {};
  for (let t = Math.floor(minTime / bucketMs) * bucketMs; t <= maxTime + bucketMs; t += bucketMs) {
    buckets[t] = [];
  }

  for (const a of sorted) {
    const t = new Date(a.detectedAt).getTime();
    const key = Math.floor(t / bucketMs) * bucketMs;
    if (!buckets[key]) buckets[key] = [];
    buckets[key].push(a);
  }

  return Object.entries(buckets)
    .map(([timeKey, items]) => {
      const time = Number(timeKey);
      const counts: Record<string, number> = {};
      let totalSeverity = 0;
      let maxSeverity = 0;

      for (const item of items) {
        const type = item.anomalyType;
        counts[type] = (counts[type] || 0) + 1;
        totalSeverity += item.severity;
        maxSeverity = Math.max(maxSeverity, item.severity);
      }

      return {
        time,
        ...counts,
        total: items.length,
        avgSeverity: items.length > 0 ? Math.round((totalSeverity / items.length) * 100) : 0,
        maxSeverity: Math.round(maxSeverity * 100),
      };
    })
    .sort((a, b) => a.time - b.time);
}

function CustomTooltip({ active, payload, label }: {
  active?: boolean;
  payload?: Array<{ dataKey: string; value: number; color: string }>;
  label?: number;
}) {
  if (!active || !payload || !label) return null;

  const time = new Date(label).toLocaleString("ko-KR", {
    month: "short", day: "numeric", hour: "2-digit", minute: "2-digit",
  });

  const barItems = payload.filter(
    (p) => p.dataKey !== "avgSeverity" && p.dataKey !== "maxSeverity" && p.value > 0,
  );
  const avgSev = payload.find((p) => p.dataKey === "avgSeverity");
  const maxSev = payload.find((p) => p.dataKey === "maxSeverity");

  const total = barItems.reduce((s, p) => s + p.value, 0);
  if (total === 0 && !avgSev?.value) return null;

  return (
    <div className="rounded-xl border border-slate-200 bg-white/95 backdrop-blur-sm px-4 py-3 shadow-xl shadow-slate-200/60 min-w-[180px]">
      <p className="text-[11px] font-semibold text-slate-800 mb-2">{time}</p>
      {barItems.map((p) => (
        <div key={p.dataKey} className="flex items-center justify-between gap-4 py-0.5">
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: p.color }} />
            <span className="text-[11px] text-slate-600">{formatAnomalyType(p.dataKey)}</span>
          </div>
          <span className="text-[12px] font-bold text-slate-800">{p.value}건</span>
        </div>
      ))}
      {(avgSev || maxSev) && (
        <div className="mt-2 pt-2 border-t border-slate-100 space-y-0.5">
          {avgSev && avgSev.value > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] text-slate-400">평균 심각도</span>
              <span className={`text-[11px] font-bold ${avgSev.value >= 70 ? "text-rose-600" : avgSev.value >= 40 ? "text-amber-600" : "text-emerald-600"}`}>
                {avgSev.value}%
              </span>
            </div>
          )}
          {maxSev && maxSev.value > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] text-slate-400">최대 심각도</span>
              <span className={`text-[11px] font-bold ${maxSev.value >= 70 ? "text-rose-600" : maxSev.value >= 40 ? "text-amber-600" : "text-emerald-600"}`}>
                {maxSev.value}%
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function SummaryCards({ anomalies }: { anomalies: AnomalyItem[] }) {
  const stats = useMemo(() => {
    const byType: Record<string, { count: number; maxSev: number; totalSev: number }> = {};
    for (const a of anomalies) {
      if (!byType[a.anomalyType]) {
        byType[a.anomalyType] = { count: 0, maxSev: 0, totalSev: 0 };
      }
      byType[a.anomalyType].count++;
      byType[a.anomalyType].maxSev = Math.max(byType[a.anomalyType].maxSev, a.severity);
      byType[a.anomalyType].totalSev += a.severity;
    }
    return Object.entries(byType)
      .map(([type, s]) => ({ type, ...s, avgSev: s.totalSev / s.count }))
      .sort((a, b) => b.maxSev - a.maxSev);
  }, [anomalies]);

  if (stats.length === 0) return null;

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
      {stats.map((s) => {
        const color = TYPE_COLORS[s.type] || "#94a3b8";
        const bg = TYPE_BG[s.type] || "bg-slate-50 ring-slate-200";
        const textColor = TYPE_TEXT[s.type] || "text-slate-600";
        const sevPct = Math.round(s.maxSev * 100);
        const sevLabel = s.maxSev >= 0.7 ? "Critical" : s.maxSev >= 0.4 ? "Warning" : "Info";

        return (
          <div key={s.type} className={`${bg} ring-1 rounded-xl px-3 py-2.5 transition-all hover:shadow-sm`}>
            <div className="flex items-center gap-2 mb-1.5">
              <div className="w-2 h-2 rounded-full" style={{ backgroundColor: color }} />
              <span className="text-[11px] font-semibold text-slate-700 truncate">
                {formatAnomalyType(s.type)}
              </span>
            </div>
            <div className="flex items-end justify-between">
              <span className={`text-xl font-bold ${textColor}`}>{s.count}</span>
              <div className="text-right">
                <span className={`text-[10px] font-medium ${s.maxSev >= 0.7 ? "text-rose-500" : s.maxSev >= 0.4 ? "text-amber-500" : "text-emerald-500"}`}>
                  {sevLabel} {sevPct}%
                </span>
              </div>
            </div>
            {/* severity mini bar */}
            <div className="w-full h-1 bg-white/60 rounded-full mt-1.5 overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{ width: `${sevPct}%`, backgroundColor: color }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
}

export function AnomalyTimeline() {
  const now = new Date();
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);

  const [startDate, setStartDate] = useState(formatDateForInput(yesterday));
  const [endDate, setEndDate] = useState(formatDateForInput(now));

  const swrKey = apiUrls.anomalies(
    100,
    toLocalISOString(startDate) || undefined,
    toLocalISOString(endDate) || undefined,
  );

  const { data, isLoading } = useSWR<AnomaliesResponse>(swrKey, fetcher, {
    refreshInterval: 15000,
  });

  const anomalies = data?.anomalies ?? [];
  const allTypes = useMemo(() => [...new Set(anomalies.map((a) => a.anomalyType))], [anomalies]);

  // 시간 범위에 따라 버킷 크기 자동 결정
  const bucketMinutes = useMemo(() => {
    if (anomalies.length === 0) return 60;
    const times = anomalies.map((a) => new Date(a.detectedAt).getTime());
    const range = Math.max(...times) - Math.min(...times);
    const hours = range / (1000 * 60 * 60);
    if (hours <= 3) return 15;
    if (hours <= 12) return 30;
    if (hours <= 48) return 60;
    return 120;
  }, [anomalies]);

  const chartData = useMemo(() => bucketAnomalies(anomalies, bucketMinutes), [anomalies, bucketMinutes]);

  const criticalCount = anomalies.filter((a) => a.severity >= 0.7).length;
  const warningCount = anomalies.filter((a) => a.severity >= 0.4 && a.severity < 0.7).length;

  return (
    <Card className="h-full">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-3">
            <CardTitle>Anomaly Timeline</CardTitle>
            {anomalies.length > 0 && (
              <div className="flex items-center gap-1.5">
                {criticalCount > 0 && (
                  <span className="text-[10px] font-bold text-rose-600 bg-rose-50 ring-1 ring-rose-200 px-2 py-0.5 rounded-full">
                    {criticalCount} critical
                  </span>
                )}
                {warningCount > 0 && (
                  <span className="text-[10px] font-bold text-amber-600 bg-amber-50 ring-1 ring-amber-200 px-2 py-0.5 rounded-full">
                    {warningCount} warning
                  </span>
                )}
              </div>
            )}
          </div>
          <div className="flex items-center gap-2">
            <input
              type="datetime-local"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-[12px] text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
            <span className="text-[12px] text-slate-400">~</span>
            <input
              type="datetime-local"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-[12px] text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="h-[380px] flex flex-col items-center justify-center gap-3">
            <div className="w-8 h-8 rounded-full border-2 border-slate-200 border-t-blue-500 animate-spin" />
            <p className="text-sm text-slate-400">데이터 로딩 중...</p>
          </div>
        ) : anomalies.length === 0 ? (
          <div className="h-[380px] flex flex-col items-center justify-center gap-3">
            <div className="w-12 h-12 rounded-full bg-emerald-50 flex items-center justify-center">
              <svg className="w-6 h-6 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-slate-600">All Clear</p>
              <p className="text-xs text-slate-400 mt-0.5">선택한 기간에 이상 탐지 이력이 없습니다</p>
            </div>
          </div>
        ) : (
          <>
            <SummaryCards anomalies={anomalies} />

            <ResponsiveContainer width="100%" height={280}>
              <ComposedChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <ReferenceLine
                  y={70}
                  yAxisId="severity"
                  stroke="#fca5a5"
                  strokeDasharray="6 3"
                  strokeWidth={1.5}
                  label={{ value: "Critical", position: "right", fontSize: 9, fill: "#f43f5e" }}
                />
                <ReferenceLine
                  y={40}
                  yAxisId="severity"
                  stroke="#fcd34d"
                  strokeDasharray="6 3"
                  strokeWidth={1.5}
                  label={{ value: "Warning", position: "right", fontSize: 9, fill: "#f59e0b" }}
                />
                <XAxis
                  dataKey="time"
                  tickFormatter={(v) =>
                    new Date(v).toLocaleString("ko-KR", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" })
                  }
                  tick={{ fontSize: 10, fill: "#94a3b8" }}
                  axisLine={{ stroke: "#e2e8f0" }}
                  tickLine={false}
                  interval="preserveStartEnd"
                />
                <YAxis
                  yAxisId="count"
                  orientation="left"
                  tick={{ fontSize: 10, fill: "#94a3b8" }}
                  axisLine={false}
                  tickLine={false}
                  width={30}
                  label={{ value: "건수", angle: -90, position: "insideLeft", fontSize: 10, fill: "#94a3b8", dx: -5 }}
                  allowDecimals={false}
                />
                <YAxis
                  yAxisId="severity"
                  orientation="right"
                  domain={[0, 100]}
                  ticks={[0, 20, 40, 60, 80, 100]}
                  tickFormatter={(v: number) => `${v}%`}
                  tick={{ fontSize: 10, fill: "#94a3b8" }}
                  axisLine={false}
                  tickLine={false}
                  width={40}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  verticalAlign="bottom"
                  iconType="square"
                  iconSize={10}
                  wrapperStyle={{ fontSize: 11, paddingTop: 8 }}
                  formatter={(value: string) => {
                    if (value === "avgSeverity") return "평균 심각도";
                    if (value === "maxSeverity") return "최대 심각도";
                    return formatAnomalyType(value);
                  }}
                />

                {/* Stacked bars per type */}
                {allTypes.map((type, i) => (
                  <Bar
                    key={type}
                    dataKey={type}
                    yAxisId="count"
                    stackId="anomalies"
                    fill={TYPE_COLORS[type] || "#94a3b8"}
                    fillOpacity={0.85}
                    radius={i === allTypes.length - 1 ? [3, 3, 0, 0] : [0, 0, 0, 0]}
                    maxBarSize={32}
                  />
                ))}

                {/* Severity trend lines */}
                <Line
                  dataKey="avgSeverity"
                  yAxisId="severity"
                  type="monotone"
                  stroke="#3b82f6"
                  strokeWidth={2}
                  dot={{ r: 3, fill: "#3b82f6", strokeWidth: 0 }}
                  activeDot={{ r: 5, fill: "#3b82f6", stroke: "#fff", strokeWidth: 2 }}
                  connectNulls
                />
                <Line
                  dataKey="maxSeverity"
                  yAxisId="severity"
                  type="monotone"
                  stroke="#ef4444"
                  strokeWidth={1.5}
                  strokeDasharray="4 3"
                  dot={false}
                  connectNulls
                />
              </ComposedChart>
            </ResponsiveContainer>

            <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-100">
              <span className="text-[10px] text-slate-400">
                {bucketMinutes}분 단위 집계 · {anomalies.length}건 표시
              </span>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
