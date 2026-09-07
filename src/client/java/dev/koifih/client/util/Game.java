package dev.koifih.client.util;

import net.minecraft.client.Minecraft;

public final class Game {
    private Game() {}

    public static boolean playing(Minecraft client) {
        return client.player != null && client.gui.screen() == null && client.mouseHandler.isMouseGrabbed();
    }
}
