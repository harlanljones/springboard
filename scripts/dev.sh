#!/usr/bin/env bash
# Run the API and the Vite dev server together. Ctrl-C stops both.
set -euo pipefail
cd "$(dirname "$0")/.."
root=$(pwd)

if ! command -v bun >/dev/null 2>&1; then
  echo "scripts/dev.sh: bun is required (see AGENTS.md §Environment)" >&2
  exit 1
fi

# .mise.toml pins java/node; use it when mise is available, otherwise use PATH.
mvn=("$root/mvnw")
if command -v mise >/dev/null 2>&1; then
  mvn=(mise exec -- "$root/mvnw")
fi

api_pid=""
web_pid=""
cleanup() {
  if [ -n "$api_pid" ]; then kill "$api_pid" 2>/dev/null || true; fi
  if [ -n "$web_pid" ]; then kill "$web_pid" 2>/dev/null || true; fi
}
trap cleanup EXIT INT TERM

if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
  echo "scripts/dev.sh: something is already serving :8080 (stop it first)" >&2
  exit 1
fi

"${mvn[@]}" -q spring-boot:run &
api_pid=$!
(cd "$root/web" && bun run dev) &
web_pid=$!

for _ in $(seq 1 90); do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then break; fi
  if ! kill -0 "$api_pid" 2>/dev/null; then
    echo "scripts/dev.sh: the API exited during startup" >&2
    exit 1
  fi
  sleep 1
done

echo
echo "API  http://localhost:8080      (health: /actuator/health)"
echo "SPA  http://localhost:5173      (/api/* is proxied to the API)"
echo
wait
