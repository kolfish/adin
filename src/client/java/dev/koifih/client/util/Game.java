package dev.koifih.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Game {
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return mc().player;
    }

    public static ClientLevel level() {
        return mc().level;
    }

    public static boolean playing(Minecraft client) {
        return client.player != null && client.gui.screen() == null && client.mouseHandler.isMouseGrabbed();
    }

    public static boolean playing() {
        return playing(mc());
    }
}
