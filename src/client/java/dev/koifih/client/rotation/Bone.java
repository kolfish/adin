package dev.koifih.client.rotation;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RequiredArgsConstructor
public enum Bone {
    MULTIPOINT(null),
    HEAD(new Vec3(0.0, 1.75, 0.0)),
    BODY(new Vec3(0.0, 1.125, 0.0)),
    LEFT_ARM(new Vec3(0.375, 1.125, 0.0)),
    RIGHT_ARM(new Vec3(-0.375, 1.125, 0.0)),
    LEFT_LEG(new Vec3(0.125, 0.375, 0.0)),
    RIGHT_LEG(new Vec3(-0.125, 0.375, 0.0));

    public static final Bone[] ALL = values();
    public static final String[] NAMES = {"Multipoint", "Head", "Body", "Left arm", "Right arm", "Left leg", "Right leg"};
    private static final double MARGIN = 0.1;
    private static final double MARGIN_FRACTION = 0.25;
    private static final int REFINEMENTS = 2;
    private final Vec3 center;

    public Vec3 point(Entity viewer, LivingEntity entity, float partialTick) {
        Vec3 position = entity.getPosition(partialTick);
        if (center == null) {
            AABB box = entity.getBoundingBox().move(position.subtract(entity.position()));
            return closest(viewer.getEyePosition(partialTick), viewer.getLookAngle(), box);
        }
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        double angle = Math.toRadians(bodyYaw);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        Vec3 offset = center.scale(entity.getScale());
        return position.add(offset.x * cos - offset.z * sin, offset.y, offset.x * sin + offset.z * cos);
    }

    static Vec3 closest(Vec3 eye, Vec3 look, AABB box) {
        AABB inner = box.deflate(margin(box.getXsize()), margin(box.getYsize()), margin(box.getZsize()));
        Vec3 point = inner.getCenter();
        for (int i = 0; i < REFINEMENTS; i++) {
            double depth = Math.max(0.0, point.subtract(eye).dot(look));
            Vec3 along = eye.add(look.scale(depth));
            point = new Vec3(Mth.clamp(along.x, inner.minX, inner.maxX), Mth.clamp(along.y, inner.minY, inner.maxY),
                    Mth.clamp(along.z, inner.minZ, inner.maxZ));
        }
        return point;
    }

    private static double margin(double size) {
        return Math.min(MARGIN, size * MARGIN_FRACTION);
    }
}
