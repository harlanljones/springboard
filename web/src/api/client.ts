/** The typed contract with the Spring Boot API. One module owns every fetch. */

export type Item = {
  id: number;
  name: string;
  status: string;
};

/** Base path is relative by default: dev uses Vite's proxy, prod serves SPA and API from one origin. */
const BASE = import.meta.env?.VITE_API_BASE ?? "/api";

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new ApiError(response.status, `GET ${path} failed with ${response.status}`);
  }

  return (await response.json()) as T;
}

export function listItems(): Promise<Item[]> {
  return request<Item[]>("/items");
}

export function getItem(id: number | string): Promise<Item> {
  return request<Item>(`/items/${id}`);
}
