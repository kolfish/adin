package dev.koifih.client.util;

public final class Time {
    private static final float NANOS_PER_SECOND = 1_000_000_000f;
    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final long ORIGIN = System.nanoTime();

    private Time() {}

    public static float seconds() {
        return (System.nanoTime() - ORIGIN) / NANOS_PER_SECOND;
    }

    public static boolean blink(long millis) {
        return ((System.nanoTime() - ORIGIN) / (millis * NANOS_PER_MILLI)) % 2 == 0;
    }

    public static final class Stopwatch {
        private long startedAt = System.nanoTime();

        public void reset() {
            startedAt = System.nanoTime();
        }

        public long elapsedMillis() {
            return (System.nanoTime() - startedAt) / NANOS_PER_MILLI;
        }

        public boolean elapsed(long millis) {
            return elapsedMillis() >= millis;
        }
    }
}
