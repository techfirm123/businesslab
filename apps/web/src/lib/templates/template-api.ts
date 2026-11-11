
import { apiRequest } from "@/lib/api/client";
import type { BusinessTemplate } from "@/lib/templates/template-types";

export const templateApi = {
  async list(): Promise<BusinessTemplate[]> {
    return apiRequest<BusinessTemplate[]>("/api/v1/templates", {
      method: "GET",
    });
  },
};
