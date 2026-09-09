package dev.koifih.client.util;

import net.minecraft.client.player.LocalPlayer;

/** Predicates about the local player's movement state. */
public final class Players {
    private static final float MOVE_EPSILON = 1.0E-10f;

    private Players() {}

    /** True when movement input is being applied in any direction. */
    public static boolean isMoving(LocalPlayer player) {
        return player.input.getMoveVector().lengthSquared() > MOVE_EPSILON;
    }

    /** True when the player is descending freely, the state a critical hit requires. */
    public static boolean canCrit(LocalPlayer player) {
        return player.fallDistance > 0.0 && !player.onClimbable() && !player.isInWater()
                && !player.isMobilityRestricted() && !player.isPassenger();
    }
}
