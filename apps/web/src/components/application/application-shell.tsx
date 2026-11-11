
"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState, type ReactNode } from "react";

import { useAuth } from "@/features/auth/auth-provider";
import { AppIcon } from "@/components/application/app-icon";

interface ApplicationShellProps {
  children: ReactNode;
}

const activeNavigation = [
  {
    href: "/app",
    label: "Overview",
    icon: "overview" as const,
  },
  {
    href: "/app/discover",
    label: "Discover",
    icon: "discover" as const,
  },
];

const upcomingNavigation = [
  { label: "My businesses", icon: "business" as const },
  { label: "Experiment lab", icon: "experiment" as const },
  { label: "Flow studio", icon: "studio" as const },
];

function getPageTitle(pathname: string): string {
  if (pathname.startsWith("/app/discover")) {
    return "Discover businesses";
  }

  return "Overview";
}

export function ApplicationShell({
  children,
}: ApplicationShellProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();

  const [loggingOut, setLoggingOut] = useState(false);
  const [logoutError, setLogoutError] = useState<string | null>(null);

  async function handleLogout() {
    setLoggingOut(true);
    setLogoutError(null);

    try {
      await logout();
      router.replace("/login");
    } catch (error) {
      setLogoutError(
        error instanceof Error
          ? error.message
          : "Unable to sign out. Please try again.",
      );
      setLoggingOut(false);
    }
  }

  const initial = user?.displayName.charAt(0).toUpperCase() ?? "B";

  return (
    <div className="app-frame">
      <aside className="app-sidebar">
        <Link href="/app" className="app-brand">
          <span className="app-brand-mark">B</span>
          <span>
            Business<span>Lab</span>
          </span>
        </Link>

        <div className="sidebar-workspace-label">
          YOUR ENVIRONMENT
        </div>

        <nav className="sidebar-navigation" aria-label="Application navigation">
          <div className="sidebar-nav-group">
            <span className="sidebar-section-label">WORKSPACE</span>

            {activeNavigation.map((item) => {
              const active =
                item.href === "/app"
                  ? pathname === "/app"
                  : pathname.startsWith(item.href);

              return (
                <Link
                  href={item.href}
                  className={`sidebar-nav-item${active ? " active" : ""}`}
                  key={item.href}
                  aria-current={active ? "page" : undefined}
                >
                  <AppIcon name={item.icon} />
                  <span>{item.label}</span>
                  {active && <span className="nav-active-indicator" />}
                </Link>
              );
            })}
          </div>

          <div className="sidebar-nav-group">
            <span className="sidebar-section-label">COMING TO BUSINESSLAB</span>

            {upcomingNavigation.map((item) => (
              <div
                className="sidebar-nav-item disabled"
                key={item.label}
                aria-disabled="true"
                title={`${item.label} is being developed`}
              >
                <AppIcon name={item.icon} />
                <span>{item.label}</span>
                <span className="nav-soon-label">SOON</span>
              </div>
            ))}
          </div>
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-foundation-card">
            <span className="foundation-card-overline">
              THE BUSINESSLAB APPROACH
            </span>
            <strong>Build with curiosity.</strong>
            <p>
              Experiment with ideas. Understand what makes a business work.
            </p>
            <span className="foundation-card-coordinate">BL / 001</span>
          </div>

          <div className="sidebar-account">
            <div className="account-avatar">{initial}</div>
            <div className="account-details">
              <strong>{user?.displayName}</strong>
              <span>{user?.email}</span>
            </div>
          </div>
        </div>
      </aside>

      <div className="app-main-area">
        <header className="app-topbar">
          <div className="app-breadcrumb">
            <span>BUSINESSLAB</span>
            <span className="breadcrumb-separator">/</span>
            <strong>{getPageTitle(pathname).toUpperCase()}</strong>
          </div>

          <div className="app-topbar-actions">
            <span className="app-session-indicator">
              <span className="auth-note-dot" />
              SESSION ACTIVE
            </span>

            <details className="account-menu">
              <summary aria-label="Open account menu">
                <span className="account-avatar account-avatar-small">
                  {initial}
                </span>
              </summary>

              <div className="account-menu-popover">
                <div className="account-menu-identity">
                  <strong>{user?.displayName}</strong>
                  <span>{user?.email}</span>
                </div>
                <div className="account-menu-divider" />
                <button
                  type="button"
                  onClick={handleLogout}
                  disabled={loggingOut}
                >
                  {loggingOut ? "Signing out..." : "Sign out"}
                </button>
              </div>
            </details>
          </div>
        </header>

        {logoutError && (
          <div className="app-action-error" role="alert">
            {logoutError}
          </div>
        )}

        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}
