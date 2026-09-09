package dev.koifih.client.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.ThreadLocalRandom;

public final class Maths {
    private static final float EPSILON = 1e-6f;

    private Maths() {}

    public static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static boolean nearlyZero(float value) {
        return Math.abs(value) < EPSILON;
    }

    public static float min(float first, float... rest) {
        float result = first;
        for (float value : rest) result = Math.min(result, value);
        return result;
    }

    public static double angle(Vec3 first, Vec3 second) {
        double cosine = first.normalize().dot(second.normalize());
        return Math.toDegrees(Math.acos(Mth.clamp(cosine, -1.0, 1.0)));
    }

    public static float random(float low, float high) {
        return high > low ? ThreadLocalRandom.current().nextFloat(low, high) : low;
    }

    public static float max(float first, float... rest) {
        float result = first;
        for (float value : rest) result = Math.max(result, value);
        return result;
    }
}
