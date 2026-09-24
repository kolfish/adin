package dev.koifih.client.render;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Scissor {
    private static ScreenRectangle current;

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
