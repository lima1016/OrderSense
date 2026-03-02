"use client";

import useSWR from "swr";
import { UserMinus, AlertTriangle, TrendingDown, ShoppingBag } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { fetcher, apiUrls } from "@/lib/api";
import type { ChurnResponse } from "@/lib/types";

function RiskBar({ score }: { score: number }) {
  const pct = Math.round(score * 100);
  const color = pct >= 70 ? "bg-rose-400" : pct >= 40 ? "bg-amber-400" : "bg-blue-400";
  return (
    <div className="flex items-center gap-2">
      <div className="w-16 h-1.5 rounded-full bg-slate-100 overflow-hidden">
        <div className={`h-full rounded-full ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className={`text-[10px] font-bold ${
        pct >= 70 ? "text-rose-600" : pct >= 40 ? "text-amber-600" : "text-blue-600"
      }`}>{pct}%</span>
    </div>
  );
}

export function ChurnPreview() {
  const { data } = useSWR<ChurnResponse>(apiUrls.churn, fetcher, {
    refreshInterval: 60000,
  });

  const customers = data?.atRiskCustomers ?? [];
  const highRisk = customers.filter((c) => c.risk_score >= 0.7);
  const medRisk = customers.filter((c) => c.risk_score >= 0.4 && c.risk_score < 0.7);

  return (
    <Card className="h-full">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Churn Risk</CardTitle>
            <p className="text-[12px] text-slate-400 mt-0.5">이탈 위험 고객</p>
          </div>
          {customers.length > 0 && (
            <span className="text-[11px] font-semibold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full">
              {customers.length}명 감지
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {customers.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-10 gap-3">
            <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center">
              <UserMinus size={18} className="text-emerald-500" />
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-slate-600">위험 고객 없음</p>
              <p className="text-xs text-slate-400 mt-0.5">이탈 위험 감지되지 않음</p>
            </div>
          </div>
        ) : (
          <>
            {/* Summary */}
            <div className="grid grid-cols-2 gap-2 mb-3">
              <div className="rounded-xl bg-rose-50 border border-rose-100 p-2.5">
                <div className="flex items-center gap-1.5 mb-1">
                  <AlertTriangle size={12} className="text-rose-500" />
                  <span className="text-[10px] font-semibold text-rose-600">고위험</span>
                </div>
                <span className="text-lg font-extrabold text-slate-900">{highRisk.length}</span>
                <span className="text-[10px] text-slate-500 ml-1">명</span>
              </div>
              <div className="rounded-xl bg-amber-50 border border-amber-100 p-2.5">
                <div className="flex items-center gap-1.5 mb-1">
                  <TrendingDown size={12} className="text-amber-500" />
                  <span className="text-[10px] font-semibold text-amber-600">주의</span>
                </div>
                <span className="text-lg font-extrabold text-slate-900">{medRisk.length}</span>
                <span className="text-[10px] text-slate-500 ml-1">명</span>
              </div>
            </div>

            {/* Customer list */}
            <div className="space-y-1 max-h-[280px] overflow-y-auto pr-1 -mr-1">
              {customers.slice(0, 10).map((c) => (
                <div key={c.customer_id} className="flex items-center gap-3 p-2.5 rounded-xl hover:bg-slate-50 transition-colors">
                  <div className={`w-7 h-7 rounded-lg flex items-center justify-center ${
                    c.risk_score >= 0.7 ? "bg-rose-50" : c.risk_score >= 0.4 ? "bg-amber-50" : "bg-blue-50"
                  }`}>
                    <UserMinus size={13} className={
                      c.risk_score >= 0.7 ? "text-rose-500" : c.risk_score >= 0.4 ? "text-amber-500" : "text-blue-500"
                    } />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-[12px] font-semibold text-slate-800">고객 #{c.customer_id}</span>
                      <RiskBar score={c.risk_score} />
                    </div>
                    <div className="flex items-center gap-3 mt-0.5 text-[10px] text-slate-400">
                      <div className="flex items-center gap-1">
                        <ShoppingBag size={9} />
                        <span>총 {c.total_orders}건</span>
                      </div>
                      <span>최근 {c.recent_orders}건</span>
                      <span className="text-rose-500 font-medium">
                        빈도 -{Math.round(c.order_frequency_drop * 100)}%
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
