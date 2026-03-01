"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, ShoppingBag, BarChart3, Activity, LogOut, Zap } from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuth } from "@/components/providers/auth-provider";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/dashboard/orders", label: "Orders", icon: ShoppingBag },
  { href: "/dashboard/analytics", label: "Analytics", icon: BarChart3 },
  { href: "/dashboard/monitoring", label: "Monitoring", icon: Activity },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  return (
    <aside className="w-64 bg-slate-950 text-slate-300 flex flex-col shrink-0">
      {/* Logo */}
      <div className="p-6 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center">
            <Zap size={18} className="text-white" />
          </div>
          <div>
            <h1 className="text-base font-bold text-white tracking-tight">OrderSense</h1>
            <p className="text-[11px] text-slate-500">Operations</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 space-y-0.5">
        <p className="px-3 pt-4 pb-2 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Menu</p>
        {navItems.map(({ href, label, icon: Icon }) => {
          const active = href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(href);
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex items-center gap-3 px-3 py-2 rounded-lg text-[13px] font-medium transition-all duration-150 relative",
                active
                  ? "bg-slate-800/80 text-white"
                  : "text-slate-400 hover:bg-slate-800/40 hover:text-slate-200"
              )}
            >
              {active && (
                <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 bg-blue-500 rounded-r-full" />
              )}
              <Icon size={16} strokeWidth={active ? 2 : 1.5} />
              {label}
            </Link>
          );
        })}
      </nav>

      {/* User */}
      {user && (
        <div className="p-3 mx-3 mb-3 rounded-lg bg-slate-900/50 border border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-blue-600/20 flex items-center justify-center text-blue-400 text-xs font-bold">
              {user.name.charAt(0).toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-slate-200 truncate">{user.name}</p>
              <p className="text-[11px] text-slate-500 truncate">{user.email}</p>
            </div>
            <button
              onClick={logout}
              className="p-1.5 rounded-md text-slate-500 hover:text-red-400 hover:bg-slate-800 transition-colors"
              title="Logout"
            >
              <LogOut size={14} />
            </button>
          </div>
        </div>
      )}
    </aside>
  );
}
