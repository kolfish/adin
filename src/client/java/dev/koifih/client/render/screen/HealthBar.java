package dev.koifih.client.render.screen;

import dev.koifih.client.render.Rect;
import dev.koifih.client.render.Style;
import dev.koifih.client.util.Colors;

public final class HealthBar {
    public enum Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    private static final float MAX_THICKNESS = 2f;
    private static final float MIN_THICKNESS = 0.6f;
    private static final float THICKNESS_FRACTION = 0.1f;
    private static final float MAX_BORDER = 1f;
    private static final float MAX_GAP = 3f;
    private static final float MIN_GAP = 0.75f;
    private static final float GAP_FRACTION = 0.15f;
    private static final int BACKGROUND = 0xFF000000;
    private static final int LOW = 0xFFFF3B30;
    private static final int MID = 0xFFFFD60A;
    private static final int HIGH = 0xFF34C759;

    private HealthBar() {}

    public static int color(float fraction) {
        float f = Math.clamp(fraction, 0f, 1f);
        return f < 0.5f ? Colors.lerp(LOW, MID, f * 2f) : Colors.lerp(MID, HIGH, (f - 0.5f) * 2f);
    }

    public static void collect(ScreenBuffer buffer, Rect box, Side side, float fraction, float clearance, float pixel) {
        float shortSide = Math.min(box.width(), box.height()) / pixel;
        float thickness = Math.clamp(shortSide * THICKNESS_FRACTION, MIN_THICKNESS, MAX_THICKNESS) * pixel;
        float border = Math.min(MAX_BORDER, thickness / pixel * 0.5f) * pixel;
        float offset = Math.clamp(shortSide * GAP_FRACTION, MIN_GAP, MAX_GAP) * pixel + clearance;
        Rect bar = switch (side) {
            case LEFT -> new Rect(box.minX() - offset - thickness, box.minY(), box.minX() - offset, box.maxY());
            case RIGHT -> new Rect(box.maxX() + offset, box.minY(), box.maxX() + offset + thickness, box.maxY());
            case TOP -> new Rect(box.minX(), box.minY() - offset - thickness, box.maxX(), box.minY() - offset);
            case BOTTOM -> new Rect(box.minX(), box.maxY() + offset, box.maxX(), box.maxY() + offset + thickness);
        };
        float f = Math.clamp(fraction, 0f, 1f);
        Rect filled = switch (side) {
            case LEFT, RIGHT -> new Rect(bar.minX(), bar.maxY() - bar.height() * f, bar.maxX(), bar.maxY());
            case TOP, BOTTOM -> new Rect(bar.minX(), bar.minY(), bar.minX() + bar.width() * f, bar.maxY());
        };
        buffer.rect(bar.expand(border), Style.fill(BACKGROUND));
        if (f > 0f) buffer.rect(filled, Style.fill(color(f)));
    }
}
