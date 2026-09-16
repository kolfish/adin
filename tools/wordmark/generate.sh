#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
assets="$repo_dir/src/main/resources/assets/adin"
build_dir="$(mktemp -d)"
javac -encoding UTF-8 -d "$build_dir" "$repo_dir/tools/wordmark/WordmarkAtlas.java"
java -Djava.awt.headless=true -cp "$build_dir" WordmarkAtlas "$assets" wordmark 128
java -Djava.awt.headless=true -cp "$build_dir" WordmarkAtlas "$assets" wordmark_small 48
rm -rf "$build_dir"
