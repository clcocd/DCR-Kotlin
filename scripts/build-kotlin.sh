#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

gradle --no-daemon -p kotlin jsProductionExecutableCompileSync "$@"
mkdir -p generated
cp kotlin/build/compileSync/js/main/productionExecutable/kotlin/*.mjs* generated/
