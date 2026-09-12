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

    public static final float DISTANCE_RANGE = COMFORTAA_BOLD.distanceRange();

    static {
        if (MATERIAL_ICONS.distanceRange() != DISTANCE_RANGE || LOGO.distanceRange() != DISTANCE_RANGE || LOGO_SMALL.distanceRange() != DISTANCE_RANGE) {
            throw new IllegalStateException("Every MSDF atlas must share the text pipeline's distance range");
        }
    }
}
