#!/usr/bin/env bash
set -euo pipefail

font_generator="${1:-msdf-atlas-gen}"
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_dir"
"$font_generator" \
  -font src/main/resources/assets/adin/font/comfortaa-bold.ttf \
  -charset tools/fonts/charset.txt \
  -type msdf -format png -size 48 -pxrange 4 -yorigin top \
  -imageout src/main/resources/assets/adin/textures/font/comfortaa-bold.png \
  -json src/main/resources/assets/adin/font/comfortaa-bold.json
