import {
  apiPostWithCsrf,
  apiRequest,
} from "@/lib/api/client";

import type {
  Business,
  BusinessCreateRequest,
} from "@/features/businesses/business-types";

export const businessApi = {
  async list(workspaceId: string): Promise<Business[]> {
    return apiRequest<Business[]>(
      `/api/v1/workspaces/${encodeURIComponent(workspaceId)}/businesses`,
      {
        method: "GET",
      },
    );
  },

  async create(
    workspaceId: string,
    request: BusinessCreateRequest,
  ): Promise<Business> {
    return apiPostWithCsrf<Business>(
      `/api/v1/workspaces/${encodeURIComponent(workspaceId)}/businesses`,
      request,
    );
  },
};
