#!/usr/bin/env bash
# Компилирует ядро алгоритма (без JavaFX) и запускает проверки.
set -euo pipefail
cd "$(dirname "$0")/.."

if command -v javac >/dev/null 2>&1; then
    JAVAC=(javac)
else
    JAVAC=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi

rm -rf out && mkdir -p out
"${JAVAC[@]}" -encoding UTF-8 -d out \
    src/main/java/com/cgvsu/rasterization/core/*.java \
    src/test/java/com/cgvsu/rasterization/core/SectorChecks.java
java -Dstdout.encoding=UTF-8 -cp out com.cgvsu.rasterization.core.SectorChecks