"use client";

import { useState, useEffect, useCallback } from "react";
import useSWR from "swr";
import {
  X, MapPin, Clock, User, ShoppingBag, Search,
  ChevronRight, ChevronLeft, Truck, Ban, Package, Timer,
} from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { OrderStatusBadge } from "@/components/ui/badge";
import { fetcher, apiUrls } from "@/lib/api";
import { formatCurrency, formatTime } from "@/lib/utils";
import type { OrderResponse, OrderStatus, OrderSearchResponse } from "@/lib/types";

const STATUSES: (OrderStatus | "ALL")[] = [
  "ALL", "PENDING", "ACCEPTED", "PREPARING", "READY",
  "PICKED_UP", "DELIVERING", "DELIVERED", "CANCELLED",
];

const PAGE_SIZE = 20;

/* ── 디바운스 훅 ── */
function useDebounce(value: string, delay: number) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debounced;
}

/* ── 주문 상태 타임라인 스텝 ── */
const TIMELINE_STEPS: { key: keyof OrderResponse; label: string; icon: typeof Clock }[] = [
  { key: "orderTime", label: "주문", icon: ShoppingBag },
  { key: "acceptedTime", label: "접수", icon: Package },
  { key: "prepStartTime", label: "조리 시작", icon: Timer },
  { key: "readyTime", label: "준비 완료", icon: Package },
  { key: "pickupTime", label: "픽업", icon: Truck },
  { key: "deliveredTime", label: "배달 완료", icon: MapPin },
];

function formatDateTime(iso: string | null): string {
  if (!iso) return "-";
  return new Date(iso).toLocaleString("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

/* ── 타임라인 컴포넌트 ── */
function OrderTimeline({ order }: { order: OrderResponse }) {
  const isCancelled = order.status === "CANCELLED";

  const steps = isCancelled
    ? [...TIMELINE_STEPS.filter((s) => order[s.key] != null), { key: "cancelledTime" as keyof OrderResponse, label: "취소", icon: Ban }]
    : TIMELINE_STEPS;

  return (
    <div className="relative">
      {steps.map((step, i) => {
        const value = order[step.key] as string | null;
        const isCompleted = value != null;
        const isLast = i === steps.length - 1;
        const isCancelStep = step.key === "cancelledTime";
        const Icon = step.icon;

        return (
          <div key={step.key} className="flex gap-3 relative">
            {!isLast && (
              <div className={`absolute left-[13px] top-[28px] w-[2px] h-[calc(100%-8px)] ${
                isCompleted ? (isCancelStep ? "bg-rose-200" : "bg-blue-200") : "bg-slate-100"
              }`} />
            )}
            <div className={`relative z-10 w-[28px] h-[28px] rounded-full flex items-center justify-center flex-shrink-0 ${
              isCompleted
                ? isCancelStep
                  ? "bg-rose-100 text-rose-600"
                  : "bg-blue-100 text-blue-600"
                : "bg-slate-100 text-slate-300"
            }`}>
              <Icon size={14} />
            </div>
            <div className={`pb-4 ${isCompleted ? "" : "opacity-40"}`}>
              <p className={`text-[12px] font-semibold ${isCancelStep ? "text-rose-600" : "text-slate-800"}`}>
                {step.label}
              </p>
              <p className="text-[11px] text-slate-400">
                {isCompleted ? formatDateTime(value) : "대기 중"}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ── 상세 모달 ── */
function OrderDetailModal({ order, onClose }: { order: OrderResponse; onClose: () => void }) {
  const items = order.items ?? [];
  const hasCancelReason = order.status === "CANCELLED" && order.cancelReason;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4" onClick={onClose}>
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" />
      <div
        className="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-hidden animate-in fade-in zoom-in-95 duration-200 flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="bg-slate-50 px-6 pt-5 pb-4 flex-shrink-0">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2.5">
                <span className="text-lg font-bold text-slate-900">주문 #{order.orderId}</span>
                <OrderStatusBadge status={order.status} />
              </div>
              <p className="text-[12px] text-slate-500 mt-1">
                {new Date(order.orderTime).toLocaleString("ko-KR", {
                  year: "numeric", month: "long", day: "numeric",
                  hour: "2-digit", minute: "2-digit",
                })}
              </p>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg hover:bg-white/80 transition-colors text-slate-400 hover:text-slate-600"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Scrollable Body */}
        <div className="overflow-y-auto flex-1 px-6 py-4 space-y-5">
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-slate-50 rounded-xl px-4 py-3">
              <div className="flex items-center gap-1.5 mb-1">
                <User size={13} className="text-slate-400" />
                <p className="text-[10px] text-slate-400 uppercase tracking-wider">고객</p>
              </div>
              <p className="text-[13px] font-semibold text-slate-800">
                {order.customerName || `Customer #${order.customerId}`}
              </p>
            </div>
            <div className="bg-slate-50 rounded-xl px-4 py-3">
              <div className="flex items-center gap-1.5 mb-1">
                <MapPin size={13} className="text-slate-400" />
                <p className="text-[10px] text-slate-400 uppercase tracking-wider">지역</p>
              </div>
              <p className="text-[13px] font-semibold text-slate-800">{order.region}</p>
            </div>
          </div>

          {order.deliveryAddress && (
            <div className="bg-blue-50/60 rounded-xl px-4 py-3">
              <div className="flex items-center gap-1.5 mb-1">
                <Truck size={13} className="text-blue-400" />
                <p className="text-[10px] text-blue-500 uppercase tracking-wider font-medium">배달 주소</p>
              </div>
              <p className="text-[13px] text-slate-700">{order.deliveryAddress}</p>
              {(order.estimatedDeliveryMinutes || order.actualDeliveryMinutes) && (
                <div className="flex gap-4 mt-2 text-[11px]">
                  {order.estimatedDeliveryMinutes && (
                    <span className="text-slate-500">
                      예상 <span className="font-semibold text-slate-700">{order.estimatedDeliveryMinutes}분</span>
                    </span>
                  )}
                  {order.actualDeliveryMinutes && (
                    <span className="text-slate-500">
                      실제 <span className={`font-semibold ${
                        order.estimatedDeliveryMinutes && order.actualDeliveryMinutes > order.estimatedDeliveryMinutes
                          ? "text-rose-600" : "text-emerald-600"
                      }`}>{order.actualDeliveryMinutes}분</span>
                    </span>
                  )}
                </div>
              )}
            </div>
          )}

          {hasCancelReason && (
            <div className="bg-rose-50 rounded-xl px-4 py-3">
              <div className="flex items-center gap-1.5 mb-1">
                <Ban size={13} className="text-rose-400" />
                <p className="text-[10px] text-rose-500 uppercase tracking-wider font-medium">취소 사유</p>
              </div>
              <p className="text-[13px] text-rose-700">{order.cancelReason}</p>
            </div>
          )}

          <div>
            <p className="text-[12px] font-semibold text-slate-700 mb-2 flex items-center gap-1.5">
              <ShoppingBag size={14} className="text-slate-400" />
              주문 항목
              {items.length > 0 && (
                <span className="text-[10px] font-medium text-slate-400 ml-1">{items.length}개</span>
              )}
            </p>
            {items.length > 0 ? (
              <div className="bg-slate-50 rounded-xl overflow-hidden">
                <table className="w-full text-[12px]">
                  <thead>
                    <tr className="border-b border-slate-200/60">
                      <th className="text-left px-4 py-2.5 text-[10px] text-slate-400 uppercase tracking-wider font-semibold">메뉴</th>
                      <th className="text-center px-2 py-2.5 text-[10px] text-slate-400 uppercase tracking-wider font-semibold">수량</th>
                      <th className="text-right px-2 py-2.5 text-[10px] text-slate-400 uppercase tracking-wider font-semibold">단가</th>
                      <th className="text-right px-4 py-2.5 text-[10px] text-slate-400 uppercase tracking-wider font-semibold">합계</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200/40">
                    {items.map((item) => (
                      <tr key={item.id}>
                        <td className="px-4 py-2.5 font-medium text-slate-800">{item.menuItemName}</td>
                        <td className="px-2 py-2.5 text-center text-slate-600">{item.quantity}</td>
                        <td className="px-2 py-2.5 text-right text-slate-500">{formatCurrency(item.unitPrice)}</td>
                        <td className="px-4 py-2.5 text-right font-semibold text-slate-800">{formatCurrency(item.totalPrice)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="bg-slate-50 rounded-xl px-4 py-6 text-center text-[12px] text-slate-400">
                주문 항목 정보가 없습니다
              </div>
            )}
          </div>

          <div>
            <p className="text-[12px] font-semibold text-slate-700 mb-3 flex items-center gap-1.5">
              <Clock size={14} className="text-slate-400" />
              진행 상태
            </p>
            <OrderTimeline order={order} />
          </div>
        </div>

        {/* Footer */}
        <div className="flex-shrink-0 px-6 py-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
          <div>
            <span className="text-[11px] text-slate-400">레스토랑 ID</span>
            <span className="text-[12px] font-mono font-semibold text-slate-600 ml-1.5">#{order.restaurantId}</span>
            {order.riderId && (
              <>
                <span className="text-slate-300 mx-2">|</span>
                <span className="text-[11px] text-slate-400">라이더 ID</span>
                <span className="text-[12px] font-mono font-semibold text-slate-600 ml-1.5">#{order.riderId}</span>
              </>
            )}
          </div>
          <div className="text-right">
            <p className="text-[10px] text-slate-400">총 결제금액</p>
            <p className="text-base font-bold text-slate-900">{formatCurrency(order.totalAmount)}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ── 페이지네이션 컴포넌트 ── */
function Pagination({
  page, totalPages, totalElements, onPageChange,
}: {
  page: number; totalPages: number; totalElements: number; onPageChange: (p: number) => void;
}) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between pt-4 border-t border-slate-100 mt-4">
      <span className="text-[11px] text-slate-400">
        총 {totalElements.toLocaleString("ko-KR")}건
      </span>
      <div className="flex items-center gap-1.5">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="p-1.5 rounded-lg border border-slate-200 text-slate-500 hover:bg-slate-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft size={14} />
        </button>
        <span className="text-[12px] font-medium text-slate-700 px-2">
          {page + 1} / {totalPages}
        </span>
        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="p-1.5 rounded-lg border border-slate-200 text-slate-500 hover:bg-slate-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
}

/* ── 메인 테이블 ── */
export function OrderTable() {
  const [status, setStatus] = useState<OrderStatus | "ALL">("ALL");
  const [searchInput, setSearchInput] = useState("");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<OrderResponse | null>(null);

  const debouncedQuery = useDebounce(searchInput, 300);

  // 필터 변경 시 페이지 초기화
  const handleStatusChange = useCallback((s: OrderStatus | "ALL") => {
    setStatus(s);
    setPage(0);
  }, []);

  // 검색어 변경 시 페이지 초기화
  useEffect(() => {
    setPage(0);
  }, [debouncedQuery]);

  const swrKey = apiUrls.orderSearch({
    q: debouncedQuery || undefined,
    status: status === "ALL" ? undefined : status,
    page,
    size: PAGE_SIZE,
  });

  const { data, isLoading } = useSWR<OrderSearchResponse>(swrKey, fetcher, {
    refreshInterval: 10000,
  });

  const orders = data?.content ?? [];
  const totalElements = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Orders</CardTitle>
            <span className="text-[11px] text-slate-400 font-medium">
              {totalElements.toLocaleString("ko-KR")} results
            </span>
          </div>
        </CardHeader>
        <CardContent>
          {/* Search Bar */}
          <div className="relative mb-4">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="고객명, 주소, 지역 검색..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full pl-9 pr-4 py-2.5 text-[13px] text-slate-700 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 placeholder:text-slate-400 transition-all"
            />
            {searchInput && (
              <button
                onClick={() => setSearchInput("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-300 hover:text-slate-500"
              >
                <X size={14} />
              </button>
            )}
          </div>

          {/* Status Tabs */}
          <div className="flex flex-wrap gap-1 mb-5 p-1 bg-slate-100/80 rounded-lg">
            {STATUSES.map((s) => (
              <button
                key={s}
                onClick={() => handleStatusChange(s)}
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
                ) : orders.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-sm text-slate-400">
                      {debouncedQuery ? `"${debouncedQuery}" 검색 결과가 없습니다` : "해당 상태의 주문이 없습니다"}
                    </td>
                  </tr>
                ) : (
                  orders.map((o) => (
                    <tr
                      key={o.orderId}
                      onClick={() => setSelected(o)}
                      className="hover:bg-slate-50/80 transition-colors cursor-pointer group"
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
                      <td className="py-3">
                        <div className="flex items-center gap-1">
                          <span className="text-[12px] text-slate-500">{formatTime(o.orderTime)}</span>
                          <ChevronRight size={14} className="text-slate-300 group-hover:text-slate-500 transition-colors" />
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        </CardContent>
      </Card>

      {selected && <OrderDetailModal order={selected} onClose={() => setSelected(null)} />}
    </>
  );
}
