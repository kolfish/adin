package dev.koifih.client.rendering.font;

import dev.koifih.Adin;

public final class Fonts {
    public static final MsdfFont COMFORTAA_BOLD =
            MsdfFont.load("comfortaa-bold", Adin.id("textures/font/comfortaa-bold.png"), '?');
    public static final MsdfFont MATERIAL_ICONS =
            MsdfFont.load("material/icons", Adin.id("textures/font/material-icons.png"), 0xe8b8);

    public static final float DISTANCE_RANGE = COMFORTAA_BOLD.distanceRange();

    static {
        if (MATERIAL_ICONS.distanceRange() != DISTANCE_RANGE) {
            throw new IllegalStateException("Every MSDF atlas must share the text pipeline's distance range");
        }
    }

    private Fonts() {}
}
