package dev.koifih.client.util;

import dev.koifih.client.AdinClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Reach {
    private static final int LAG_TICKS = 2;

    private Reach() {}

    public static boolean canHit(LocalPlayer player, Entity target) {
        Vec3 eye = player.getEyePosition();
        double range = player.entityInteractionRange();
        AABB now = target.getBoundingBox();
        AABB ahead = now.move(target.getDeltaMovement().scale(LAG_TICKS));
        Vec3 look = player.getLookAngle().scale(range);
        Vec3 sent = AdinClient.ROTATIONS.lastSent().direction().scale(range);
        return hits(now, eye, look) && hits(now, eye, sent) && hits(ahead, eye, look);
    }

    private static boolean hits(AABB box, Vec3 eye, Vec3 ray) {
        return box.clip(eye, eye.add(ray)).isPresent();
    }
}
