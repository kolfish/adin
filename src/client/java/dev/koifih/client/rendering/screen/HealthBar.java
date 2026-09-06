package dev.koifih.client.rendering.screen;

import dev.koifih.client.utils.Colors;
import dev.koifih.client.utils.MathHelper;

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
        float f = MathHelper.clamp01(fraction);
        return f < 0.5f ? Colors.lerp(LOW, MID, f * 2f) : Colors.lerp(MID, HIGH, (f - 0.5f) * 2f);
    }

    public static void collect(OverlayCollector shapes, ScreenRect box, Side side, float fraction, float clearance, float pixel) {
        float shortSide = Math.min(box.width(), box.height()) / pixel;
        float thickness = MathHelper.clamp(shortSide * THICKNESS_FRACTION, MIN_THICKNESS, MAX_THICKNESS) * pixel;
        float border = Math.min(MAX_BORDER, thickness / pixel * 0.5f) * pixel;
        float offset = MathHelper.clamp(shortSide * GAP_FRACTION, MIN_GAP, MAX_GAP) * pixel + clearance;
        ScreenRect bar = switch (side) {
            case LEFT -> new ScreenRect(box.minX() - offset - thickness, box.minY(), box.minX() - offset, box.maxY());
            case RIGHT -> new ScreenRect(box.maxX() + offset, box.minY(), box.maxX() + offset + thickness, box.maxY());
            case TOP -> new ScreenRect(box.minX(), box.minY() - offset - thickness, box.maxX(), box.minY() - offset);
            case BOTTOM -> new ScreenRect(box.minX(), box.maxY() + offset, box.maxX(), box.maxY() + offset + thickness);
        };
        float f = MathHelper.clamp01(fraction);
        ScreenRect filled = switch (side) {
            case LEFT, RIGHT -> new ScreenRect(bar.minX(), bar.maxY() - bar.height() * f, bar.maxX(), bar.maxY());
            case TOP, BOTTOM -> new ScreenRect(bar.minX(), bar.minY(), bar.minX() + bar.width() * f, bar.maxY());
        };
        shapes.rect(bar.expand(border), RectStyle.fill(BACKGROUND));
        if (f > 0f) shapes.rect(filled, RectStyle.fill(color(f)));
    }
}
