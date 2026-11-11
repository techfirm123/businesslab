
"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

import { authApi } from "@/features/auth/auth-api";
import { AuthShell } from "@/features/auth/auth-shell";

interface LoginFormProps {
  registrationComplete: boolean;
}

export function LoginForm({
  registrationComplete,
}: LoginFormProps) {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError(null);
    setSubmitting(true);

    try {
      await authApi.login({
        email: email.trim(),
        password,
      });

      router.replace("/app");
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to sign in. Please try again.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthShell
      mode="login"
      eyebrow="WELCOME BACK"
      title="Sign in to your world."
      description="Pick up where your business ideas left off."
    >
      {registrationComplete && (
        <div className="auth-success" role="status">
          Your account has been created. Sign in to continue.
        </div>
      )}

      {error && (
        <div className="auth-error" role="alert">
          <span className="auth-alert-icon">!</span>
          {error}
        </div>
      )}

      <form className="auth-form" onSubmit={handleSubmit}>
        <label className="auth-field">
          <span>Email address</span>
          <input
            type="email"
            name="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            maxLength={320}
            required
          />
        </label>

        <label className="auth-field">
          <span>Password</span>
          <div className="auth-password-field">
            <input
              type={showPassword ? "text" : "password"}
              name="password"
              autoComplete="current-password"
              placeholder="Enter your password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              maxLength={72}
              required
            />

            <button
              type="button"
              className="auth-password-toggle"
              onClick={() => setShowPassword((value) => !value)}
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>
        </label>

        <button
          className="auth-submit"
          type="submit"
          disabled={submitting}
        >
          <span>
            {submitting ? "Signing in..." : "Sign in to BusinessLab"}
          </span>
          <span aria-hidden="true">↗</span>
        </button>
      </form>

      <div className="auth-form-note">
        <span className="auth-note-dot" />
        Your business workspace is private to your account.
      </div>
    </AuthShell>
  );
}
