"use client";

import useSWR from "swr";
import { UserX } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import type { ChurnResponse } from "@/lib/types";

export function ChurnTable() {
  const { data } = useSWR<ChurnResponse>(apiUrls.churn, fetcher, {
    refreshInterval: 60000,
  });

  const customers = data?.atRiskCustomers ?? [];
  const sorted = [...customers].sort((a, b) => b.risk_score - a.risk_score);

  return (
    <Card className="h-full">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Churn Risk</CardTitle>
          {sorted.length > 0 && (
            <span className="text-[11px] font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md ring-1 ring-inset ring-amber-600/20">
              {sorted.length} at risk
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {sorted.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-10 gap-3">
            <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center">
              <UserX size={18} className="text-emerald-500" />
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-slate-600">No Risk Detected</p>
              <p className="text-xs text-slate-400 mt-0.5">이탈 위험 고객 없음</p>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left">
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Customer</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Total</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Recent</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Drop</th>
                  <th className="pb-3 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Risk</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sorted.map((c) => (
                  <tr key={c.customer_id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 pr-4 font-mono text-xs text-slate-600">#{c.customer_id}</td>
                    <td className="py-3 pr-4 text-[13px] text-slate-900 font-medium">{c.total_orders}</td>
                    <td className="py-3 pr-4 text-[13px] text-slate-900">{c.recent_orders}</td>
                    <td className="py-3 pr-4">
                      <span className="text-[12px] font-semibold text-rose-600">
                        -{Math.round(c.order_frequency_drop * 100)}%
                      </span>
                    </td>
                    <td className="py-3">
                      <div className="flex items-center gap-2.5">
                        <div className="w-16 h-1.5 bg-slate-100 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all duration-500 ${
                              c.risk_score >= 0.7
                                ? "bg-rose-500"
                                : c.risk_score >= 0.4
                                ? "bg-amber-500"
                                : "bg-slate-400"
                            }`}
                            style={{ width: `${c.risk_score * 100}%` }}
                          />
                        </div>
                        <Badge
                          variant={
                            c.risk_score >= 0.7
                              ? "destructive"
                              : c.risk_score >= 0.4
                              ? "warning"
                              : "secondary"
                          }
                        >
                          {Math.round(c.risk_score * 100)}%
                        </Badge>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
