# springboard — brief

Written before the first file existed, and kept as the record of *why* the repo
looks the way it does. If a decision here is later reversed, reverse it here
first, then in the code.

## Purpose

A starter for "Java Spring Boot + React web app": both halves in one repository,
one deployable artifact, every documented command actually working. The example
feature slice (items) is deliberately small; its job is to prove the full wire
(React → Vite proxy/jar → Spring → JSON → React) rather than to be a product.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Build tool | Maven via vendored `./mvnw` | Overwhelming default in Spring docs/tooling; wrapper means no global install and a pinned Maven (3.9.16). |
| Java | 25 LTS, pinned in `.mise.toml` | Spring Boot 4.1 supports Java 17–26, so 25 LTS is the newest in-matrix LTS. The machine's JDK 27 is *outside* Boot 4.1's supported range. |
| Spring Boot | 4.1.1 | Latest GA. Note the 4.x starter renames: `spring-boot-starter-webmvc` (not `-web`) and relocated test annotations (`org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`). |
| Frontend | Vite 8 + React 19 + TypeScript, `web/`, bun as package manager | Vite is the default React toolchain; the directory split keeps the two toolchains from contaminating each other. |
| State / routing | `useState` + React Router 7 | Enough to make the SPA fallback real; no state library until a ROADMAP item needs one. |
| Persistence | none | See non-goals. |
| Deployable | one jar serving SPA + API | Simpler ops for a starter: one process, one port, no CORS. |

## Packaging research (the one non-obvious call)

The stock answer for "React in a Spring Boot jar" is **`frontend-maven-plugin`**:
Maven downloads its *own* Node into `target/`, runs `npm install` + the frontend
build, then `maven-resources-plugin` copies `dist` into `target/classes/static`.
JHipster (9.4.0, Sep 2026) still bakes that in. Findings that made us diverge:

- `com.github.eirslett:frontend-maven-plugin` is at **1.15.1 (Sep 2024)** — the
  project describes itself as maintained but not actively developed.
- By design it ignores the machine's Node, so the repo gets **two toolchains**:
  the pinned one (`.mise.toml`) and a hidden one Maven downloads. They drift, and
  the frontend build then behaves differently under `./scripts/dev.sh` than under
  `./mvnw package`.
- Quarkus has a first-class equivalent (`quarkus-quinoa`, 2.9.1, Sep 2026,
  Red Hat/IBM-maintained); **Spring Boot has no official answer**, and the
  HMR-oriented alternatives (`wimdeblauwe/vite-spring-boot`) target templating,
  not a pure SPA.

**Chosen divergence:** the frontend builds itself, with its own pinned Node and
its own lockfile (`bun.lock`). Maven does exactly two things with it — fail fast
if `web/dist/index.html` is missing (`maven-enforcer-plugin`
`requireFilesExist` at `prepare-package`), and copy `web/dist` into the jar
(`maven-resources-plugin`). The result is still one artifact served by
`java -jar`, but there is only one Node in play, and `./scripts/verify.sh`
exercises that exact path. If somebody later wants `mvn package` to work on a
bare machine with no frontend build, that is a ROADMAP conversation — it is not a
reason to quietly add a Maven frontend plugin.

## Constraints

- Every command documented in `AGENTS.md`/`README.md` must have been executed on
  this checkout (verified before this repo was committed).
- No credentials, no external services, no database needed to run, test or build.
- The toolchain is repo-local: `.mise.toml` + `./mvnw` + `web/package.json`.
- Focused, small example: no auth, no admin UI, no multi-module Maven split.

## Non-goals (do not "improve" these without a ROADMAP item)

- A database, ORM entities, migrations, or seeded data.
- Authentication/authorization, users, roles.
- Docker/CI/CD configuration (see ROADMAP items 2 and 3 for their acceptance
  criteria — they are planned, not forgotten).
- A component library, CSS framework, or design system.
- Any Maven-driven frontend build (see Packaging research).

## Success criteria (met, verified on this checkout)

1. `./mvnw test` — 8 green backend tests, no network.
2. `cd web && bun test && bun run typecheck && bun run lint && bun run build` — green.
3. `./mvnw package` — jar contains `BOOT-INF/classes/static/index.html` plus hashed assets.
4. Running that jar: `/api/items` 200 JSON, `/api/items/999` 404
   `application/problem+json`, `/items/42` 200 HTML (deep link), `/api/nope` 404
   JSON, `/assets/does-not-exist.js` 404 JSON — asserted by `./scripts/smoke.sh`.
5. Vite dev server proxies `/api` to the API (verified over HTTP on `:5173`).
6. `./scripts/verify.sh` green end to end; `./dev doctor` exits 0.
