import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{ts,tsx}",
    "./components/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: "#2563eb", foreground: "#ffffff" },
        destructive: { DEFAULT: "#dc2626", foreground: "#ffffff" },
        warning: { DEFAULT: "#d97706", foreground: "#ffffff" },
        success: { DEFAULT: "#16a34a", foreground: "#ffffff" },
        muted: { DEFAULT: "#f1f5f9", foreground: "#64748b" },
        card: { DEFAULT: "#ffffff", foreground: "#0f172a" },
        sidebar: { DEFAULT: "#0f172a", foreground: "#e2e8f0" },
      },
    },
  },
  plugins: [],
};

export default config;
