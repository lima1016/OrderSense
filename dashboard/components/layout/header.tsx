"use client";

import { usePathname } from "next/navigation";
import { Shield } from "lucide-react";
import { useAuth } from "@/components/providers/auth-provider";

const pageTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/dashboard/orders": "Orders",
  "/dashboard/analytics": "Analytics",
  "/dashboard/monitoring": "Monitoring",
};

export function Header() {
  const pathname = usePathname();
  const { user } = useAuth();
  const title = pageTitles[pathname] || "Dashboard";

  return (
    <header className="h-14 border-b border-slate-200 bg-white/80 backdrop-blur-sm px-6 flex items-center justify-between shrink-0">
      <div className="flex items-center gap-4">
        <h2 className="text-[15px] font-semibold text-slate-900">{title}</h2>
      </div>
      <div className="flex items-center gap-3">
        {/* Live */}
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-200">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
          <span className="text-[11px] font-medium text-emerald-700">Live</span>
        </div>
        {/* User badge */}
        {user && (
          <div className="flex items-center gap-2 pl-3 border-l border-slate-200">
            <div className="w-7 h-7 rounded-full bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center">
              <span className="text-[11px] font-bold text-white">{user.name.charAt(0).toUpperCase()}</span>
            </div>
            <div className="hidden sm:block">
              <p className="text-[12px] font-semibold text-slate-800 leading-none">{user.name}</p>
              <div className="flex items-center gap-1 mt-0.5">
                <Shield size={9} className={user.role === "ADMIN" ? "text-amber-500" : "text-blue-400"} />
                <span className="text-[10px] text-slate-400">{user.role}</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
