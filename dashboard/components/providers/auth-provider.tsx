"use client";

import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import {
  type User,
  type AuthResponse,
  getUser,
  getAccessToken,
  setAuth,
  clearAuth,
  apiLogin,
  apiSignup,
  apiLogout,
} from "@/lib/auth";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, name: string, phone?: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    setUser(getUser());
    setLoading(false);
  }, []);

  const handleAuthResponse = useCallback(
    (data: AuthResponse) => {
      setAuth(data);
      setUser({ email: data.email, name: data.name, role: data.role });
      router.push("/dashboard");
    },
    [router]
  );

  const login = useCallback(
    async (email: string, password: string) => {
      const data = await apiLogin(email, password);
      handleAuthResponse(data);
    },
    [handleAuthResponse]
  );

  const signup = useCallback(
    async (email: string, password: string, name: string, phone?: string) => {
      const data = await apiSignup(email, password, name, phone);
      handleAuthResponse(data);
    },
    [handleAuthResponse]
  );

  const logout = useCallback(async () => {
    const token = getAccessToken();
    if (token) await apiLogout(token);
    clearAuth();
    setUser(null);
    router.push("/login");
  }, [router]);

  return (
    <AuthContext.Provider value={{ user, loading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
