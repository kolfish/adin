package dev.koifih.client.util;

public final class MathUtil {
    private static final float EPSILON = 1e-6f;

    private MathUtil() {}

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

    public static float max(float first, float... rest) {
        float result = first;
        for (float value : rest) result = Math.max(result, value);
        return result;
    }
}
