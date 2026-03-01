"use client";

import useSWR from "swr";
import { AlertCircle } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { SeverityBadge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { timeAgo, formatAnomalyType } from "@/lib/utils";
import type { AnomaliesResponse } from "@/lib/types";

export function AnomalyList() {
  const { data } = useSWR<AnomaliesResponse>(apiUrls.anomalies(10), fetcher, {
    refreshInterval: 15000,
  });

  const anomalies = data?.anomalies ?? [];

  return (
    <Card className="h-full">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Recent Anomalies</CardTitle>
          {anomalies.length > 0 && (
            <span className="text-[11px] font-semibold text-rose-600 bg-rose-50 px-2 py-0.5 rounded-full">
              {anomalies.length} active
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {anomalies.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-10 gap-3">
            <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center">
              <AlertCircle size={18} className="text-emerald-500" />
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-slate-600">All Clear</p>
              <p className="text-xs text-slate-400 mt-0.5">이상 탐지 없음</p>
            </div>
          </div>
        ) : (
          <div className="space-y-1 max-h-[340px] overflow-y-auto pr-1 -mr-1">
            {anomalies.map((a, i) => (
              <div
                key={i}
                className="flex items-start gap-3 p-3 rounded-xl hover:bg-slate-50 transition-colors group"
              >
                <div className="mt-0.5">
                  <SeverityBadge severity={a.severity} />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-[13px] font-medium text-slate-900">
                      {formatAnomalyType(a.anomalyType)}
                    </span>
                    {a.region && (
                      <span className="text-[10px] font-medium text-slate-500 bg-slate-100 rounded-md px-1.5 py-0.5">
                        {a.region}
                      </span>
                    )}
                  </div>
                  <p className="text-[12px] text-slate-500 mt-0.5 truncate leading-relaxed">
                    {a.description}
                  </p>
                </div>
                <span className="text-[11px] text-slate-400 whitespace-nowrap pt-0.5">
                  {timeAgo(a.detectedAt)}
                </span>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
