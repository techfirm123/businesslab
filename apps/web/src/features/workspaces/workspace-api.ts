import {
  apiPostWithCsrf,
  apiRequest,
} from "@/lib/api/client";

import type {
  Workspace,
  WorkspaceCreateRequest,
} from "@/features/workspaces/types";

export const workspaceApi = {
  async list(): Promise<Workspace[]> {
    return apiRequest<Workspace[]>("/api/v1/workspaces", {
      method: "GET",
    });
  },

  async create(
    request: WorkspaceCreateRequest,
  ): Promise<Workspace> {
    return apiPostWithCsrf<Workspace>(
      "/api/v1/workspaces",
      request,
    );
  },
};
