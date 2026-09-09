package dev.koifih.client.util;

import net.minecraft.client.player.LocalPlayer;

public final class Players {
    private static final float MOVE_EPSILON = 1.0E-10f;

    private Players() {}

    public static boolean isMoving(LocalPlayer player) {
        return player.input.getMoveVector().lengthSquared() > MOVE_EPSILON;
    }

    public static boolean canCrit(LocalPlayer player) {
        return player.fallDistance > 0.0 && !player.onClimbable() && !player.isInWater()
                && !player.isMobilityRestricted() && !player.isPassenger();
    }
}
