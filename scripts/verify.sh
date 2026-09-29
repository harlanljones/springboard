#!/usr/bin/env bash
# The full gate. Run this before claiming any change is done.
set -euo pipefail
cd "$(dirname "$0")/.."

mvn() {
  if command -v mise >/dev/null 2>&1; then
    mise exec -- ./mvnw "$@"
  else
    ./mvnw "$@"
  fi
}

mvn -B verify
(
  cd web
  bun install --frozen-lockfile
  bun test
  bun run typecheck
  bun run lint
  bun run build
)
# Packages the jar (Maven copies the freshly built web/dist into it) and smoke-tests it over HTTP.
mvn -B -DskipTests package
./scripts/smoke.sh

echo "✓ backend tests + frontend tests/typecheck/lint/build + packaged jar smoke test all pass"
