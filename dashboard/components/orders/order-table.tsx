"use client";

import { useState } from "react";
import useSWR from "swr";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { OrderStatusBadge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { formatCurrency, formatTime } from "@/lib/utils";
import type { OrderResponse, OrderStatus } from "@/lib/types";

const STATUSES: OrderStatus[] = [
  "PENDING", "ACCEPTED", "PREPARING", "READY",
  "PICKED_UP", "DELIVERING", "DELIVERED", "CANCELLED",
];

export function OrderTable() {
  const [status, setStatus] = useState<OrderStatus>("PENDING");
  const { data: orders, isLoading } = useSWR<OrderResponse[]>(
    apiUrls.ordersByStatus(status),
    fetcher,
    { refreshInterval: 10000 }
  );

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Orders</CardTitle>
          <span className="text-[11px] text-slate-400 font-medium">
            {orders?.length ?? 0} results
          </span>
        </div>
      </CardHeader>
      <CardContent>
        {/* Status Tabs */}
        <div className="flex flex-wrap gap-1 mb-5 p-1 bg-slate-100/80 rounded-lg">
          {STATUSES.map((s) => (
            <button
              key={s}
              onClick={() => setStatus(s)}
              className={`px-3 py-1.5 text-[11px] font-semibold rounded-md transition-all duration-150 ${
                status === s
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              {s}
            </button>
          ))}
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left">
                <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">ID</th>
                <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Customer</th>
                <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Region</th>
                <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Amount</th>
                <th className="pb-3 pr-4 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Status</th>
                <th className="pb-3 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center">
                    <div className="flex flex-col items-center gap-2">
                      <div className="h-5 w-5 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
                      <span className="text-sm text-slate-400">Loading...</span>
                    </div>
                  </td>
                </tr>
              ) : !orders || orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-sm text-slate-400">
                    해당 상태의 주문이 없습니다
                  </td>
                </tr>
              ) : (
                orders.map((o) => (
                  <tr
                    key={o.orderId}
                    className="hover:bg-slate-50/80 transition-colors"
                  >
                    <td className="py-3 pr-4 font-mono text-xs text-slate-600">#{o.orderId}</td>
                    <td className="py-3 pr-4 text-[13px] font-medium text-slate-900">
                      {o.customerName || `Customer #${o.customerId}`}
                    </td>
                    <td className="py-3 pr-4">
                      <span className="text-[11px] font-medium text-slate-600 bg-slate-100 rounded-md px-2 py-0.5">
                        {o.region}
                      </span>
                    </td>
                    <td className="py-3 pr-4 text-[13px] font-semibold text-slate-900">
                      {formatCurrency(o.totalAmount)}
                    </td>
                    <td className="py-3 pr-4">
                      <OrderStatusBadge status={o.status} />
                    </td>
                    <td className="py-3 text-[12px] text-slate-500">{formatTime(o.orderTime)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
}
