# Comfortaa Bold MSDF assets

The original, unmodified **Comfortaa Bold** TTF is bundled in
`src/main/resources/assets/adin/font/comfortaa-bold.ttf`, together with its
SIL Open Font License in `ofl.txt`. The font and derived atlas are covered by
that license, rather than the example mod's CC0 license.

Source: [googlefonts/comfortaa](https://github.com/googlefonts/comfortaa/blob/main/fonts/TTF/Comfortaa-Bold.ttf).
TTF SHA-256: `1960ed76ee4fe61de32b5779d104072d5d10431bf7fa45bc982d0cf71004843d`.

The checked-in PNG and JSON were generated with
[msdf-atlas-gen](https://github.com/Chlumsky/msdf-atlas-gen), commit
`6148900d59423059bafde2f51a0cb303184404bd`, built with
`MSDF_ATLAS_USE_VCPKG=OFF` and `MSDF_ATLAS_USE_SKIA=OFF`.
No atlas generation or font download happens at game startup.

To regenerate, run from the repository root:

```sh
bash tools/fonts/generate.sh /absolute/path/to/msdf-atlas-gen
```

The atlas contains 415 glyphs: printable ASCII, Latin-1, Latin Extended-A and basic Cyrillic. It uses 48 pixels
per em, a 4-pixel distance range and top-origin coordinates. Unsupported
codepoints render as `?`. Extend `charset.txt` and regenerate both the PNG
and JSON together to support more characters available in the font.

`TextRenderer` supports single-line text, width measurement and colored spans
with kerning across span boundaries. Positions and font size are GUI pixels;
the vertical draw position is a baseline. The GLSL fragment shader takes the
median of the atlas's RGB channels and uses screen derivatives for coverage.
The texture uses linear filtering without mipmaps. Minecraft owns texture,
sampler and vertex buffer lifetimes. Like the rectangle renderer, this renderer
currently submits unclipped geometry.

Header color and placement live in `ClickGui`: `adin` is `#EAEAEA`, `.lol`
is pastel green `#B8DDB0`, and the brand is centered vertically in the top bar.
