"use client";

import { OrderTable } from "@/components/orders/order-table";
import { OrderStatusChart } from "@/components/orders/order-status-chart";

export default function OrdersPage() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <OrderTable />
        </div>
        <div>
          <OrderStatusChart />
        </div>
      </div>
    </div>
  );
}
