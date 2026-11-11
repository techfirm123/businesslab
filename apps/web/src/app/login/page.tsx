
import type { Metadata } from "next";

import { LoginForm } from "@/features/auth/login-form";

export const metadata: Metadata = {
  title: "Sign in",
  description: "Sign in to your BusinessLab account.",
};

interface LoginPageProps {
  searchParams: Promise<{
    registration?: string | string[];
  }>;
}

export default async function LoginPage({
  searchParams,
}: LoginPageProps) {
  const params = await searchParams;

  return (
    <LoginForm
      registrationComplete={params.registration === "complete"}
    />
  );
}
