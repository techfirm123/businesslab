
"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import { ApiRequestError } from "@/lib/api/client";
import { authApi } from "@/features/auth/auth-api";
import type { UserIdentity } from "@/features/auth/types";

export type AuthStatus =
  | "loading"
  | "authenticated"
  | "unauthenticated"
  | "error";

interface AuthContextValue {
  user: UserIdentity | null;
  status: AuthStatus;
  error: string | null;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [user, setUser] = useState<UserIdentity | null>(null);
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setStatus("loading");
    setError(null);

    try {
      const currentUser = await authApi.currentUser();

      setUser(currentUser);
      setStatus("authenticated");
    } catch (requestError) {
      setUser(null);

      if (
        requestError instanceof ApiRequestError &&
        requestError.status === 401
      ) {
        setStatus("unauthenticated");
        return;
      }

      setStatus("error");
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to verify your BusinessLab session.",
      );
    }
  }, []);

  useEffect(() => {
    let active = true;

    void authApi.currentUser()
      .then((currentUser) => {
        if (!active) {
          return;
        }

        setUser(currentUser);
        setStatus("authenticated");
        setError(null);
      })
      .catch((requestError: unknown) => {
        if (!active) {
          return;
        }

        setUser(null);

        if (
          requestError instanceof ApiRequestError &&
          requestError.status === 401
        ) {
          setStatus("unauthenticated");
          return;
        }

        setStatus("error");
        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to verify your BusinessLab session.",
        );
      });

    return () => {
      active = false;
    };
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();

    setUser(null);
    setStatus("unauthenticated");
    setError(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      status,
      error,
      refresh,
      logout,
    }),
    [user, status, error, refresh, logout],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error(
      "useAuth must be used inside an AuthProvider.",
    );
  }

  return context;
}
