"use client";

import useSWR from "swr";
import { ShoppingBag, Truck, Bike, Store, CreditCard, ArrowRight } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { AnalyticsStats } from "@/lib/types";

const PIPELINE_ITEMS = [
  { key: "orders", label: "주문", icon: ShoppingBag, color: "text-blue-600", bg: "bg-blue-50", ring: "ring-blue-200" },
  { key: "deliveries", label: "배달", icon: Truck, color: "text-emerald-600", bg: "bg-emerald-50", ring: "ring-emerald-200" },
  { key: "riders", label: "라이더", icon: Bike, color: "text-violet-600", bg: "bg-violet-50", ring: "ring-violet-200" },
  { key: "restaurants", label: "가맹점", icon: Store, color: "text-amber-600", bg: "bg-amber-50", ring: "ring-amber-200" },
  { key: "payments", label: "결제", icon: CreditCard, color: "text-rose-600", bg: "bg-rose-50", ring: "ring-rose-200" },
] as const;

export function EventPipeline() {
  const { data: stats } = useSWR<AnalyticsStats>(apiUrls.analyticsStats, fetcher, {
    refreshInterval: 15000,
  });

  const counts = stats?.eventCounts ?? { orders: 0, deliveries: 0, riders: 0, restaurants: 0, payments: 0 };
  const total = Object.values(counts).reduce((a, b) => a + b, 0);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Event Pipeline</CardTitle>
            <p className="text-[12px] text-slate-400 mt-0.5">Kafka 이벤트 스트림 실시간 현황</p>
          </div>
          <div className="text-right">
            <span className="text-lg font-bold text-slate-900">{total.toLocaleString()}</span>
            <span className="text-[11px] text-slate-400 ml-1">total events</span>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-2">
          {PIPELINE_ITEMS.map((item, i) => {
            const count = counts[item.key] ?? 0;
            const pct = total > 0 ? Math.round((count / total) * 100) : 0;
            return (
              <div key={item.key} className="flex items-center gap-2 flex-1">
                <div className={`flex-1 rounded-xl border border-slate-100 p-3 hover:shadow-md transition-all duration-200 group`}>
                  <div className="flex items-center gap-2.5 mb-2">
                    <div className={`w-8 h-8 rounded-lg ${item.bg} flex items-center justify-center ring-1 ${item.ring}`}>
                      <item.icon size={15} className={item.color} />
                    </div>
                    <span className="text-[12px] font-semibold text-slate-700">{item.label}</span>
                  </div>
                  <div className="flex items-end justify-between">
                    <span className="text-xl font-extrabold text-slate-900">{count.toLocaleString()}</span>
                    <span className="text-[11px] font-medium text-slate-400">{pct}%</span>
                  </div>
                  {/* Mini progress bar */}
                  <div className="mt-2 w-full h-1.5 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-700 ${item.bg.replace("bg-", "bg-").replace("-50", "-400")}`}
                      style={{ width: `${pct}%`, backgroundColor: undefined }}
                    />
                  </div>
                </div>
                {i < PIPELINE_ITEMS.length - 1 && (
                  <ArrowRight size={14} className="text-slate-300 shrink-0" />
                )}
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
