"use client";

import useSWR from "swr";
import { ShoppingBag, MapPin, Clock, User } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { timeAgo, formatCurrency } from "@/lib/utils";
import type { OrderSearchResponse, OrderStatus } from "@/lib/types";

const STATUS_STYLE: Record<string, { variant: "success" | "destructive" | "warning" | "secondary" | "default"; label: string }> = {
  PENDING: { variant: "warning", label: "대기" },
  ACCEPTED: { variant: "default", label: "접수" },
  PREPARING: { variant: "default", label: "조리중" },
  READY: { variant: "default", label: "준비완료" },
  PICKED_UP: { variant: "default", label: "픽업" },
  DELIVERING: { variant: "warning", label: "배달중" },
  DELIVERED: { variant: "success", label: "완료" },
  CANCELLED: { variant: "destructive", label: "취소" },
};

export function RecentOrders() {
  const { data } = useSWR<OrderSearchResponse>(
    apiUrls.orderSearch({ page: 0, size: 8 }),
    fetcher,
    { refreshInterval: 30000 }
  );

  const orders = data?.content ?? [];

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Recent Orders</CardTitle>
            <p className="text-[12px] text-slate-400 mt-0.5">최근 주문 현황</p>
          </div>
          {data && (
            <span className="text-[11px] text-slate-400 font-medium">
              총 {data.totalElements.toLocaleString()}건
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {orders.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-10 gap-3">
            <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center">
              <ShoppingBag size={18} className="text-slate-400" />
            </div>
            <p className="text-sm text-slate-400">주문 데이터 없음</p>
          </div>
        ) : (
          <div className="space-y-1">
            {orders.map((order) => {
              const style = STATUS_STYLE[order.status] || STATUS_STYLE.PENDING;
              return (
                <div
                  key={order.orderId}
                  className="flex items-center gap-4 p-3 rounded-xl hover:bg-slate-50 transition-colors"
                >
                  {/* Order ID */}
                  <div className="w-16 shrink-0">
                    <span className="text-[11px] font-mono font-bold text-slate-500">#{order.orderId}</span>
                  </div>

                  {/* Customer & Region */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-1">
                        <User size={11} className="text-slate-400" />
                        <span className="text-[12px] font-semibold text-slate-800">{order.customerName}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <MapPin size={10} className="text-slate-400" />
                        <span className="text-[11px] text-slate-500">{order.region}</span>
                      </div>
                    </div>
                    <p className="text-[11px] text-slate-400 truncate mt-0.5">
                      {order.items.map((it) => it.menuItemName).join(", ")}
                    </p>
                  </div>

                  {/* Amount */}
                  <div className="text-right shrink-0">
                    <span className="text-[13px] font-bold text-slate-900">{formatCurrency(order.totalAmount)}</span>
                  </div>

                  {/* Status */}
                  <div className="shrink-0">
                    <Badge variant={style.variant}>{style.label}</Badge>
                  </div>

                  {/* Time */}
                  <div className="shrink-0 w-16 text-right">
                    <span className="text-[11px] text-slate-400">{timeAgo(order.orderTime)}</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
