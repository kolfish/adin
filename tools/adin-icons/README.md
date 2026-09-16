# Adin icons

These icons are drawn in the logo's style: a grey back layer, an accent hero
layer and a light front layer, split along the logo's -22° tilt with
round-capped strokes and small gaps between layers. `AdinIconAtlas.java` builds
each icon on a 500 unit canvas with a 44 unit stroke, so every icon has the same
line weight, and writes each layer as a signed-distance glyph in the layout the
font loader reads, with the same 4 pixel distance range as the text atlases.

Like the logo, there are two atlases: `adin_icons` with 128 px cells and
`adin_icons_small` with 64 px cells, picked by the drawn size. Each icon takes
three glyphs, `index * 3 + 1` for the back layer, then the hero and front
layers, so `Draw.icon` can colour each layer from the theme. The index is the
`AdinIcon` ordinal, so the generator's list and the enum must stay in the same
order.

| Index | `AdinIcon` | Used by |
| --- | --- | --- |
| 0 | `COMBAT` | Combat tab |
| 1 | `MOVEMENT` | Movement tab |
| 2 | `RENDER` | Render tab |
| 3 | `HUD` | HUD tab |
| 4 | `PLAYER` | Player tab |
| 5 | `MISC` | Misc tab |
| 6 | `FRIENDS` | Friends tab |
| 7 | `CONFIGS` | Configs tab |
| 8 | `SETTINGS` | Client settings and module settings buttons |
| 9 | `LANGUAGE` | Language row |
| 10 | `SIZE` | Size row |
| 11 | `UNITS` | Units row |
| 12 | `TOOLTIPS` | Tooltips row |
| 13 | `PREVIEW` | Preview and cape catalog buttons |
| 14 | `KEYBINDS` | Keybind list header |
| 15 | `LIGHT` | Light theme segment |
| 16 | `DARK` | Dark theme segment |
| 17 | `PLAY` | Module video button and animated cape badge |
| 18 | `TOGGLE` | Toggle bind mode |
| 19 | `HOLD` | Hold bind mode |
| 20 | `IDLE` | Idle preview pose |
| 21 | `WALK` | Walking preview pose |
| 22 | `SNEAK` | Sneaking preview pose |
| 23 | `SWIM` | Swimming preview pose |
| 24 | `FLY` | Elytra flying preview pose |

The icons use `shaders/gui/icon.fsh` rather than the text shader. It averages
a 4 × 4 grid of distance samples per pixel and blends edges in linear light
against the backdrop colour passed by the caller, so partly covered pixels
keep the brightness they should have instead of the darker edges that plain
sRGB blending gives.

```sh
bash tools/adin-icons/generate.sh
```
