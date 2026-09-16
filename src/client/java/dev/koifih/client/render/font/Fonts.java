package dev.koifih.client.render.font;

import dev.koifih.Adin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Fonts {
    public static final Font COMFORTAA_BOLD =
            Font.load("comfortaa-bold", Adin.id("textures/font/comfortaa-bold.png"), '?');
    public static final Font MATERIAL_ICONS =
            Font.load("material/icons", Adin.id("textures/font/material-icons.png"), 0xe8b8);
    public static final Font LOGO = Font.load("logo", Adin.id("textures/font/logo.png"), 1);
    public static final Font LOGO_SMALL = Font.load("logo_small", Adin.id("textures/font/logo_small.png"), 1);
    public static final Font ADIN_ICONS = Font.load("adin_icons", Adin.id("textures/font/adin_icons.png"), 1);
    public static final Font ADIN_ICONS_SMALL =
            Font.load("adin_icons_small", Adin.id("textures/font/adin_icons_small.png"), 1);
    public static final Font WORDMARK = Font.load("wordmark", Adin.id("textures/font/wordmark.png"), 1);
    public static final Font WORDMARK_SMALL = Font.load("wordmark_small", Adin.id("textures/font/wordmark_small.png"), 1);

    public static final float DISTANCE_RANGE = COMFORTAA_BOLD.distanceRange();

    static {
        for (Font font : new Font[] {MATERIAL_ICONS, LOGO, LOGO_SMALL, ADIN_ICONS, ADIN_ICONS_SMALL, WORDMARK, WORDMARK_SMALL}) {
            if (font.distanceRange() != DISTANCE_RANGE) {
                throw new IllegalStateException("Every MSDF atlas must share the text pipeline's distance range");
            }
        }
    }
}
