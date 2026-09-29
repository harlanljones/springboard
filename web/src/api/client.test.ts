import { afterEach, describe, expect, test } from "bun:test";
import { ApiError, getItem, listItems, type Item } from "./client";

const realFetch = globalThis.fetch;

function stubFetch(response: Response | (() => Response)) {
  const calls: string[] = [];
  globalThis.fetch = ((input: RequestInfo | URL) => {
    calls.push(String(input));
    return Promise.resolve(typeof response === "function" ? response() : response);
  }) as unknown as typeof fetch;
  return calls;
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  globalThis.fetch = realFetch;
});

describe("api client", () => {
  test("listItems parses the item array and hits the proxied path", async () => {
    const items: Item[] = [{ id: 1, name: "First item", status: "active" }];
    const calls = stubFetch(json(items));

    await expect(listItems()).resolves.toEqual(items);
    expect(calls).toEqual(["/api/items"]);
  });

  test("getItem interpolates the id", async () => {
    const calls = stubFetch(json({ id: 7, name: "Seventh", status: "active" }));

    await expect(getItem(7)).resolves.toMatchObject({ id: 7 });
    expect(calls).toEqual(["/api/items/7"]);
  });

  test("a non-2xx response becomes an ApiError carrying the status", async () => {
    stubFetch(json({ status: 404 }, 404));

    const error = await getItem(999).catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(404);
  });
});
