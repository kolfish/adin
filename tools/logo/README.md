# Logo distance fields

The watermark mark is drawn through the text pipeline like the Material icons,
so it stays sharp at any size. `LogoAtlas.java` builds the star, the orbit band
behind the star and the band in front of it as three signed-distance glyphs,
with metrics in the layout the font loader reads and the same 4 pixel distance
range as the text atlases. Two atlases are generated: `logo` with 256 px cells
for large sizes and `logo_small` with 64 px cells for small ones, because the
shader's edge softening is measured in atlas texels and needs the atlas to sit
near the on-screen size. `Logo` picks the atlas from the size being drawn.

```sh
bash tools/logo/generate.sh
```
