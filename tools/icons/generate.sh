#!/usr/bin/env bash
set -euo pipefail
icon_generator="${1:-msdf-atlas-gen}"
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_dir"
"$icon_generator" \
  -font src/main/resources/assets/adin/font/material/material-icons-regular.ttf \
  -charset tools/icons/charset.txt \
  -type msdf -format png -size 48 -pxrange 4 -yorigin top \
  -imageout src/main/resources/assets/adin/textures/font/material-icons.png \
  -json src/main/resources/assets/adin/msdf/material/icons.json
