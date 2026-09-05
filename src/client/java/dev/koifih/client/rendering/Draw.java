package dev.koifih.client.rendering;

import dev.koifih.client.gui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Draw {
    private Draw() {}

    public static void translated(GuiGraphicsExtractor graphics, float dx, float dy, Runnable draw) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(dx, dy);
            draw.run();
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void scaledAbout(GuiGraphicsExtractor graphics, float centerX, float centerY, float zoom, Runnable draw) {
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

    public static void borderedBox(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float radius, int fill) {
        int r = Math.max(0, Math.round(radius));
        RectRenderer.draw(graphics, x - 1, y - 1, width + 2, Math.max(1, Math.round(height)) + 2, r + 1, Theme.POPUP_BORDER);
        RectRenderer.draw(graphics, x, y, width, Math.max(1, Math.round(height)), r, fill);
    }
}
