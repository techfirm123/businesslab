export interface Business {
  id: string;
  workspaceId: string;
  templateId: string;
  templateVersion: string;
  name: string;
  description: string;
  status: string;
  configuration: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface BusinessCreateRequest {
  templateId: string;
  templateVersion: string;
  name: string;
  description: string;
  configuration: Record<string, unknown>;
}
