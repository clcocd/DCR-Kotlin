#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew --no-daemon :kotlin:jsProductionExecutableCompileSync "$@"
mkdir -p generated
mapfile -t bundle < <(find . -path '*/build/compileSync/*' -type f -name '*.mjs' | sort)
[[ ${#bundle[@]} -eq 1 ]] || { printf 'Kotlin JS bundles: %s\n' "${bundle[*]}" >&2; exit 1; }
cp "${bundle[0]}" generated/dcr-kotlin.mjs
