# springboard

A full-stack starter: **Spring Boot 4.1** (Java 25, Maven) REST API + **React 19 /
Vite 8 / TypeScript** SPA in one repository, shipped as **one jar**.

- Dev: Vite on `:5173` proxies `/api` to the API on `:8080` — one origin, no CORS.
- Prod: `./mvnw package` copies the built SPA into the jar; the API serves both
  `/api/**` and the client-side routes (deep links included).

## Quickstart

```bash
mise install                 # Java 25 + Node 26 from .mise.toml
cd web && bun install && cd ..
./scripts/dev.sh             # API :8080 + SPA :5173 (Ctrl-C stops both)
```

Then open <http://localhost:5173>. The example list comes from `GET /api/items`.

## Build and run the real artifact

```bash
cd web && bun run build && cd ..   # the enforcer will tell you if you skip this
./mvnw package
java -jar target/springboard-0.0.1-SNAPSHOT.jar
# http://localhost:8080        -> SPA
# http://localhost:8080/api/items -> JSON
# http://localhost:8080/items/2   -> deep link, served by index.html
```

## Checks

```bash
./scripts/verify.sh   # everything: backend tests, frontend tests/typecheck/lint/build, jar smoke test
cd web && bun test    # frontend only
./mvnw test           # backend only
```

## Layout

```
src/main/java/ai/primeiq/springboard/   API (package-by-feature: items/, web/)
src/test/java/                          backend tests + SPA routing test
web/                                    React app (its own package.json, bun.lock, tsconfigs)
scripts/                                dev.sh, smoke.sh, verify.sh
docs/                                   BRIEF, ARCHITECTURE, CONVENTIONS, ROADMAP
dev                                     Hermes session entrypoint for this repo
```

The toolchain is pinned in `.mise.toml` (Java 25 LTS, Node 26) and Maven is
vendored through `./mvnw`, so nothing depends on globally installed versions.

Deep dives: [`AGENTS.md`](AGENTS.md) (operating manual) ·
[`docs/BRIEF.md`](docs/BRIEF.md) (decisions and the packaging research) ·
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) ·
[`docs/CONVENTIONS.md`](docs/CONVENTIONS.md) ·
[`docs/ROADMAP.md`](docs/ROADMAP.md).
