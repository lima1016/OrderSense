import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const PUBLIC_PATHS = ["/login", "/signup"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // API 요청은 통과
  if (pathname.startsWith("/api/") || pathname.startsWith("/_next/")) {
    return NextResponse.next();
  }

  // 클라이언트 사이드 토큰은 미들웨어에서 직접 확인 불가 (localStorage)
  // 실제 보호는 AuthProvider에서 처리, 여기서는 기본 라우팅만 담당
  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
