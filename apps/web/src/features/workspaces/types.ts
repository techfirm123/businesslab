export type WorkspaceRole =
  | "OWNER"
  | "ADMIN"
  | "MEMBER"
  | "VIEWER";

export type WorkspaceStatus =
  | "ACTIVE"
  | "ARCHIVED";

export interface Workspace {
  id: string;
  name: string;
  storageMode: "CLOUD";
  status: WorkspaceStatus;
  membershipRole: WorkspaceRole;
  membershipStatus: "ACTIVE" | "SUSPENDED" | "REMOVED";
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceCreateRequest {
  name: string;
}
