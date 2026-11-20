#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew --no-daemon :kotlin:jsProductionExecutableCompileSync "$@"
mkdir -p generated
cp kotlin/build/compileSync/js/main/productionExecutable/kotlin/*.mjs* generated/
