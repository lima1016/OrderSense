"use client";

import { useAuth } from "@/components/providers/auth-provider";
import { User, Shield, Mail, Clock, Settings, Bell } from "lucide-react";

export function WelcomeBanner() {
  const { user } = useAuth();
  if (!user) return null;

  const now = new Date();
  const hour = now.getHours();
  const greeting = hour < 12 ? "좋은 아침이에요" : hour < 18 ? "안녕하세요" : "수고하셨습니다";
  const dateStr = now.toLocaleDateString("ko-KR", {
    year: "numeric", month: "long", day: "numeric", weekday: "long",
  });
  const timeStr = now.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" });

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 via-slate-800 to-blue-900 p-6">
      {/* Background pattern */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-0 right-0 w-64 h-64 bg-blue-400 rounded-full -translate-y-1/2 translate-x-1/3 blur-3xl" />
        <div className="absolute bottom-0 left-0 w-48 h-48 bg-violet-400 rounded-full translate-y-1/2 -translate-x-1/4 blur-3xl" />
      </div>

      <div className="relative flex items-center justify-between">
        {/* Left: User info */}
        <div className="flex items-center gap-5">
          {/* Avatar */}
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-500 flex items-center justify-center shadow-lg shadow-blue-500/25">
            <span className="text-xl font-bold text-white">{user.name.charAt(0).toUpperCase()}</span>
          </div>

          <div>
            <p className="text-blue-200 text-[13px] font-medium">{greeting},</p>
            <h2 className="text-xl font-bold text-white mt-0.5">{user.name}</h2>
            <div className="flex items-center gap-4 mt-2">
              <div className="flex items-center gap-1.5 text-[12px] text-slate-400">
                <Mail size={12} />
                <span>{user.email}</span>
              </div>
              <div className="flex items-center gap-1.5 text-[12px]">
                <Shield size={12} className={user.role === "ADMIN" ? "text-amber-400" : "text-blue-400"} />
                <span className={user.role === "ADMIN" ? "text-amber-400 font-semibold" : "text-blue-400 font-medium"}>
                  {user.role}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Right: Date & quick actions */}
        <div className="text-right">
          <div className="flex items-center gap-1.5 text-slate-400 text-[12px] justify-end mb-1">
            <Clock size={12} />
            <span>{dateStr}</span>
          </div>
          <p className="text-2xl font-bold text-white tabular-nums">{timeStr}</p>
          <div className="flex items-center gap-2 mt-3 justify-end">
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/10 text-[11px] text-slate-300 font-medium">
              <div className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-400" />
              </div>
              시스템 정상
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
