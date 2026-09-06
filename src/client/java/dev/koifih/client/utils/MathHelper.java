package dev.koifih.client.utils;

public final class MathHelper {
    public static final float EPSILON = 1e-6f;
    public static final float PI = (float) Math.PI;
    public static final float TAU = PI * 2f;
    public static final float DEG_TO_RAD = PI / 180f;
    public static final float RAD_TO_DEG = 180f / PI;

    private MathHelper() {}

    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    public static float inverseLerp(float from, float to, float value) {
        float range = to - from;
        return nearlyZero(range) ? 0f : (value - from) / range;
    }

    public static float remap(float value, float fromMin, float fromMax, float toMin, float toMax) {
        return lerp(toMin, toMax, inverseLerp(fromMin, fromMax, value));
    }

    public static float approach(float current, float target, float maxDelta) {
        float delta = target - current;
        return Math.abs(delta) <= maxDelta ? target : current + Math.copySign(maxDelta, delta);
    }

    public static float square(float value) {
        return value * value;
    }

    public static double square(double value) {
        return value * value;
    }

    public static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static float distance(float x0, float y0, float x1, float y1) {
        return length(x1 - x0, y1 - y0);
    }

    public static double distanceSq(double x0, double y0, double z0, double x1, double y1, double z1) {
        return square(x1 - x0) + square(y1 - y0) + square(z1 - z0);
    }

    public static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360f;
        if (wrapped >= 180f) wrapped -= 360f;
        if (wrapped < -180f) wrapped += 360f;
        return wrapped;
    }

    public static float toRadians(float degrees) {
        return degrees * DEG_TO_RAD;
    }

    public static float toDegrees(float radians) {
        return radians * RAD_TO_DEG;
    }

    public static float roundTo(float value, float step) {
        return step <= 0f ? value : Math.round(value / step) * step;
    }

    public static boolean nearlyZero(float value) {
        return Math.abs(value) < EPSILON;
    }

    public static boolean nearlyEqual(float a, float b) {
        return nearlyZero(a - b);
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
