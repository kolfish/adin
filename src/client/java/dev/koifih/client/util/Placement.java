package dev.koifih.client.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public final class Placement {
    private static final float DUPLICATE_ROTATION = 2f;
    private static final float DUPLICATE_TOLERANCE = 0.0001f;

    private static boolean placed;
    private static boolean rotated;
    private static float lastRotationDelta;
    private static float lastPlacedDelta = -1f;

    private Placement() {}

    public static boolean ready() {
        return !placed && !(rotated && duplicates(lastRotationDelta));
    }

    public static boolean duplicates(float yawDelta) {
        return yawDelta > DUPLICATE_ROTATION && Math.abs(yawDelta - lastPlacedDelta) < DUPLICATE_TOLERANCE;
    }

    public static void rotated(float yawDelta) {
        lastRotationDelta = yawDelta;
        rotated = true;
    }

    public static void sent(Packet<?> packet) {
        if (packet instanceof ServerboundClientTickEndPacket) {
            placed = false;
        } else if (packet instanceof ServerboundUseItemOnPacket) {
            placed = true;
            if (rotated) lastPlacedDelta = lastRotationDelta;
            rotated = false;
        }
    }
}
