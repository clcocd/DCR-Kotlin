#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew --no-daemon :kotlin:jsProductionExecutableCompileSync "$@"
mkdir -p generated
output=kotlin/build/compileSync/js/main/productionExecutable/kotlin
cp "$output"/*.mjs generated/
mv generated/dcr-kotlin-kotlin.mjs generated/dcr-kotlin.mjs
