import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-semibold tracking-wide ring-1 ring-inset",
  {
    variants: {
      variant: {
        default: "bg-blue-50 text-blue-700 ring-blue-600/20",
        destructive: "bg-rose-50 text-rose-700 ring-rose-600/20",
        warning: "bg-amber-50 text-amber-700 ring-amber-600/20",
        success: "bg-emerald-50 text-emerald-700 ring-emerald-600/20",
        secondary: "bg-slate-50 text-slate-600 ring-slate-500/20",
        indigo: "bg-indigo-50 text-indigo-700 ring-indigo-600/20",
      },
    },
    defaultVariants: { variant: "default" },
  }
);

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement>, VariantProps<typeof badgeVariants> {}

export function Badge({ className, variant, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export function SeverityBadge({ severity }: { severity: number }) {
  const variant = severity >= 0.7 ? "destructive" : severity >= 0.4 ? "warning" : "secondary";
  const label = severity >= 0.7 ? "Critical" : severity >= 0.4 ? "Warning" : "Info";
  return <Badge variant={variant}>{label}</Badge>;
}

export function OrderStatusBadge({ status }: { status: string }) {
  const variantMap: Record<string, "default" | "warning" | "indigo" | "success" | "destructive"> = {
    PENDING: "default",
    ACCEPTED: "default",
    PREPARING: "warning",
    READY: "warning",
    PICKED_UP: "indigo",
    DELIVERING: "indigo",
    DELIVERED: "success",
    CANCELLED: "destructive",
  };
  return <Badge variant={variantMap[status] || "secondary"}>{status}</Badge>;
}
