# Sidebar Material Icons

Icons come from Google's official [Material Design Icons](https://github.com/google/material-design-icons)
font, downloaded from `font/MaterialIcons-Regular.ttf`. The original font is
bundled under `assets/adin/font/material/`, with its Apache 2.0 license in
`license.txt`. This license applies to the font and derived icon atlas instead
of the example mod's CC0 license.

| Category | Material icon | Codepoint |
| --- | --- | --- |
| Combat | gps_fixed | E1B3 |
| Render | visibility | E8F4 |
| Misc | widgets | E1BD |
| Player | person | E7FD |
| Row and client settings | settings | E8B8 |
| Client settings close | close | E5CD |
| Bind mode: toggle | sync_alt | EA18 |
| Bind mode: hold | touch_app | E913 |
| Dropdown chevron | expand_more | E5CF |
| Dropdown selected option | check | E5CA |
| Color picker copy | content_copy | E14D |
| Color picker paste | content_paste | E14F |
| Theme switch light | light_mode | E518 |
| Theme switch dark | dark_mode | E51C |
| Language row | language | E894 |
| Configs | folder | E2C7 |
| Delete config | delete | E872 |
| New config | add | E145 |
| Picker search | search | E8B6 |
| Movement category | directions_run | E566 |
| Size row | zoom_in | E8FF |
| Units row | straighten | E41C |
| ESP preview | open_in_new | E89E |
| Preview window back | arrow_back | E5C4 |
| Keybind list header | keyboard | E312 |
| Friends tab | group | E7EF |
| Module video | play_circle_outline | E039 |
| Tooltips row | info | E88E |

`material-icons.codepoints` contains the relevant entries from Google's upstream
codepoint list. `charset.txt` selects these twenty-five glyphs for the MSDF atlas. Icons share `text.vsh` / `text.fsh` with the Comfortaa renderer, including
the four-pixel distance range and linear texture filtering. No runtime download
or font generation is needed.

Regenerate with the same msdf-atlas-gen build documented in `../fonts/README.md`:

```sh
bash tools/icons/generate.sh /absolute/path/to/msdf-atlas-gen
```

`Category` defines the ordering and icon mapping. `CategoryButton` handles pill
rendering, mouse clicks, keyboard activation and narration. `ClickGui` retains
one selected category for the current client session; category contents will be
added separately. Hover or keyboard focus highlights a row without selecting it.

Pill widths use measured Comfortaa label advances plus icon/padding space.
A single opaque selection pill slides and resizes over 150 ms using a monotonic
clock. Selected labels and icons are solid black. Unselected icons remain gray. Mouse and keyboard activation intentionally play no sound.
