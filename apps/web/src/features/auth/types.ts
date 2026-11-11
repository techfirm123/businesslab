
export interface UserIdentity {
  userId: string;
  email: string;
  displayName: string;
  status: "ACTIVE" | "SUSPENDED";
  createdAt: string;
}

export interface RegisterRequest {
  email: string;
  displayName: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface CsrfTokenResponse {
  token: string;
  headerName: string;
  parameterName: string;
}
