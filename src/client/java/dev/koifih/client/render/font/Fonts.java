package dev.koifih.client.render.font;

import dev.koifih.Adin;

public final class Fonts {
    public static final Font COMFORTAA_BOLD =
            Font.load("comfortaa-bold", Adin.id("textures/font/comfortaa-bold.png"), '?');
    public static final Font MATERIAL_ICONS =
            Font.load("material/icons", Adin.id("textures/font/material-icons.png"), 0xe8b8);

    public static final float DISTANCE_RANGE = COMFORTAA_BOLD.distanceRange();

    static {
        if (MATERIAL_ICONS.distanceRange() != DISTANCE_RANGE) {
            throw new IllegalStateException("Every MSDF atlas must share the text pipeline's distance range");
        }
    }

    private Fonts() {}
}
