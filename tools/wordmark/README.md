# Wordmark distance fields

The watermark shows the word "adin" as a lockup: the dotless `ı` with the adin
star as its dot, and the letters split along the logo's -22° line into the
logo's light and grey tones. `WordmarkAtlas.java` builds that from the bundled
Comfortaa Bold outlines as three signed-distance glyphs (grey part, star, light
part) with the same 4 pixel distance range as the text atlases, so the split is
exact per pixel and stays sharp at any size.

Glyph metrics are measured in units of the lockup's height: drawing at a size
places the ink box at that height, and each glyph's advance is the ink width.
Like the logo there is a large atlas and a small one for small sizes.

```sh
bash tools/wordmark/generate.sh
```
