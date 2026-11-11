
import type { Metadata } from "next";
import { RegisterForm } from "@/features/auth/register-form";

export const metadata: Metadata = {
  title: "Create your account",
  description: "Create your BusinessLab account.",
};

export default function RegisterPage() {
  return <RegisterForm />;
}
