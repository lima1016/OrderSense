"use client";

import useSWR from "swr";
import { Package, Bike, AlertTriangle, CreditCard, TrendingUp, TrendingDown } from "lucide-react";
import { fetcher, apiUrls } from "@/lib/api";
import { formatNumber } from "@/lib/utils";
import type { AnalyticsStats, AnomaliesResponse } from "@/lib/types";

export function StatsCards() {
  const { data: stats } = useSWR<AnalyticsStats>(apiUrls.analyticsStats, fetcher, {
    refreshInterval: 15000,
  });
  const { data: anomalies } = useSWR<AnomaliesResponse>(apiUrls.anomalies(50), fetcher, {
    refreshInterval: 15000,
  });

  const totalOrders = stats
    ? Object.values(stats.currentOrdersByRegion).reduce((a, b) => a + b, 0)
    : 0;
  const totalRiders = stats
    ? Object.values(stats.availableRidersByRegion).reduce((a, b) => a + b, 0)
    : 0;
  const anomalyCount = anomalies?.count ?? 0;
  const paymentFailures = stats?.paymentFailures1h ?? 0;

  const cards = [
    {
      label: "Active Orders",
      value: formatNumber(totalOrders),
      subtext: "Current hour",
      icon: Package,
      iconBg: "bg-blue-50",
      iconColor: "text-blue-600",
      trend: { value: 12, up: true },
    },
    {
      label: "Available Riders",
      value: formatNumber(totalRiders),
      subtext: "All regions",
      icon: Bike,
      iconBg: "bg-emerald-50",
      iconColor: "text-emerald-600",
      trend: { value: 5, up: true },
    },
    {
      label: "Anomalies",
      value: formatNumber(anomalyCount),
      subtext: "Detected",
      icon: AlertTriangle,
      iconBg: "bg-rose-50",
      iconColor: "text-rose-600",
      trend: anomalyCount > 0 ? { value: anomalyCount, up: false } : undefined,
    },
    {
      label: "Payment Failures",
      value: formatNumber(paymentFailures),
      subtext: "Last 1 hour",
      icon: CreditCard,
      iconBg: "bg-amber-50",
      iconColor: "text-amber-600",
      trend: paymentFailures > 0 ? { value: paymentFailures, up: false } : undefined,
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {cards.map(({ label, value, subtext, icon: Icon, iconBg, iconColor, trend }) => (
        <div
          key={label}
          className="group rounded-2xl border border-slate-200/60 bg-white p-5 hover:shadow-md transition-all duration-200 hover:border-slate-300/80"
        >
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-[13px] font-medium text-slate-500">{label}</p>
              <p className="text-[28px] font-bold text-slate-900 tracking-tight mt-1 leading-none">
                {value}
              </p>
              <div className="flex items-center gap-2 mt-2">
                {trend ? (
                  <span
                    className={`inline-flex items-center gap-0.5 text-[11px] font-semibold px-1.5 py-0.5 rounded-md ${
                      trend.up
                        ? "text-emerald-700 bg-emerald-50"
                        : "text-rose-700 bg-rose-50"
                    }`}
                  >
                    {trend.up ? (
                      <TrendingUp size={10} />
                    ) : (
                      <TrendingDown size={10} />
                    )}
                    {trend.value}%
                  </span>
                ) : (
                  <span className="text-[11px] font-medium text-slate-400">—</span>
                )}
                <span className="text-[11px] text-slate-400">{subtext}</span>
              </div>
            </div>
            <div className={`w-10 h-10 rounded-xl ${iconBg} flex items-center justify-center shrink-0`}>
              <Icon size={18} className={iconColor} strokeWidth={1.8} />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
