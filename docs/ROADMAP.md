# Roadmap

Each item states what "done" means. Nothing here is stubbed in code: if it isn't
implemented, it isn't in `src/` or `web/src/`. Order is the intended order, but
the acceptance criteria are what matter.

## 1. Persistence: PostgreSQL + Spring Data JPA

*Why:* the example slice proves the wire; real work needs storage.

- Add `spring-boot-starter-data-jpa` + the PostgreSQL driver; local database via
  `docker-compose.yml` (host `127.0.0.1` only).
- Replace `ItemService`'s in-memory list with a repository; `ItemsController`
  must not change (that is the test of the seam).
- Schema via `spring.jpa.hibernate.ddl-auto=update` for the starter only; add
  Flyway the first time a migration must be reversible.
- Add `spring-boot-testcontainers` (or `spring-boot-docker-compose`) and one
  integration test that exercises the repository against a real PostgreSQL
  container. `bun test` and `./mvnw test` must stay runnable with no Docker for
  the unit path.
- **Acceptance:** `./scripts/verify.sh` green, plus a documented
  `./mvnw -Dtest='*RepositoryIT' test` that runs against the container.

## 2. Container image

*Why:* the jar is the unit of deploy; a container makes that reproducible.

- Multi-stage `Dockerfile`: frontend build stage (bun) → Maven build stage
  (JDK 25) → `eclipse-temurin:25-jre` runtime, non-root user, `-XX:MaxRAMPercentage`.
- `.dockerignore` excluding `target/`, `web/node_modules/`, `web/dist/`, `.git/`.
- **Acceptance:** `docker build -t springboard .` then `docker run -p 8080:8080`
  and `./scripts/smoke.sh` passes against the container; image < 250 MB.

## 3. CI

*Why:* the gate is only real if it runs somewhere other than one laptop.

- GitHub Actions workflow on push/PR: `mise install`,
  `./scripts/verify.sh`, with the Maven and bun caches warmed.
- **Acceptance:** a red PR fails on a genuinely broken test (verify by breaking
  one deliberately), and a green PR is green in under 5 minutes.

## 4. Authentication

*Why:* the moment this serves a real user, `/api/**` needs a boundary.

- Spring Security with session cookies (same origin — no CORS) or OIDC if the
  deployment target is behind an identity provider; decide with a spike first.
- SPA: login route, 401 handling in `api/client.ts`, and a protected example
  endpoint.
- **Acceptance:** unauthenticated `/api/items` returns 401
  `application/problem+json`; an authenticated request returns 200; a Playwright
  or MockMvc test covers both.

## 5. Revisit TypeScript 7

*Why:* TS 7 (native, ~10x faster) is GA but unusable here yet.

- `typescript-eslint` 8.71 rejects TS 7.0 and tracks support from 7.1
  (<https://github.com/typescript-eslint/typescript-eslint/issues/10940>).
- The TS team's transition path is side-by-side installs
  (`@typescript/typescript6` aliased, TS 7 for `tsc`), but with bun's bin
  resolution the repo ends up with `.bin/tsc` pointing at a *transitive* TS 6
  build — a silent trap, so this repo pins TS 6 and waits.
- **Acceptance:** bump `typescript` to 7.x in `web/package.json` and get
  `bun run typecheck && bun run lint && bun run build` green without aliases.

## 6. Decide on Vite-in-Spring for HMR parity

*Why:* dev and prod currently load the SPA differently (Vite server vs jar).

- Evaluate `wimdeblauwe/vite-spring-boot` against the current proxy setup.
- **Acceptance:** a written decision (README/ADR-style note) with the tradeoff,
  or an implemented change that keeps `./scripts/verify.sh` green.

## 7. Observability and configuration hygiene

- Actuator: expose `health` with probes, add `info` metadata (build/version),
  optionally `/actuator/metrics` behind auth once item 4 lands.
- Structured JSON logging profile for production (`logback-spring.xml`), plain
  console for dev.
- **Acceptance:** `/actuator/health` reports `UP` in the packaged jar and the
  production profile emits one JSON line per request/error.
