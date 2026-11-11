
import Link from "next/link";
import type { ReactNode } from "react";

interface AuthShellProps {
  mode: "login" | "register";
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
}

export function AuthShell({
  mode,
  eyebrow,
  title,
  description,
  children,
}: AuthShellProps) {
  const isLogin = mode === "login";

  return (
    <main className="auth-screen">
      <aside className="auth-story">
        <Link
          href="/"
          className="auth-brand"
          aria-label="BusinessLab home"
        >
          <span className="auth-brand-mark">B</span>
          <span>
            Business<span className="auth-brand-accent">Lab</span>
          </span>
        </Link>

        <div className="auth-story-content">
          <span className="auth-story-eyebrow">
            THE BUSINESS EXPERIMENTATION PLATFORM
          </span>

          <h1>
            Ideas are easy.
            <br />
            <span>Understanding business</span>
            <br />
            is the experiment.
          </h1>

          <p>
            Create a virtual business, explore how it works,
            make decisions and discover what drives its outcomes.
            All in a space designed for learning by doing.
          </p>

          <div
            className="auth-world-visual"
            aria-hidden="true"
          >
            <div className="auth-world-ring auth-ring-one" />
            <div className="auth-world-ring auth-ring-two" />
            <div className="auth-world-ring auth-ring-three" />

            <div className="auth-world-core">
              <span>B</span>
            </div>

            <div className="auth-world-node auth-node-one">
              <span>01</span>
              <strong>BUILD</strong>
            </div>

            <div className="auth-world-node auth-node-two">
              <span>02</span>
              <strong>OPERATE</strong>
            </div>

            <div className="auth-world-node auth-node-three">
              <span>03</span>
              <strong>EXPERIMENT</strong>
            </div>

            <div className="auth-world-caption">
              ONE WORLD / MANY POSSIBILITIES
            </div>
          </div>
        </div>

        <div className="auth-story-footer">
          <span className="auth-live-dot" />
          A new way to explore business.
        </div>
      </aside>

      <section className="auth-main">
        <div className="auth-main-top">
          <Link href="/" className="auth-mobile-brand">
            <span className="auth-brand-mark">B</span>
            <span>
              Business<span className="auth-brand-accent">Lab</span>
            </span>
          </Link>

          <Link href="/" className="auth-back-link">
            Back to BusinessLab <span aria-hidden="true">↗</span>
          </Link>
        </div>

        <div className="auth-card">
          <div className="auth-card-heading">
            <span className="auth-eyebrow">{eyebrow}</span>
            <h2>{title}</h2>
            <p>{description}</p>
          </div>

          {children}

          <div className="auth-switch">
            {isLogin ? (
              <>
                New to BusinessLab?{" "}
                <Link href="/register">Create an account</Link>
              </>
            ) : (
              <>
                Already have an account?{" "}
                <Link href="/login">Sign in</Link>
              </>
            )}
          </div>
        </div>

        <footer className="auth-main-footer">
          <span>Build. Operate. Experiment. Understand.</span>
          <span>BUSINESSLAB / 001</span>
        </footer>
      </section>
    </main>
  );
}
