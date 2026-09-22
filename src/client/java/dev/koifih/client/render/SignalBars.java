package dev.koifih.client.render;

import dev.koifih.client.util.Colors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalBars {
    public static final int COUNT = 4;

    private static final float GRID = 24f;
    private static final float[] HEIGHTS = {4.5f, 8f, 11.5f, 15f};
    private static final float FIRST = 5f;
    private static final float SPACING = 4.67f;
    private static final float WIDTH = 2.6f;
    private static final float BASE = 20.8f;

    public static void draw(GuiGraphicsExtractor graphics, float x, float y, float size, float lit, int off, int on) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y).scale(size / GRID);
            for (int i = 0; i < COUNT; i++) {
                float bar = HEIGHTS[i] + WIDTH;
                Draw.rect(graphics, FIRST + i * SPACING - WIDTH * 0.5f, BASE - bar, WIDTH, Math.round(bar), 1,
                        Colors.lerp(off, on, Math.clamp(lit - i, 0f, 1f)));
            }
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
