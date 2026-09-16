# Sidebar Material Icons

Icons come from Google's official [Material Design Icons](https://github.com/google/material-design-icons)
font, downloaded from `font/MaterialIcons-Regular.ttf`. The original font is
bundled under `assets/adin/font/material/`, with its Apache 2.0 license in
`license.txt`. This license applies to the font and derived icon atlas instead
of the example mod's CC0 license.

| Category | Material icon | Codepoint |
| --- | --- | --- |
| Missing glyph fallback | settings | E8B8 |
| Client settings close | close | E5CD |
| Dropdown chevron | expand_more | E5CF |
| Dropdown selected option | check | E5CA |
| Color picker copy | content_copy | E14D |
| Color picker paste | content_paste | E14F |
| Delete config | delete | E872 |
| New config | add | E145 |
| Picker search | search | E8B6 |
| Preview window back | arrow_back | E5C4 |
| Cape rename | edit | E3C9 |

`material-icons.codepoints` contains the relevant entries from Google's upstream
codepoint list. `charset.txt` selects these twenty-five glyphs for the MSDF atlas. Icons share `text.vsh` / `text.fsh` with the Comfortaa renderer, including
the four-pixel distance range and linear texture filtering. No runtime download
or font generation is needed.

Regenerate with the same msdf-atlas-gen build documented in `../fonts/README.md`:

```sh
bash tools/icons/generate.sh /absolute/path/to/msdf-atlas-gen
```

`Category` defines the ordering. The sidebar tabs, settings buttons, settings rows, theme, bind mode and preview pose switches, preview and video buttons and the keybind list use `../adin-icons`. `CategoryButton` handles pill
rendering, mouse clicks, keyboard activation and narration. `ClickGui` retains
one selected category for the current client session; category contents will be
added separately. Hover or keyboard focus highlights a row without selecting it.

Pill widths use measured Comfortaa label advances plus icon/padding space.
A single opaque selection pill slides and resizes over 150 ms using a monotonic
clock. Selected labels and icons are solid black. Unselected icons remain gray. Mouse and keyboard activation intentionally play no sound.
