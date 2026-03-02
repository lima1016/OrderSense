"use client";

import { useState } from "react";
import useSWR from "swr";
import { AlertCircle, X, Clock, MapPin, Activity, ChevronRight } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { SeverityBadge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { timeAgo, formatAnomalyType, severityLabel, severityColor as getSeverityColor } from "@/lib/utils";
import type { AnomalyItem, AnomaliesResponse } from "@/lib/types";

const TYPE_COLORS: Record<string, string> = {
  ORDER_DROP: "#f43f5e",
  DELIVERY_DELAY: "#f59e0b",
  CUSTOMER_CHURN: "#8b5cf6",
  REJECTION_SPIKE: "#f97316",
};

const TYPE_BG: Record<string, string> = {
  ORDER_DROP: "bg-rose-50",
  DELIVERY_DELAY: "bg-amber-50",
  CUSTOMER_CHURN: "bg-violet-50",
  REJECTION_SPIKE: "bg-orange-50",
};

const TYPE_DESCRIPTIONS: Record<string, string> = {
  ORDER_DROP: "주문량이 평균 대비 급감한 상태입니다. 시스템 장애, 결제 실패 또는 외부 요인을 확인하세요.",
  DELIVERY_DELAY: "배달 시간이 정상 범위를 초과했습니다. 라이더 부족이나 교통 상황을 확인하세요.",
  CUSTOMER_CHURN: "고객 이탈 위험이 감지되었습니다. 재주문율 하락 추세를 모니터링하세요.",
  REJECTION_SPIKE: "가맹점 주문 거절률이 급등했습니다. 재고 부족이나 운영 이슈를 확인하세요.",
};

function formatMetadataKey(key: string): string {
  return key
    .replace(/([A-Z])/g, " $1")
    .replace(/_/g, " ")
    .replace(/^\w/, (c) => c.toUpperCase())
    .trim();
}

function formatMetadataValue(key: string, value: unknown): string {
  if (typeof value === "number") {
    if (key.includes("rate") || key.includes("ratio") || key.includes("drop") || key.includes("Rate")) {
      return `${(value * 100).toFixed(1)}%`;
    }
    if (key.includes("Minutes") || key.includes("min") || key.includes("Min")) {
      return `${value.toFixed(0)}분`;
    }
    return value.toLocaleString("ko-KR");
  }
  if (typeof value === "object" && value !== null) {
    return Object.entries(value as Record<string, unknown>)
      .map(([k, v]) => `${k}: ${v}`)
      .join(", ");
  }
  return String(value);
}

function DetailModal({ anomaly, onClose }: { anomaly: AnomalyItem; onClose: () => void }) {
  const sevLabel = severityLabel(anomaly.severity);
  const sevPct = Math.round(anomaly.severity * 100);
  const color = TYPE_COLORS[anomaly.anomalyType] || "#94a3b8";
  const bgClass = TYPE_BG[anomaly.anomalyType] || "bg-slate-50";
  const metadata = anomaly.metadata ?? {};
  const metaEntries = Object.entries(metadata).filter(([k]) => k !== "cause");

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4" onClick={onClose}>
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" />
      <div
        className="relative bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className={`${bgClass} px-6 pt-5 pb-4`}>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div
                className="w-10 h-10 rounded-xl flex items-center justify-center"
                style={{ backgroundColor: `${color}18` }}
              >
                <Activity size={20} style={{ color }} />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">
                  {formatAnomalyType(anomaly.anomalyType)}
                </h3>
                <p className="text-[12px] text-slate-500 mt-0.5">
                  {TYPE_DESCRIPTIONS[anomaly.anomalyType] || "이상 상태가 감지되었습니다."}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg hover:bg-white/60 transition-colors text-slate-400 hover:text-slate-600"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Info Row */}
        <div className="px-6 py-3 flex items-center gap-4 border-b border-slate-100">
          <div className="flex items-center gap-1.5 text-[12px] text-slate-600">
            <MapPin size={13} className="text-slate-400" />
            <span className="font-medium">{anomaly.region || "Global"}</span>
          </div>
          <div className="flex items-center gap-1.5 text-[12px] text-slate-600">
            <Clock size={13} className="text-slate-400" />
            <span>{new Date(anomaly.detectedAt).toLocaleString("ko-KR", {
              month: "short", day: "numeric", hour: "2-digit", minute: "2-digit", second: "2-digit",
            })}</span>
          </div>
          <div className="ml-auto">
            <SeverityBadge severity={anomaly.severity} />
          </div>
        </div>

        {/* Severity Bar */}
        <div className="px-6 py-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[12px] font-medium text-slate-700">심각도</span>
            <span className="text-[12px] font-bold" style={{ color }}>{sevLabel} ({sevPct}%)</span>
          </div>
          <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-500"
              style={{ width: `${sevPct}%`, backgroundColor: color }}
            />
          </div>
          <div className="flex justify-between mt-1 text-[9px] text-slate-400">
            <span>0%</span>
            <span className="text-amber-400">40% Warning</span>
            <span className="text-rose-400">70% Critical</span>
            <span>100%</span>
          </div>
        </div>

        {/* Description */}
        <div className="px-6 pb-3">
          <p className="text-[12px] font-medium text-slate-700 mb-1">설명</p>
          <p className="text-[13px] text-slate-600 leading-relaxed bg-slate-50 rounded-xl px-4 py-3">
            {anomaly.description}
          </p>
        </div>

        {/* Metadata */}
        {metaEntries.length > 0 && (
          <div className="px-6 pb-5">
            <p className="text-[12px] font-medium text-slate-700 mb-2">상세 데이터</p>
            <div className="grid grid-cols-2 gap-2">
              {metaEntries.map(([key, value]) => (
                <div key={key} className="bg-slate-50 rounded-lg px-3 py-2.5">
                  <p className="text-[10px] text-slate-400 uppercase tracking-wider">{formatMetadataKey(key)}</p>
                  <p className="text-[14px] font-semibold text-slate-800 mt-0.5">{formatMetadataValue(key, value)}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export function AnomalyList() {
  const [selected, setSelected] = useState<AnomalyItem | null>(null);

  const { data } = useSWR<AnomaliesResponse>(apiUrls.anomalies(10), fetcher, {
    refreshInterval: 15000,
  });

  const anomalies = data?.anomalies ?? [];

  return (
    <>
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
                  onClick={() => setSelected(a)}
                  className="flex items-start gap-3 p-3 rounded-xl hover:bg-slate-50 transition-colors group cursor-pointer"
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
                  <div className="flex items-center gap-1 pt-0.5">
                    <span className="text-[11px] text-slate-400 whitespace-nowrap">
                      {timeAgo(a.detectedAt)}
                    </span>
                    <ChevronRight size={14} className="text-slate-300 group-hover:text-slate-500 transition-colors" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {selected && <DetailModal anomaly={selected} onClose={() => setSelected(null)} />}
    </>
  );
}
