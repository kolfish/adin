package dev.koifih.client.render;

import dev.koifih.client.util.Colors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Opacity {
    private static float current = 1f;

    public static void with(float alpha, Runnable draw) {
        float previous = current;
        current = previous * Math.clamp(alpha, 0f, 1f);
        try {
            draw.run();
        } finally {
            current = previous;
        }
    }

    public static int apply(int argb) {
        return current >= 1f ? argb : Colors.withAlpha(argb, current);
    }

    public static Style apply(Style style) {
        if (current >= 1f) return style;
        return new Style(apply(style.fill()), apply(style.stroke()), style.strokeWidth(),
                apply(style.outline()), style.outlineWidth(), style.edges());
    }
}
