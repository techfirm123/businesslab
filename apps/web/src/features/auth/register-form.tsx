
"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

import { authApi } from "@/features/auth/auth-api";
import { AuthShell } from "@/features/auth/auth-shell";

export function RegisterForm() {
  const router = useRouter();

  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError("Your passwords do not match.");
      return;
    }

    setSubmitting(true);

    try {
      await authApi.register({
        email: email.trim(),
        displayName: displayName.trim(),
        password,
      });

      router.replace("/login?registration=complete");
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to create your account. Please try again.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthShell
      mode="register"
      eyebrow="YOUR BUSINESS JOURNEY STARTS HERE"
      title="Create your account."
      description="Set up your identity and start exploring a world of businesses."
    >
      {error && (
        <div className="auth-error" role="alert">
          <span className="auth-alert-icon">!</span>
          {error}
        </div>
      )}

      <form className="auth-form auth-register-form" onSubmit={handleSubmit}>
        <label className="auth-field">
          <span>Your name</span>
          <input
            type="text"
            name="displayName"
            autoComplete="name"
            placeholder="How should we address you?"
            value={displayName}
            onChange={(event) => setDisplayName(event.target.value)}
            minLength={1}
            maxLength={100}
            required
          />
        </label>

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
              name="new-password"
              autoComplete="new-password"
              placeholder="Create a strong passphrase"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={12}
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
          <small>Use at least 12 characters. Passphrases work well.</small>
        </label>

        <label className="auth-field">
          <span>Confirm password</span>
          <input
            type="password"
            name="confirm-password"
            autoComplete="new-password"
            placeholder="Enter your password again"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            minLength={12}
            maxLength={72}
            required
          />
        </label>

        <button
          className="auth-submit"
          type="submit"
          disabled={submitting}
        >
          <span>
            {submitting ? "Creating account..." : "Create your account"}
          </span>
          <span aria-hidden="true">↗</span>
        </button>
      </form>

      <div className="auth-form-note">
        <span className="auth-note-dot" />
        Your account is created securely with BusinessLab.
      </div>
    </AuthShell>
  );
}
