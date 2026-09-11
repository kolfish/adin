#!/usr/bin/env bash
set -euo pipefail
src="${1:-$HOME/records/cropped}"
repo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
out="$repo/src/main/resources/assets/adin/video"
fps=30; width=320; height=180; quality=6
# clip id | source file | module | label key (empty = single clip)
clips=(
  "aim_assist|aimassist.mp4|aimAssist|"
  "triggerbot|triggerbot.mp4|triggerbot|"
  "shield_breaker|shieldbreaker.mp4|shieldBreaker|"
  "auto_hit_crystal|auto hit crystal.mp4|autoHitCrystal|"
  "auto_crystal|auto crystal.mp4|autoCrystal|"
  "auto_cart|auto cart.mp4|autoCart|"
  "auto_anchor|auto anchor.mp4|autoAnchor|video.normal"
  "auto_anchor_safe|auto anchor w safe anchor.mp4|autoAnchor|video.safe"
)
mkdir -p "$out"
{
  echo '{'
  echo '  "clips": {'
  first=1
  for entry in "${clips[@]}"; do
    IFS='|' read -r id file module label <<< "$entry"
    ffmpeg -v error -y -i "$src/$file" -vf "fps=$fps,scale=$width:$height:flags=lanczos" -c:v mjpeg -q:v $quality -pix_fmt yuvj420p -an -f avi "$out/$id.avi"
    [ $first = 1 ] || echo ','
    first=0
    printf '    "%s": {"module": "%s", "label": %s}' "$id" "$module" "$([ -n "$label" ] && printf '"%s"' "$label" || echo null)"
    echo "  $id: $(du -h "$out/$id.avi" | cut -f1)" >&2
  done
  echo
  echo '  }'
  echo '}'
} > "$out/index.json"
