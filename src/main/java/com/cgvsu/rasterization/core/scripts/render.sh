#!/usr/bin/env bash
# Рисует демонстрационную сцену и увеличенные фрагменты в PNG (без JavaFX).
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
    src/main/java/com/cgvsu/rasterization/demo/*.java \
    src/test/java/com/cgvsu/rasterization/core/ScenePngRenderer.java
java -Dstdout.encoding=UTF-8 -cp out com.cgvsu.rasterization.core.ScenePngRenderer "${1:-build-images}"