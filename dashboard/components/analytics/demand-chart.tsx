"use client";

import useSWR from "swr";
import {
  ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, ReferenceLine,
} from "recharts";
import { TrendingUp, TrendingDown, Minus, Activity, BarChart3, Target } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { DemandResponse, DemandForecast } from "@/lib/types";

/* ── Summary Cards ── */
function SummaryCards({ forecasts }: { forecasts: DemandForecast[] }) {
  const totalCurrent = forecasts.reduce((s, f) => s + f.current_hour_orders, 0);
  const totalPredicted = forecasts.reduce((s, f) => s + f.predicted_next_hour_orders, 0);
  const changeRate = totalCurrent > 0
    ? ((totalPredicted - totalCurrent) / totalCurrent) * 100
    : 0;
  const avgConfidence = forecasts.length > 0
    ? forecasts.reduce((s, f) => s + f.confidence, 0) / forecasts.length
    : 0;

  const cards = [
    {
      label: "현재 주문",
      value: totalCurrent.toLocaleString(),
      sub: "건 / 시간",
      icon: BarChart3,
      color: "text-blue-600",
      bg: "bg-blue-50",
    },
    {
      label: "예측 주문",
      value: totalPredicted.toLocaleString(),
      sub: changeRate >= 0 ? `+${changeRate.toFixed(1)}%` : `${changeRate.toFixed(1)}%`,
      icon: Activity,
      color: changeRate >= 0 ? "text-emerald-600" : "text-rose-600",
      bg: changeRate >= 0 ? "bg-emerald-50" : "bg-rose-50",
    },
    {
      label: "평균 신뢰도",
      value: `${Math.round(avgConfidence * 100)}%`,
      sub: avgConfidence >= 0.7 ? "높음" : avgConfidence >= 0.4 ? "보통" : "낮음",
      icon: Target,
      color: avgConfidence >= 0.7 ? "text-emerald-600" : avgConfidence >= 0.4 ? "text-amber-600" : "text-rose-600",
      bg: avgConfidence >= 0.7 ? "bg-emerald-50" : avgConfidence >= 0.4 ? "bg-amber-50" : "bg-rose-50",
    },
  ];

  return (
    <div className="grid grid-cols-3 gap-3 mb-5">
      {cards.map((c) => (
        <div key={c.label} className="flex items-center gap-3 rounded-xl border border-slate-100 bg-slate-50/50 p-3">
          <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${c.bg}`}>
            <c.icon size={18} className={c.color} />
          </div>
          <div>
            <p className="text-[11px] text-slate-400 font-medium">{c.label}</p>
            <div className="flex items-baseline gap-1.5">
              <span className="text-lg font-bold text-slate-900">{c.value}</span>
              <span className={`text-[11px] font-semibold ${c.color}`}>{c.sub}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

/* ── Trend Icon ── */
const TrendIcon = ({ trend, size = 14 }: { trend: string; size?: number }) => {
  if (trend === "increasing") return <TrendingUp size={size} className="text-emerald-500" />;
  if (trend === "decreasing") return <TrendingDown size={size} className="text-rose-500" />;
  return <Minus size={size} className="text-slate-400" />;
};

/* ── Custom Tooltip ── */
function CustomTooltip({ active, payload, label }: {
  active?: boolean;
  payload?: Array<{ dataKey: string; value: number; color: string; payload: Record<string, unknown> }>;
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  const d = payload[0]?.payload as Record<string, unknown>;
  const current = d.current as number;
  const predicted = d.predicted as number;
  const change = current > 0 ? ((predicted - current) / current) * 100 : 0;
  const confidence = d.confidence as number;
  const trend = d.trend as string;

  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-lg shadow-slate-200/50 min-w-[180px]">
      <div className="flex items-center justify-between mb-2">
        <p className="text-[13px] font-bold text-slate-900">{label}</p>
        <TrendIcon trend={trend} size={14} />
      </div>
      <div className="space-y-1.5">
        <div className="flex items-center justify-between text-[12px]">
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-sm bg-blue-500" />
            <span className="text-slate-500">현재</span>
          </div>
          <span className="font-bold text-slate-900">{current.toLocaleString()}건</span>
        </div>
        <div className="flex items-center justify-between text-[12px]">
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-sm bg-violet-500" />
            <span className="text-slate-500">예측</span>
          </div>
          <span className="font-bold text-slate-900">{predicted.toLocaleString()}건</span>
        </div>
        <div className="border-t border-slate-100 pt-1.5 flex items-center justify-between text-[11px]">
          <span className="text-slate-400">변화율</span>
          <span className={`font-bold ${change >= 0 ? "text-emerald-600" : "text-rose-600"}`}>
            {change >= 0 ? "+" : ""}{change.toFixed(1)}%
          </span>
        </div>
        <div className="flex items-center justify-between text-[11px]">
          <span className="text-slate-400">신뢰도</span>
          <span className="font-bold text-slate-600">{confidence}%</span>
        </div>
      </div>
    </div>
  );
}

/* ── Custom Bar Label ── */
function BarLabel({ x, y, width, value, data }: {
  x?: number; y?: number; width?: number; value?: number;
  data: Array<{ change: number }>;
  index?: number;
}) {
  // This renders the change percentage above each predicted bar
  if (!x || !y || !width) return null;
  return null; // We'll show change in the region cards instead
}

/* ── Region Detail Cards ── */
function RegionCards({ data }: { data: Array<{
  region: string; current: number; predicted: number; trend: string; confidence: number; change: number;
}> }) {
  // Sort by predicted orders descending
  const sorted = [...data].sort((a, b) => b.predicted - a.predicted);

  return (
    <div className="mt-4 grid grid-cols-2 sm:grid-cols-5 gap-2">
      {sorted.map((d, i) => {
        const isUp = d.change > 0;
        const isDown = d.change < 0;
        return (
          <div
            key={d.region}
            className="relative overflow-hidden rounded-xl border border-slate-100 bg-gradient-to-br from-white to-slate-50 p-3"
          >
            {/* Rank badge */}
            {i < 3 && (
              <div className={`absolute top-1.5 right-1.5 w-5 h-5 rounded-full flex items-center justify-center text-[9px] font-bold text-white ${
                i === 0 ? "bg-amber-400" : i === 1 ? "bg-slate-400" : "bg-amber-700"
              }`}>
                {i + 1}
              </div>
            )}
            <p className="text-[12px] font-bold text-slate-900 mb-1">{d.region}</p>
            <div className="flex items-end gap-1 mb-1.5">
              <span className="text-[20px] font-extrabold text-slate-900 leading-none">
                {d.predicted}
              </span>
              <span className="text-[10px] text-slate-400 mb-0.5">건 예측</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-1">
                <TrendIcon trend={d.trend} size={12} />
                <span className={`text-[11px] font-bold ${
                  isUp ? "text-emerald-600" : isDown ? "text-rose-600" : "text-slate-400"
                }`}>
                  {isUp ? "+" : ""}{d.change.toFixed(0)}%
                </span>
              </div>
              {/* Confidence mini bar */}
              <div className="flex items-center gap-1">
                <div className="w-12 h-1.5 rounded-full bg-slate-100 overflow-hidden">
                  <div
                    className={`h-full rounded-full ${
                      d.confidence >= 70 ? "bg-emerald-400" : d.confidence >= 40 ? "bg-amber-400" : "bg-rose-400"
                    }`}
                    style={{ width: `${d.confidence}%` }}
                  />
                </div>
                <span className="text-[9px] text-slate-400">{d.confidence}%</span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ── Main Component ── */
export function DemandChart() {
  const { data } = useSWR<DemandResponse>(apiUrls.demand(), fetcher, {
    refreshInterval: 30000,
  });

  const forecasts = data?.forecasts ?? [];

  const chartData = forecasts.map((f) => {
    const change = f.current_hour_orders > 0
      ? ((f.predicted_next_hour_orders - f.current_hour_orders) / f.current_hour_orders) * 100
      : 0;
    return {
      region: f.region,
      current: f.current_hour_orders,
      predicted: f.predicted_next_hour_orders,
      trend: f.trend,
      confidence: Math.round(f.confidence * 100),
      change,
    };
  });

  // Average predicted for reference line
  const avgPredicted = chartData.length > 0
    ? Math.round(chartData.reduce((s, d) => s + d.predicted, 0) / chartData.length)
    : 0;

  // Color for each bar based on trend
  const barColor = (trend: string) => {
    if (trend === "increasing") return "#10b981";
    if (trend === "decreasing") return "#f43f5e";
    return "#94a3b8";
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Demand Forecast</CardTitle>
            <p className="text-[12px] text-slate-400 mt-0.5">지역별 다음 1시간 주문 수요 예측</p>
          </div>
          <div className="flex items-center gap-4 text-[11px] text-slate-400">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-sm bg-blue-400" />
              현재
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-sm bg-violet-400" />
              예측
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-0 border-t-2 border-dashed border-amber-400" />
              평균
            </div>
          </div>
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
            <SummaryCards forecasts={forecasts} />

            <ResponsiveContainer width="100%" height={300}>
              <ComposedChart data={chartData} barCategoryGap="25%">
                <defs>
                  <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#3b82f6" stopOpacity={0.9} />
                    <stop offset="100%" stopColor="#3b82f6" stopOpacity={0.5} />
                  </linearGradient>
                  <linearGradient id="predictedGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#8b5cf6" stopOpacity={0.9} />
                    <stop offset="100%" stopColor="#8b5cf6" stopOpacity={0.5} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis
                  dataKey="region"
                  tick={{ fontSize: 12, fill: "#64748b", fontWeight: 600 }}
                  axisLine={{ stroke: "#e2e8f0" }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false}
                  tickLine={false}
                  width={35}
                />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: "#f1f5f9", radius: 8 }} />

                {/* Average predicted reference line */}
                {avgPredicted > 0 && (
                  <ReferenceLine
                    y={avgPredicted}
                    stroke="#f59e0b"
                    strokeDasharray="6 4"
                    strokeWidth={1.5}
                    label={{
                      value: `평균 ${avgPredicted}`,
                      position: "right",
                      fill: "#f59e0b",
                      fontSize: 10,
                      fontWeight: 600,
                    }}
                  />
                )}

                {/* Current orders bar */}
                <Bar dataKey="current" fill="url(#barGradient)" radius={[6, 6, 0, 0]} barSize={28} />

                {/* Predicted line with dots */}
                <Line
                  type="monotone"
                  dataKey="predicted"
                  stroke="#8b5cf6"
                  strokeWidth={2.5}
                  dot={(props: { cx: number; cy: number; index: number; payload: Record<string, unknown> }) => {
                    const { cx, cy, index, payload } = props;
                    const trend = payload.trend as string;
                    const fill = trend === "increasing" ? "#10b981"
                      : trend === "decreasing" ? "#f43f5e"
                      : "#8b5cf6";
                    return (
                      <g key={index}>
                        <circle cx={cx} cy={cy} r={6} fill="white" stroke={fill} strokeWidth={2.5} />
                        <circle cx={cx} cy={cy} r={2.5} fill={fill} />
                      </g>
                    );
                  }}
                  activeDot={{ r: 8, stroke: "#8b5cf6", strokeWidth: 2, fill: "white" }}
                />
              </ComposedChart>
            </ResponsiveContainer>

            <RegionCards data={chartData} />
          </>
        )}
      </CardContent>
    </Card>
  );
}
