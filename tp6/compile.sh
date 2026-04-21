#!/bin/bash
# TP6 — Best-First Tunisia Map — Compilation Script
# Usage: bash compile.sh  (from the TunisiaUCS/ directory)

echo "=========================================="
echo "  TP6 Best-First — Compilation"
echo "=========================================="

mkdir -p out

javac -encoding UTF-8 -d out src/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✔ Compilation réussie !"
    echo "  Lancez avec : bash run.sh"
else
    echo ""
    echo "✗ Erreur de compilation."
fi
