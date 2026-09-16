package dev.koifih.client.render;

import dev.koifih.client.render.Opacity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Transform {
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
        popIn(graphics, centerX, centerY, shown, minZoom, 0f, 0f, draw);
    }

    public static void popIn(GuiGraphicsExtractor graphics, float centerX, float centerY, float shown, float minZoom,
                             float slideX, float slideY, Runnable draw) {
        float zoom = minZoom + (1f - minZoom) * shown;
        float fade = Math.clamp(shown, 0f, 1f);
        float away = 1f - fade;
        translated(graphics, slideX * away, slideY * away,
                () -> scaledAbout(graphics, centerX, centerY, zoom, () -> Opacity.with(fade, draw)));
    }
}
