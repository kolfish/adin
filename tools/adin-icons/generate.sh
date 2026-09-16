#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
assets="$repo_dir/src/main/resources/assets/adin"
build_dir="$(mktemp -d)"
javac -d "$build_dir" "$repo_dir/tools/adin-icons/AdinIconAtlas.java"
java -Djava.awt.headless=true -cp "$build_dir" AdinIconAtlas "$assets" adin_icons 128
java -Djava.awt.headless=true -cp "$build_dir" AdinIconAtlas "$assets" adin_icons_small 64
rm -rf "$build_dir"
