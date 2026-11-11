
const API_BASE_URL = (
  process.env.NEXT_PUBLIC_BUSINESSLAB_API_URL ??
  "http://localhost:8080"
).replace(/\/+$/, "");

export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  error?: string;
  code?: string;
  message?: string;
  path?: string;
  details?: Array<{
    field?: string;
    code?: string;
    message?: string;
  }>;
}

export class ApiRequestError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
    public readonly details: ApiErrorPayload["details"] = [],
  ) {
    super(message);
    this.name = "ApiRequestError";
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

async function readResponseBody(
  response: Response,
): Promise<unknown> {
  const contentType = response.headers.get("content-type") ?? "";

  if (!contentType.includes("application/json")) {
    return null;
  }

  try {
    return await response.json();
  } catch {
    return null;
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers);

  headers.set("Accept", "application/json");

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: "include",
      cache: "no-store",
    });
  } catch {
    throw new ApiRequestError(
      0,
      "API_UNAVAILABLE",
      "Unable to connect to BusinessLab. Please check that the API is running.",
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const body = await readResponseBody(response);

  if (!response.ok) {
    const payload = isRecord(body)
      ? (body as ApiErrorPayload)
      : {};

    throw new ApiRequestError(
      response.status,
      payload.code ?? "REQUEST_FAILED",
      payload.message ?? "The request could not be completed.",
      payload.details ?? [],
    );
  }

  return body as T;
}
