package dev.koifih.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

/** Shorthand for the client singleton and the state hanging off it. */
public final class Game {
    private Game() {}

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** The local player, or null when not in a world. */
    public static LocalPlayer player() {
        return mc().player;
    }

    /** The current level, or null when not in a world. */
    public static ClientLevel level() {
        return mc().level;
    }

    /** True when in a world with no screen open and the mouse grabbed. */
    public static boolean playing(Minecraft client) {
        return client.player != null && client.gui.screen() == null && client.mouseHandler.isMouseGrabbed();
    }

    public static boolean playing() {
        return playing(mc());
    }
}
