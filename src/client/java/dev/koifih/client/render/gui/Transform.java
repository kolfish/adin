package dev.koifih.client.render.gui;

import dev.koifih.client.render.Opacity;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Transform {
    private Transform() {}

    public static void translated(GuiGraphicsExtractor graphics, float dx, float dy, Runnable draw) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(dx, dy);
            draw.run();
        } finally {
            graphics.pose().popMatrix();
        }
    }

    private static void scaledAbout(GuiGraphicsExtractor graphics, float centerX, float centerY, float zoom, Runnable draw) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(centerX, centerY).scale(zoom, zoom).translate(-centerX, -centerY);
            draw.run();
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void rotatedAbout(GuiGraphicsExtractor graphics, float radians, float centerX, float centerY, Runnable draw) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().rotateAbout(radians, centerX, centerY);
            draw.run();
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void popIn(GuiGraphicsExtractor graphics, float centerX, float centerY, float shown, float minZoom, Runnable draw) {
        float zoom = minZoom + (1f - minZoom) * shown;
        scaledAbout(graphics, centerX, centerY, zoom, () -> Opacity.with(shown, draw));
    }
}
