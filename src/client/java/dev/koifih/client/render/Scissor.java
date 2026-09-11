package dev.koifih.client.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class Scissor {
    private static ScreenRectangle current;

    private Scissor() {}

    public static ScreenRectangle current() {
        return current;
    }

    public static void clip(ScreenRectangle area, Runnable draw) {
        ScreenRectangle previous = current;
        ScreenRectangle intersection = previous == null ? area : previous.intersection(area);
        current = intersection == null ? ScreenRectangle.empty() : intersection;
        try {
            draw.run();
        } finally {
            current = previous;
        }
    }
}
