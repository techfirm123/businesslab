
import { apiRequest } from "@/lib/api/client";
import type {
  CsrfTokenResponse,
  LoginRequest,
  RegisterRequest,
  UserIdentity,
} from "@/features/auth/types";

async function getCsrfToken(): Promise<CsrfTokenResponse> {
  return apiRequest<CsrfTokenResponse>("/api/v1/auth/csrf", {
    method: "GET",
  });
}

async function csrfProtectedRequest<T>(
  path: string,
  method: "POST",
  body?: unknown,
): Promise<T> {
  const csrf = await getCsrfToken();

  const headers = new Headers();

  headers.set(csrf.headerName, csrf.token);

  return apiRequest<T>(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

export const authApi = {
  async register(request: RegisterRequest): Promise<UserIdentity> {
    return csrfProtectedRequest<UserIdentity>(
      "/api/v1/auth/register",
      "POST",
      request,
    );
  },

  async login(request: LoginRequest): Promise<UserIdentity> {
    return csrfProtectedRequest<UserIdentity>(
      "/api/v1/auth/login",
      "POST",
      request,
    );
  },

  async currentUser(): Promise<UserIdentity> {
    return apiRequest<UserIdentity>("/api/v1/auth/me", {
      method: "GET",
    });
  },

  async logout(): Promise<void> {
    await csrfProtectedRequest<void>(
      "/api/v1/auth/logout",
      "POST",
    );
  },
};
