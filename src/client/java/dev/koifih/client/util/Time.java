package dev.koifih.client.util;

/** Monotonic wall-clock helpers, so timing code does not hand-roll {@link System#nanoTime()}. */
public final class Time {
    private static final float NANOS_PER_SECOND = 1_000_000_000f;
    private static final long NANOS_PER_MILLI = 1_000_000L;
    /** Anchors {@link #seconds()} near zero so float precision stays fine on a long-lived JVM. */
    private static final long ORIGIN = System.nanoTime();

    private Time() {}

    /** Seconds since the client started. */
    public static float seconds() {
        return (System.nanoTime() - ORIGIN) / NANOS_PER_SECOND;
    }

    /** Alternates between true and false every {@code millis}, for blinking carets and the like. */
    public static boolean blink(long millis) {
        return ((System.nanoTime() - ORIGIN) / (millis * NANOS_PER_MILLI)) % 2 == 0;
    }

    /** A restartable elapsed-time measurement. */
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
