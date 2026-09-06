package dev.koifih.client.util;

public final class Colors {
    private Colors() {}

    public static int lerp(int from, int to, float t) {
        t = Math.clamp(t, 0f, 1f);
        int a = channel(from >>> 24, to >>> 24, t);
        int r = channel((from >> 16) & 255, (to >> 16) & 255, t);
        int g = channel((from >> 8) & 255, (to >> 8) & 255, t);
        int b = channel(from & 255, to & 255, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int withAlpha(int argb, float alpha) {
        int scaled = Math.round((argb >>> 24) * Math.clamp(alpha, 0f, 1f));
        return (scaled << 24) | (argb & 0xFFFFFF);
    }

    public static int darken(int rgb, float amount) {
        int r = Math.round(((rgb >> 16) & 255) * (1f - amount));
        int g = Math.round(((rgb >> 8) & 255) * (1f - amount));
        int b = Math.round((rgb & 255) * (1f - amount));
        return (r << 16) | (g << 8) | b;
    }

    public static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static int channel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
