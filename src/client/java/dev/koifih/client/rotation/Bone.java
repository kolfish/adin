package dev.koifih.client.rotation;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public enum Bone {
    HEAD(0.0, 1.75, 0.0),
    BODY(0.0, 1.125, 0.0),
    LEFT_ARM(0.375, 1.125, 0.0),
    RIGHT_ARM(-0.375, 1.125, 0.0),
    LEFT_LEG(0.125, 0.375, 0.0),
    RIGHT_LEG(-0.125, 0.375, 0.0);

    public static final Bone[] ALL = values();
    public static final String[] NAMES = {"Head", "Body", "Left arm", "Right arm", "Left leg", "Right leg"};
    private final Vec3 center;

    Bone(double x, double y, double z) {
        center = new Vec3(x, y, z);
    }

    public Vec3 center(LivingEntity entity, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        double angle = Math.toRadians(bodyYaw);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        Vec3 offset = center.scale(entity.getScale());
        return entity.getPosition(partialTick).add(offset.x * cos - offset.z * sin, offset.y, offset.x * sin + offset.z * cos);
    }
}
