#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew --no-daemon :kotlin:jsProductionExecutableCompileSync "$@"
mkdir -p generated
bundle=(kotlin/build/compileSync/js/main/productionExecutable/kotlin/*.mjs)
[[ ${#bundle[@]} -eq 1 && -f ${bundle[0]} ]]
cp "${bundle[0]}" generated/dcr-kotlin.mjs
