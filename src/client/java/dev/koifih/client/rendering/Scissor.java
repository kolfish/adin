package dev.koifih.client.rendering;

import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class Scissor {
    private static final ThreadLocal<ScreenRectangle> CURRENT = new ThreadLocal<>();

    private Scissor() {}

    public static ScreenRectangle current() {
        return CURRENT.get();
    }

    public static void clip(ScreenRectangle area, Runnable draw) {
        ScreenRectangle previous = CURRENT.get();
        ScreenRectangle intersection = previous == null ? area : previous.intersection(area);
        CURRENT.set(intersection == null ? ScreenRectangle.empty() : intersection);
        try {
            draw.run();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }
}
