package dev.koifih.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectilePrediction {
    public static final float ARROW_SPEED = 3f;
    private static final float ARROW_DRAG = 0.99f;
    private static final float ARROW_GRAVITY = 0.05f;
    private static final double EYE_DROP = 0.1;
    private static final int FLIGHT_TICKS = 100;

    public static BlockHitResult arrow(LocalPlayer player, float speed) {
        return landing(player, player.getEyePosition().subtract(0.0, EYE_DROP, 0.0),
                Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(speed), ARROW_DRAG, ARROW_GRAVITY);
    }

    public static BlockHitResult arrow(LocalPlayer player, AbstractArrow arrow) {
        return landing(player, arrow.position(), arrow.getDeltaMovement(), ARROW_DRAG, ARROW_GRAVITY);
    }

    public static BlockHitResult landing(LocalPlayer player, Vec3 position, Vec3 motion, float drag, float gravity) {
        for (int tick = 0; tick < FLIGHT_TICKS; tick++) {
            Vec3 next = position.add(motion);
            BlockHitResult hit = Game.level().clip(new ClipContext(position, next, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK) return hit;
            position = next;
            motion = motion.scale(drag).subtract(0.0, gravity, 0.0);
        }
        return null;
    }
}
