# Architecture

Three parts, one artifact:

```
┌──────────────────────────── springboard jar (java -jar) ───────────────────────────┐
│                                                                                    │
│  React SPA (web/dist, copied into BOOT-INF/classes/static at prepare-package)       │
│      │  GET /            -> index.html                                             │
│      │  GET /items/42    -> index.html   (client-side route, SpaWebConfig resolver) │
│      │  GET /assets/*.js -> the file, or a JSON 404 if absent                       │
│      ▼                                                                             │
│  Spring Boot 4.1 (Tomcat, :8080)                                                   │
│      ├── ItemsController  GET /api/items, /api/items/{id}  ->  JSON (records)       │
│      │     └── ItemService (in-memory; the ROADMAP swap point for persistence)      │
│      ├── SpaWebConfig     /** resource handler + PathResourceResolver fallback      │
│      └── actuator         /actuator/health (+info) — used by scripts/dev.sh probes  │
└────────────────────────────────────────────────────────────────────────────────────┘
```

## Components and responsibilities

| Component | Owns | Must never do |
|---|---|---|
| `items/` (`Item`, `ItemService`, `ItemsController`) | The one example feature slice; the shape new features copy. | Touch persistence, security, or DTO/entity mapping that doesn't exist yet. |
| `web/SpaWebConfig` | Serving the built SPA and the client-side-route fallback. | Answer `/api/**` or `/actuator/**` with HTML; rewrite asset-looking paths to `index.html`. |
| `web/` (frontend) | UI, routing, and every HTTP call (`src/api/client.ts`). | Call `fetch` outside the api module; know about Maven or the jar. |
| `pom.xml` build steps | Verifying and copying `web/dist` into the jar. | Installing Node, running bun/npm, or compiling frontend sources. |
| `scripts/` | The developer's and CI's entry points: `dev.sh`, `smoke.sh`, `verify.sh`. | Hide failures (`dev.sh`/`smoke.sh` exit non-zero and print the log path). |

## Request flow (primary use case: the items list)

1. Browser (dev) loads `http://localhost:5173/` from Vite; (prod) loads `/` from the
   Spring app, which forwards to `index.html` (Boot's welcome page).
2. React Router renders `ItemsPage`, which calls `listItems()` in
   `web/src/api/client.ts` → `fetch("/api/items")`.
3. In dev, Vite's proxy forwards `/api/*` to `http://localhost:8080`; in prod the
   request never leaves the origin. The browser sees one origin in both cases, so
   there is no CORS configuration in the repo.
4. `ItemsController` → `ItemService` → `List<Item>` serialized as JSON.
5. A failure (e.g. unknown id) raises `ResponseStatusException`; because
   `spring.mvc.problemdetails.enabled=true`, the client receives an RFC 9457
   `application/problem+json` body with `status` and `detail`.

Deep links (step 2 of a `GET /items/42`) hit the same `index.html` through
`SpaWebConfig`'s resolver — which is why the resolver must distinguish "unknown
client route" from "unknown asset" and "unknown API path" (see below).

## Key decisions and why

- **One jar, frontend built outside Maven.** Rationale and research in
  `docs/BRIEF.md` ("Packaging research"); the invariant is restated in `AGENTS.md`.
  Maven's only frontend steps are the enforcer check and the copy.
- **Two guard rails in `SpaWebConfig`.** Returning `index.html` for *everything*
  is the common copy-paste version of an SPA fallback, and it breaks two things:
  a typo'd API path starts returning HTML (the frontend then parses HTML as JSON),
  and a missing asset returns HTML with a 200 (the browser reports a syntax error
  instead of a 404). Hence: reserved prefixes (`api/`, `actuator/`) and
  dot-containing paths always resolve to `null` → JSON 404. `SpaRoutingTest`
  asserts all four behaviours.
- **Problem details, not the legacy error map.** `spring.mvc.problemdetails.enabled:
  true` gives errors a stable, documented shape (`application/problem+json`) and
  keeps `detail` messages meaningful for the SPA's error path.
- **Vite, bun, and TypeScript pinned per repo.** `.mise.toml` pins Java and Node;
  `bun.lock` pins the frontend graph; `./mvnw` pins Maven. Nothing depends on
  what happens to be installed globally.
- **Test fixture, not a stub, for the SPA fallback.** `src/test/resources/static/index.html`
  is a sentinel page so `SpaRoutingTest` can prove deep links work in a backend-only
  checkout; the real `index.html` is produced by `bun run build`. Unimplemented
  behaviour lives in `docs/ROADMAP.md`, never as empty files or stubs.
