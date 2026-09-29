#!/usr/bin/env bash
# Boot the packaged jar and assert the API + SPA contract over real HTTP.
set -euo pipefail
cd "$(dirname "$0")/.."

port="${PORT:-8080}"
base="http://localhost:${port}"

jar=""
for candidate in target/springboard-*.jar; do
  if [ -f "$candidate" ]; then jar="$candidate"; fi
done
if [ -z "$jar" ]; then
  echo "scripts/smoke.sh: no jar in target/ — run ./scripts/verify.sh first" >&2
  exit 1
fi

java=("java")
if command -v mise >/dev/null 2>&1; then
  java=(mise exec -- java)
fi

if curl -fsS "$base/actuator/health" >/dev/null 2>&1; then
  echo "scripts/smoke.sh: something is already serving :$port" >&2
  exit 1
fi

log="${TMPDIR:-/tmp}/springboard-smoke.log"
"${java[@]}" -jar "$jar" --server.port="$port" >"$log" 2>&1 &
pid=$!
# shellcheck disable=SC2064  # expand $pid now, on purpose
trap "kill $pid 2>/dev/null || true" EXIT

for _ in $(seq 1 60); do
  if curl -fsS "$base/actuator/health" >/dev/null 2>&1; then break; fi
  if ! kill -0 "$pid" 2>/dev/null; then
    echo "scripts/smoke.sh: the app exited during startup — see $log" >&2
    exit 1
  fi
  sleep 1
done

fail=0
check() { # check <path> <expected-status> <body-substring> [expected-content-type]
  response=$(curl -s -o - -w '\n%{http_code}\n%{content_type}' "${base}${1}")
  code=$(printf '%s' "$response" | tail -n2 | head -n1)
  ctype=$(printf '%s' "$response" | tail -n1)
  body=$(printf '%s' "$response" | sed '$d' | sed '$d')

  if [ "$code" != "$2" ]; then
    echo "FAIL ${1} -> HTTP $code (want $2)"
    fail=1
    return
  fi
  case "$body" in
    *"$3"*) ;;
    *) echo "FAIL ${1} -> HTTP $code but body lacks '$3'"; fail=1; return ;;
  esac
  if [ -n "${4:-}" ]; then
    case "$ctype" in
      *"$4"*) ;;
      *) echo "FAIL ${1} -> HTTP $code content-type '$ctype' lacks '$4'"; fail=1; return ;;
    esac
  fi
  echo "ok   ${1} -> HTTP $code"
}

check "/api/items" 200 '"First item"' "application/json"
check "/api/items/999" 404 '"status":404' "application/problem+json"
check "/items/42" 200 'id="root"' "text/html"
check "/api/nope" 404 '"status":404' "application/problem+json"
check "/assets/does-not-exist.js" 404 '"status":404' "application/problem+json"

if [ "$fail" -ne 0 ]; then
  echo "smoke test FAILED — app log: $log" >&2
  exit 1
fi

echo "ok   smoke test passed against $jar"
