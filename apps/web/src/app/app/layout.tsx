
"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";

import {
  AuthProvider,
  useAuth,
} from "@/features/auth/auth-provider";
import { ApplicationShell } from "@/components/application/application-shell";

interface AppLayoutProps {
  children: ReactNode;
}

function ProtectedApplication({
  children,
}: AppLayoutProps) {
  const router = useRouter();
  const { status, error, refresh } = useAuth();

  useEffect(() => {
    if (status === "unauthenticated") {
      router.replace("/login");
    }
  }, [status, router]);

  if (status === "loading" || status === "unauthenticated") {
    return (
      <main className="app-loading-screen">
        <div className="app-loading-brand">
          <span className="app-brand-mark">B</span>
          <span>
            Business<span>Lab</span>
          </span>
        </div>
        <div className="app-loading-spinner" />
        <p>
          {status === "unauthenticated"
            ? "Returning to sign in..."
            : "Preparing your BusinessLab environment..."}
        </p>
      </main>
    );
  }

  if (status === "error") {
    return (
      <main className="app-loading-screen">
        <div className="app-error-card">
          <span className="app-error-mark">!</span>
          <h1>We couldn&apos;t verify your session.</h1>
          <p>{error}</p>
          <button type="button" onClick={() => void refresh()}>
            Try again
          </button>
        </div>
      </main>
    );
  }

  return <ApplicationShell>{children}</ApplicationShell>;
}

export default function AppLayout({
  children,
}: AppLayoutProps) {
  return (
    <AuthProvider>
      <ProtectedApplication>{children}</ProtectedApplication>
    </AuthProvider>
  );
}
