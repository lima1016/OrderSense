"use client";

import useSWR from "swr";
import { Zap } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { formatTime, formatAnomalyType } from "@/lib/utils";
import type { ActionLogResponse } from "@/lib/types";

const RESULT_VARIANT: Record<string, "success" | "destructive" | "warning"> = {
  SUCCESS: "success",
  FAILED: "destructive",
  PARTIAL: "warning",
};

export function ActionLogs() {
  const { data: actions, isLoading } = useSWR<ActionLogResponse[]>(apiUrls.actions, fetcher, {
    refreshInterval: 15000,
  });

  const logs = actions ?? [];

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Action Executor Logs</CardTitle>
          <span className="text-[11px] text-slate-400 font-medium">
            {logs.length} actions
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
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left">
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">ID</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Type</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Region</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Action</th>
                  <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Result</th>
                  <th className="pb-3 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 pr-4 font-mono text-xs text-slate-600">#{log.id}</td>
                    <td className="py-3 pr-4">
                      <span className="text-[12px] font-medium text-slate-900">
                        {formatAnomalyType(log.anomalyType)}
                      </span>
                    </td>
                    <td className="py-3 pr-4">
                      <span className="text-[11px] font-medium text-slate-600 bg-slate-100 rounded-md px-2 py-0.5">
                        {log.region || "—"}
                      </span>
                    </td>
                    <td className="py-3 pr-4 max-w-[200px]">
                      <p className="text-[12px] text-slate-600 truncate">{log.action}</p>
                    </td>
                    <td className="py-3 pr-4">
                      <Badge variant={RESULT_VARIANT[log.actionResult] || "secondary"}>
                        {log.actionResult}
                      </Badge>
                    </td>
                    <td className="py-3 text-[12px] text-slate-500">{formatTime(log.executedAt)}</td>
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
