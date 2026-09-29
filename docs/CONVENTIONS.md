# Conventions

Patterns to copy. Where a rule exists because of a specific trap, the trap is
named.

## Java

- **API values are records** (`Item`). Add a field by adding it to the record;
  never introduce a mutable bean for the API boundary.
- **Package by feature.** `items/` contains its controller, service and model.
  Cross-cutting web configuration lives in `web/`. Do not create `controllers/`,
  `services/` or `models/` buckets.
- **Constructor injection only.** No field injection, no `@Autowired` on fields.
- **Errors are exceptions**, rendered as RFC 9457 problem details
  (`spring.mvc.problemdetails.enabled: true` in `application.yaml`). Throw
  `ResponseStatusException` (or a `@ResponseStatus`-annotated exception) rather
  than returning error DTOs by hand.
- **Configuration lives in `application.yaml`**, not `.properties`, and only
  there — no `@Value` sprinkles for things that belong in config.
- **Virtual threads / thread pools:** leave Boot's defaults alone until a ROADMAP
  item says otherwise.

### Backend tests

- `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`. Note the Boot 4
  relocation: `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
  (it used to be `...test.autoconfigure.web.servlet...`). The static helpers
  (`MockMvcRequestBuilders`, `MockMvcResultMatchers`) still live in
  `org.springframework.test.web.servlet`.
- One test class per behaviour area, named `<Area>Test`, in the package it tests:
  `ItemsApiTest` (the API contract) and `SpaRoutingTest` (the SPA/routing contract).
- Always assert status **and** payload/content type — a 200 with the wrong body is
  the failure mode these tests exist to catch.
- `MockMvcTester` (AssertJ) is available in Boot 4 and is fine for new tests; the
  existing two use classic `MockMvc` for readability. Don't mix styles inside one
  class.

## Frontend (TypeScript / React)

- **One HTTP module.** `web/src/api/client.ts` owns `fetch`, the base path, the
  `Item` type and `ApiError`. Components import from it; no `fetch` elsewhere.
- **Function components + local state.** `useState`/`useEffect`, no state library,
  no context until something actually needs it. Guard async `useEffect` bodies
  with an `active` flag (see `ItemsPage`) so unmounts don't set state.
- **Routing** is React Router 7 in `App.tsx`; new routes go there. Any new
  client-side path is automatically covered by the SPA fallback — add a case to
  `SpaRoutingTest` if it changes the fallback rules.
- **Styling** is plain CSS in `src/index.css` with CSS custom properties and a
  `prefers-color-scheme` block. No CSS framework, no CSS-in-JS.
- **TypeScript stays on 6.x.** `typescript-eslint` 8.71 refuses TS 7.0
  ("typescript-eslint does not support TS 7.0") and supports it from TS 7.1+;
  bumping `typescript` alone breaks `bun run lint`. Tracked in ROADMAP.
- `tsconfig.app.json` sets `types: ["vite/client", "bun"]` deliberately: TS 7
  changes the default to `[]`, and the frontend tests are `bun:test` files.
- Prettier config is `.prettierrc.json` at the repo root (100 cols, double quotes);
  the Hermes formatter hook runs the repo's own prettier — see `.hermes.md`.
  `.java` and `.md` files are not auto-formatted here (no Java formatter is
  configured); keep them tidy by hand.

## Build / packaging boundary

If you need something new in the build, put it on the correct side of the line:

- Frontend concern (`bun`/Vite/deps/TS config) → `web/`, driven by `bun run …`.
- Backend or packaging concern → `pom.xml`, driven by `./mvnw …`.
- Both at once → `scripts/verify.sh`, which is the only place the two toolchains
  meet. Maven itself must not invoke Node (see `AGENTS.md` §Non-negotiable).

## Commits

Conventional Commits: `type(scope)?: subject` with `type` from
`build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test`, optional scope,
subject ≤ 100 chars, imperative mood (`feat(items): add pagination`).

This is not a style preference: the machine-wide `commit-msg` hook
(`core.hooksPath=~/.config/git/hooks`, from the dotfiles) rejects anything else
with `not a Conventional Commit`. Keep the initial scaffold as one commit and
squash noisy work-in-progress before sharing.
