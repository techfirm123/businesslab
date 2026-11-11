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

import { workspaceApi } from "@/features/workspaces/workspace-api";
import type { Workspace } from "@/features/workspaces/types";

const ACTIVE_WORKSPACE_STORAGE_KEY =
  "businesslab.activeWorkspaceId";

interface WorkspaceContextValue {
  workspaces: Workspace[];
  activeWorkspace: Workspace | null;
  loading: boolean;
  creating: boolean;
  error: string | null;
  refreshWorkspaces: () => Promise<void>;
  createWorkspace: (name: string) => Promise<Workspace>;
  setActiveWorkspaceId: (workspaceId: string) => void;
}

const WorkspaceContext =
  createContext<WorkspaceContextValue | null>(null);

interface WorkspaceProviderProps {
  children: ReactNode;
}

function readPreferredWorkspaceId(): string | null {
  try {
    return window.localStorage.getItem(
      ACTIVE_WORKSPACE_STORAGE_KEY,
    );
  } catch {
    return null;
  }
}

function savePreferredWorkspaceId(
  workspaceId: string | null,
): void {
  try {
    if (workspaceId) {
      window.localStorage.setItem(
        ACTIVE_WORKSPACE_STORAGE_KEY,
        workspaceId,
      );
    } else {
      window.localStorage.removeItem(
        ACTIVE_WORKSPACE_STORAGE_KEY,
      );
    }
  } catch {
    // Workspace selection remains usable when browser storage is unavailable.
  }
}

function resolveActiveWorkspace(
  workspaces: Workspace[],
): Workspace | null {
  const preferredId = readPreferredWorkspaceId();

  return (
    workspaces.find((workspace) => workspace.id === preferredId) ??
    workspaces[0] ??
    null
  );
}

export function WorkspaceProvider({
  children,
}: WorkspaceProviderProps) {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [activeWorkspaceId, setActiveWorkspaceIdState] =
    useState<string | null>(null);

  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /*
   * Initial loading is performed directly in the effect.
   * State updates happen only after the asynchronous request settles.
   */
  useEffect(() => {
    let active = true;

    void workspaceApi.list()
      .then((result) => {
        if (!active) {
          return;
        }

        const selectedWorkspace = resolveActiveWorkspace(result);
        const selectedId = selectedWorkspace?.id ?? null;

        setWorkspaces(result);
        setActiveWorkspaceIdState(selectedId);
        savePreferredWorkspaceId(selectedId);
        setError(null);
      })
      .catch((requestError: unknown) => {
        if (!active) {
          return;
        }

        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load your workspaces.",
        );
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const refreshWorkspaces = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await workspaceApi.list();

      const selectedWorkspace = resolveActiveWorkspace(result);
      const selectedId = selectedWorkspace?.id ?? null;

      setWorkspaces(result);
      setActiveWorkspaceIdState(selectedId);
      savePreferredWorkspaceId(selectedId);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load your workspaces.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  const setActiveWorkspaceId = useCallback(
    (workspaceId: string) => {
      const exists = workspaces.some(
        (workspace) => workspace.id === workspaceId,
      );

      if (!exists) {
        return;
      }

      setActiveWorkspaceIdState(workspaceId);
      savePreferredWorkspaceId(workspaceId);
    },
    [workspaces],
  );

  const createWorkspace = useCallback(
    async (name: string): Promise<Workspace> => {
      setCreating(true);

      try {
        const created = await workspaceApi.create({
          name: name.trim(),
        });

        setWorkspaces((current) => [
          created,
          ...current.filter(
            (workspace) => workspace.id !== created.id,
          ),
        ]);

        setActiveWorkspaceIdState(created.id);
        savePreferredWorkspaceId(created.id);

        return created;
      } finally {
        setCreating(false);
      }
    },
    [],
  );

  const activeWorkspace = useMemo(
    () =>
      workspaces.find(
        (workspace) => workspace.id === activeWorkspaceId,
      ) ?? null,
    [workspaces, activeWorkspaceId],
  );

  const value = useMemo<WorkspaceContextValue>(
    () => ({
      workspaces,
      activeWorkspace,
      loading,
      creating,
      error,
      refreshWorkspaces,
      createWorkspace,
      setActiveWorkspaceId,
    }),
    [
      workspaces,
      activeWorkspace,
      loading,
      creating,
      error,
      refreshWorkspaces,
      createWorkspace,
      setActiveWorkspaceId,
    ],
  );

  return (
    <WorkspaceContext.Provider value={value}>
      {children}
    </WorkspaceContext.Provider>
  );
}

export function useWorkspaces(): WorkspaceContextValue {
  const context = useContext(WorkspaceContext);

  if (!context) {
    throw new Error(
      "useWorkspaces must be used inside a WorkspaceProvider.",
    );
  }

  return context;
}
