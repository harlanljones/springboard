# springboard — Development Agent Guide

springboard is a full-stack starter: a Spring Boot 4.1 REST API and a React 19
single-page app in one repository, packaged into one deployable jar. The API
serves `/api/**` and the built SPA; the frontend is developed against Vite's dev
server with a proxy to the API, so there is one origin in both dev and prod and
no CORS configuration anywhere. The example slice (`Item`, `/api/items`, the
Items/ItemDetail pages) exists to prove the whole wire end to end — copy its
shape, don't grow it.

## Environment

| Tool | Version | Where it comes from |
|---|---|---|
| Java | 25.0.2 (LTS) | `.mise.toml` → `mise install` |
| Node | 26.10.0 | `.mise.toml` → `mise install` |
| bun | 1.4.2 | system |
| Maven | 3.9.16 | `./mvnw` wrapper (vendored, nothing installs globally) |
| Spring Boot | 4.1.1 | `pom.xml` parent |

- First run: `mise install` (installs the pinned Java + Node) and `cd web && bun install`.
- Ports: API `8080`, Vite dev server `5173`.
- Env vars: none required. `VITE_API_BASE` optionally overrides the frontend's API base
  (default `/api`, which is correct for both dev-proxied and jar-served builds).
- No database, no external service, no credentials are needed to run anything here.

## Commands (all verified on this checkout)

```bash
mise install                       # pinned Java 25 + Node 26
./mvnw test                        # backend tests (8 tests, no network)
./mvnw -Dtest=ItemsApiTest test    # a single backend test class
./mvnw spring-boot:run             # API only on :8080

cd web && bun install              # frontend deps (bun.lock is the lockfile)
cd web && bun run dev              # SPA only on :5173, proxies /api -> :8080
cd web && bun test                 # frontend tests (bun:test)
cd web && bun run typecheck        # tsc -b
cd web && bun run lint             # eslint (flat config)
cd web && bun run build            # -> web/dist

./mvnw package                     # REQUIRES web/dist: enforcer fails fast with the fix
./scripts/dev.sh                   # API + Vite together, Ctrl-C stops both
./scripts/smoke.sh                 # boots the packaged jar, asserts the HTTP contract
./scripts/verify.sh                # the full gate — run before claiming done
./dev                              # Hermes session for this repo (profile: springboard)
```

## Layout

| Path | What lives there |
|---|---|
| `src/main/java/ai/primeiq/springboard/` | Spring Boot app. Package-by-feature: `items/` is one example feature slice; `web/` holds web-layer configuration only. |
| `src/main/resources/application.yaml` | The only Spring config file (YAML). |
| `src/test/java/...` | Backend tests, mirroring the package they test. `src/test/resources/static/index.html` is a SPA-routing fixture, not a stub. |
| `web/` | The Vite + React + TypeScript app; owns its own `package.json`, `bun.lock`, tsconfigs and eslint config. New frontend deps go here, never at the repo root. |
| `scripts/` | `dev.sh` (both servers), `smoke.sh` (HTTP contract against the jar), `verify.sh` (the gate). |
| `docs/` | `BRIEF.md` (decisions + why), `ARCHITECTURE.md`, `CONVENTIONS.md`, `ROADMAP.md`. |
| `dev` | Hermes entrypoint: bootstraps the `springboard` profile, project skills, hooks, trust. |
| `.hermes/skills/` | Gitignored per-machine symlinks created by `./dev`. |

## Conventions

- Java: records for API values, constructor injection, one feature per package,
  errors thrown as `ResponseStatusException` and rendered as RFC 9457 problem
  details. Test specifics in `docs/CONVENTIONS.md`.
- Frontend: every HTTP call goes through `web/src/api/client.ts` — no `fetch`
  anywhere else. Components are function components; state is local `useState`.
- Comments explain *why* (the constraint or bug behind a rule), not *what*.
- Commits: Conventional Commits — `type(scope)?: subject` (`chore: scaffold …`,
  `feat(items): add pagination`), max 100 chars. This is enforced by the
  machine-wide `commit-msg` hook wired through `core.hooksPath`, so a plain
  imperative subject is rejected outright.

## Non-negotiable

- **The frontend builds itself.** Maven never installs Node, never runs npm/bun,
  never sees `web/src`. It only verifies `web/dist/index.html` exists
  (`maven-enforcer-plugin`) and copies it into the jar
  (`maven-resources-plugin`). Do not add `frontend-maven-plugin` or any other
  Maven build step for the frontend — see `docs/BRIEF.md` for the reasoning.
- **`/api/**` never returns HTML.** Unknown API paths stay JSON 404s
  (`application/problem+json`), and missing assets are never rewritten to
  `index.html`. `SpaWebConfig` holds both guard rails; `SpaRoutingTest` proves them.
- **No hidden toolchain.** Java, Node and Maven versions come from `.mise.toml`
  and `./mvnw`. Nothing in this repo depends on a globally installed Maven, Gradle
  or Node.
- **TypeScript stays on 6.x** until typescript-eslint supports TS 7
  (`docs/CONVENTIONS.md` has the link).
- **No database, no seeding, no auth** at this stage. Those are ROADMAP items
  with acceptance criteria; do not land them half-wired, and never as stubs.

## Where to look next

- `docs/BRIEF.md` — why this layout, including the packaging research behind it.
- `docs/ARCHITECTURE.md` — components, request flow, decisions.
- `docs/CONVENTIONS.md` — code and test patterns to copy.
- `docs/ROADMAP.md` — the next milestones and their acceptance criteria.
- `.hermes.md` — Hermes-specific conduct (skills, hooks) for this repo.
