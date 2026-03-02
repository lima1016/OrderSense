"use client";

import { useState, useMemo } from "react";
import useSWR from "swr";
import {
  Zap, CheckCircle2, XCircle, AlertTriangle, Filter, ChevronDown,
  ShieldAlert, Clock, MapPin, TrendingDown, UserMinus, Store,
} from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { formatAnomalyType, timeAgo } from "@/lib/utils";
import type { ActionLogResponse, ActionResult } from "@/lib/types";

/* ── Constants ── */
const ANOMALY_TYPES = ["ALL", "ORDER_DROP", "DELIVERY_DELAY", "CUSTOMER_CHURN", "REJECTION_SPIKE"] as const;
const RESULTS = ["ALL", "SUCCESS", "FAILED", "PARTIAL"] as const;

const RESULT_CONFIG: Record<string, { icon: typeof CheckCircle2; color: string; bg: string; badge: "success" | "destructive" | "warning" }> = {
  SUCCESS: { icon: CheckCircle2, color: "text-emerald-600", bg: "bg-emerald-50", badge: "success" },
  FAILED: { icon: XCircle, color: "text-rose-600", bg: "bg-rose-50", badge: "destructive" },
  PARTIAL: { icon: AlertTriangle, color: "text-amber-600", bg: "bg-amber-50", badge: "warning" },
};

const ANOMALY_ICON: Record<string, typeof ShieldAlert> = {
  ORDER_DROP: TrendingDown,
  DELIVERY_DELAY: Clock,
  CUSTOMER_CHURN: UserMinus,
  REJECTION_SPIKE: Store,
};

const ANOMALY_COLOR: Record<string, { text: string; bg: string; border: string }> = {
  ORDER_DROP: { text: "text-red-700", bg: "bg-red-50", border: "border-red-200" },
  DELIVERY_DELAY: { text: "text-amber-700", bg: "bg-amber-50", border: "border-amber-200" },
  CUSTOMER_CHURN: { text: "text-violet-700", bg: "bg-violet-50", border: "border-violet-200" },
  REJECTION_SPIKE: { text: "text-orange-700", bg: "bg-orange-50", border: "border-orange-200" },
};

/* ── Summary Stats ── */
function SummaryStats({ logs }: { logs: ActionLogResponse[] }) {
  const total = logs.length;
  const success = logs.filter((l) => l.actionResult === "SUCCESS").length;
  const failed = logs.filter((l) => l.actionResult === "FAILED").length;
  const partial = logs.filter((l) => l.actionResult === "PARTIAL").length;
  const successRate = total > 0 ? Math.round((success / total) * 100) : 0;

  const stats = [
    { label: "총 실행", value: total, icon: Zap, color: "text-blue-600", bg: "bg-blue-50" },
    { label: "성공", value: success, icon: CheckCircle2, color: "text-emerald-600", bg: "bg-emerald-50" },
    { label: "실패", value: failed, icon: XCircle, color: "text-rose-600", bg: "bg-rose-50" },
    { label: "부분", value: partial, icon: AlertTriangle, color: "text-amber-600", bg: "bg-amber-50" },
  ];

  return (
    <div className="mb-4">
      <div className="grid grid-cols-4 gap-2 mb-3">
        {stats.map((s) => (
          <div key={s.label} className="flex items-center gap-2.5 rounded-xl border border-slate-100 bg-slate-50/50 p-2.5">
            <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${s.bg}`}>
              <s.icon size={16} className={s.color} />
            </div>
            <div>
              <p className="text-[10px] text-slate-400 font-medium">{s.label}</p>
              <span className="text-base font-bold text-slate-900">{s.value}</span>
            </div>
          </div>
        ))}
      </div>
      {/* Success rate bar */}
      <div className="flex items-center gap-3">
        <span className="text-[11px] text-slate-400 font-medium whitespace-nowrap">성공률</span>
        <div className="flex-1 h-2 rounded-full bg-slate-100 overflow-hidden">
          <div className="h-full rounded-full bg-emerald-400 transition-all duration-500" style={{ width: `${successRate}%` }} />
        </div>
        <span className="text-[12px] font-bold text-slate-900">{successRate}%</span>
      </div>
    </div>
  );
}

/* ── Type Distribution ── */
function TypeDistribution({ logs }: { logs: ActionLogResponse[] }) {
  const typeCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    logs.forEach((l) => { counts[l.anomalyType] = (counts[l.anomalyType] || 0) + 1; });
    return Object.entries(counts).sort((a, b) => b[1] - a[1]);
  }, [logs]);

  const max = typeCounts.length > 0 ? typeCounts[0][1] : 1;

  return (
    <div className="mb-4 grid grid-cols-2 sm:grid-cols-4 gap-2">
      {typeCounts.map(([type, count]) => {
        const Icon = ANOMALY_ICON[type] || ShieldAlert;
        const colors = ANOMALY_COLOR[type] || { text: "text-slate-700", bg: "bg-slate-50", border: "border-slate-200" };
        return (
          <div key={type} className={`relative overflow-hidden rounded-xl border ${colors.border} ${colors.bg} p-2.5`}>
            <div className="flex items-center gap-2 mb-1.5">
              <Icon size={14} className={colors.text} />
              <span className={`text-[11px] font-bold ${colors.text}`}>{formatAnomalyType(type)}</span>
            </div>
            <div className="flex items-end justify-between">
              <span className="text-xl font-extrabold text-slate-900">{count}</span>
              <div className="w-16 h-1.5 rounded-full bg-white/60 overflow-hidden">
                <div
                  className={`h-full rounded-full ${colors.text.replace("text-", "bg-").replace("-700", "-400")}`}
                  style={{ width: `${(count / max) * 100}%` }}
                />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ── Filter Bar ── */
function FilterBar({
  typeFilter, setTypeFilter, resultFilter, setResultFilter,
}: {
  typeFilter: string; setTypeFilter: (v: string) => void;
  resultFilter: string; setResultFilter: (v: string) => void;
}) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
        <Filter size={12} />
        필터
      </div>

      {/* Type filter */}
      <div className="relative">
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="appearance-none text-[12px] font-medium bg-slate-50 border border-slate-200 rounded-lg pl-2.5 pr-7 py-1.5 text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          {ANOMALY_TYPES.map((t) => (
            <option key={t} value={t}>{t === "ALL" ? "전체 타입" : formatAnomalyType(t)}</option>
          ))}
        </select>
        <ChevronDown size={12} className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
      </div>

      {/* Result filter */}
      <div className="flex gap-1">
        {RESULTS.map((r) => {
          const active = resultFilter === r;
          const cfg = RESULT_CONFIG[r];
          return (
            <button
              key={r}
              onClick={() => setResultFilter(r)}
              className={`text-[11px] font-medium px-2.5 py-1 rounded-lg transition-colors ${
                active
                  ? r === "ALL"
                    ? "bg-slate-900 text-white"
                    : `${cfg?.bg} ${cfg?.color}`
                  : "bg-slate-50 text-slate-400 hover:bg-slate-100"
              }`}
            >
              {r === "ALL" ? "전체" : r}
            </button>
          );
        })}
      </div>
    </div>
  );
}

/* ── Detail Drawer (expanded row) ── */
function DetailRow({ log }: { log: ActionLogResponse }) {
  const colors = ANOMALY_COLOR[log.anomalyType] || { text: "text-slate-700", bg: "bg-slate-50", border: "border-slate-200" };
  const resultCfg = RESULT_CONFIG[log.actionResult] || RESULT_CONFIG.PARTIAL;
  const ResultIcon = resultCfg.icon;

  return (
    <tr>
      <td colSpan={6} className="p-0">
        <div className="bg-slate-50/80 border-t border-b border-slate-100 px-6 py-4 animate-in slide-in-from-top-1 duration-200">
          <div className="grid grid-cols-2 gap-4">
            {/* Left: Action detail */}
            <div>
              <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-1">실행 액션</p>
              <div className={`rounded-lg border ${colors.border} ${colors.bg} p-3`}>
                <p className="text-[13px] text-slate-800 leading-relaxed">{log.action}</p>
              </div>
            </div>
            {/* Right: Result detail */}
            <div>
              <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-1">실행 결과</p>
              <div className={`rounded-lg border ${resultCfg.color.replace("text-", "border-").replace("-600", "-200")} ${resultCfg.bg} p-3`}>
                <div className="flex items-center gap-2 mb-1">
                  <ResultIcon size={14} className={resultCfg.color} />
                  <span className={`text-[12px] font-bold ${resultCfg.color}`}>{log.actionResult}</span>
                </div>
                <p className="text-[13px] text-slate-800 leading-relaxed">{log.result}</p>
              </div>
            </div>
          </div>
          <div className="mt-3 flex items-center gap-4 text-[11px] text-slate-400">
            <span>실행: {new Date(log.executedAt).toLocaleString("ko-KR")}</span>
            <span>기록: {new Date(log.createdAt).toLocaleString("ko-KR")}</span>
          </div>
        </div>
      </td>
    </tr>
  );
}

/* ── Main Component ── */
export function ActionLogs() {
  const { data: actions, isLoading } = useSWR<ActionLogResponse[]>(apiUrls.actions, fetcher, {
    refreshInterval: 15000,
  });

  const [typeFilter, setTypeFilter] = useState("ALL");
  const [resultFilter, setResultFilter] = useState("ALL");
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const logs = actions ?? [];

  const filtered = useMemo(() => {
    return logs.filter((l) => {
      if (typeFilter !== "ALL" && l.anomalyType !== typeFilter) return false;
      if (resultFilter !== "ALL" && l.actionResult !== resultFilter) return false;
      return true;
    });
  }, [logs, typeFilter, resultFilter]);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Action Executor Logs</CardTitle>
            <p className="text-[12px] text-slate-400 mt-0.5">이상 감지 자동 대응 실행 기록</p>
          </div>
          <span className="text-[11px] text-slate-400 font-medium">
            {filtered.length} / {logs.length} actions
          </span>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-2">
            <div className="h-5 w-5 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
            <span className="text-sm text-slate-400">Loading...</span>
          </div>
        ) : logs.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3">
            <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center">
              <Zap size={18} className="text-slate-400" />
            </div>
            <div className="text-center">
              <p className="text-sm font-medium text-slate-600">No Actions Yet</p>
              <p className="text-xs text-slate-400 mt-0.5">실행된 자동 대응 없음</p>
            </div>
          </div>
        ) : (
          <>
            <SummaryStats logs={logs} />
            <TypeDistribution logs={logs} />
            <FilterBar
              typeFilter={typeFilter}
              setTypeFilter={setTypeFilter}
              resultFilter={resultFilter}
              setResultFilter={setResultFilter}
            />

            <div className="overflow-x-auto rounded-xl border border-slate-100">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-slate-50/80">
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">ID</th>
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Type</th>
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Region</th>
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Action</th>
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Result</th>
                    <th className="py-2.5 px-4 text-left text-[10px] font-semibold text-slate-400 uppercase tracking-wider">Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {filtered.map((log) => {
                    const Icon = ANOMALY_ICON[log.anomalyType] || ShieldAlert;
                    const colors = ANOMALY_COLOR[log.anomalyType] || { text: "text-slate-700", bg: "bg-slate-50", border: "border-slate-200" };
                    const resultCfg = RESULT_CONFIG[log.actionResult] || RESULT_CONFIG.PARTIAL;
                    const isExpanded = expandedId === log.id;

                    return (
                      <>
                        <tr
                          key={log.id}
                          onClick={() => setExpandedId(isExpanded ? null : log.id)}
                          className={`cursor-pointer transition-colors ${
                            isExpanded ? "bg-slate-50" : "hover:bg-slate-50/60"
                          }`}
                        >
                          <td className="py-2.5 px-4 font-mono text-[11px] text-slate-500">#{log.id}</td>
                          <td className="py-2.5 px-4">
                            <div className="flex items-center gap-1.5">
                              <div className={`w-5 h-5 rounded flex items-center justify-center ${colors.bg}`}>
                                <Icon size={11} className={colors.text} />
                              </div>
                              <span className={`text-[11px] font-semibold ${colors.text}`}>
                                {formatAnomalyType(log.anomalyType)}
                              </span>
                            </div>
                          </td>
                          <td className="py-2.5 px-4">
                            <div className="flex items-center gap-1">
                              <MapPin size={10} className="text-slate-400" />
                              <span className="text-[11px] font-medium text-slate-600">{log.region || "—"}</span>
                            </div>
                          </td>
                          <td className="py-2.5 px-4 max-w-[220px]">
                            <p className="text-[12px] text-slate-600 truncate">{log.action}</p>
                          </td>
                          <td className="py-2.5 px-4">
                            <Badge variant={resultCfg.badge}>{log.actionResult}</Badge>
                          </td>
                          <td className="py-2.5 px-4 text-[11px] text-slate-400">{timeAgo(log.executedAt)}</td>
                        </tr>
                        {isExpanded && <DetailRow key={`detail-${log.id}`} log={log} />}
                      </>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {filtered.length === 0 && (
              <div className="text-center py-8 text-[13px] text-slate-400">
                필터 조건에 맞는 로그가 없습니다
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
