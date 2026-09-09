package dev.koifih.client.render;

import dev.koifih.client.util.Colors;

public final class Opacity {
    private static final ThreadLocal<Float> CURRENT = ThreadLocal.withInitial(() -> 1f);

    private Opacity() {}

    public static void with(float alpha, Runnable draw) {
        float previous = CURRENT.get();
        CURRENT.set(previous * Math.clamp(alpha, 0f, 1f));
        try {
            draw.run();
        } finally {
            CURRENT.set(previous);
        }
    }

    public static int apply(int argb) {
        float alpha = CURRENT.get();
        if (alpha >= 1f) return argb;
        return Colors.withAlpha(argb, alpha);
    }

    public static Style apply(Style style) {
        if (CURRENT.get() >= 1f) return style;
        return new Style(apply(style.fill()), apply(style.stroke()), style.strokeWidth(),
                apply(style.outline()), style.outlineWidth(), style.edges());
    }
}
