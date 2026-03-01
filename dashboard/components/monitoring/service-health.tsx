"use client";

import useSWR from "swr";
import { Database, Truck, Store, CreditCard, ShoppingBag } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import { formatNumber } from "@/lib/utils";
import type { AnalyticsStats } from "@/lib/types";

const EVENT_META = [
  { key: "orders", label: "Orders", icon: ShoppingBag, color: "text-blue-600", bg: "bg-blue-50" },
  { key: "deliveries", label: "Deliveries", icon: Truck, color: "text-violet-600", bg: "bg-violet-50" },
  { key: "riders", label: "Riders", icon: Truck, color: "text-emerald-600", bg: "bg-emerald-50" },
  { key: "restaurants", label: "Restaurants", icon: Store, color: "text-amber-600", bg: "bg-amber-50" },
  { key: "payments", label: "Payments", icon: CreditCard, color: "text-rose-600", bg: "bg-rose-50" },
] as const;

export function ServiceHealth() {
  const { data: stats, error } = useSWR<AnalyticsStats>(apiUrls.analyticsStats, fetcher, {
    refreshInterval: 15000,
  });

  const pipelineUp = !!stats && !error;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Event Pipeline</CardTitle>
          <div className="flex items-center gap-2">
            <span className="relative flex h-2.5 w-2.5">
              {pipelineUp && (
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
              )}
              <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${pipelineUp ? "bg-emerald-500" : "bg-rose-500"}`} />
            </span>
            <span className={`text-[12px] font-semibold ${pipelineUp ? "text-emerald-700" : "text-rose-700"}`}>
              {pipelineUp ? "Healthy" : "Disconnected"}
            </span>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
          {EVENT_META.map(({ key, label, icon: Icon, color, bg }) => {
            const count = stats?.eventCounts[key] ?? 0;
            return (
              <div
                key={key}
                className="p-4 rounded-xl bg-slate-50/80 border border-slate-100 hover:border-slate-200 transition-colors text-center group"
              >
                <div className={`w-9 h-9 rounded-xl ${bg} flex items-center justify-center mx-auto mb-2`}>
                  <Icon size={16} className={color} strokeWidth={1.8} />
                </div>
                <p className="text-xl font-bold text-slate-900 tracking-tight">{formatNumber(count)}</p>
                <p className="text-[11px] font-medium text-slate-400 mt-0.5">{label}</p>
              </div>
            );
          })}
        </div>
        {stats && (
          <div className="flex items-center justify-center gap-2 mt-4 pt-3 border-t border-slate-100">
            <Database size={12} className="text-slate-400" />
            <p className="text-[11px] text-slate-400">
              {stats.totalRegions} regions tracked
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
