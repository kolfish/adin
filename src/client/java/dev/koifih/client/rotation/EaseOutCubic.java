package dev.koifih.client.rotation;

final class EaseOutCubic implements Rotator {
    private static final float SLOWEST_TICKS = 40f;

    @Override
    public Rotation step(Rotation from, Rotation target, RotationConfig config, float deltaTicks) {
        float duration = config.scaled(SLOWEST_TICKS, 0f);
        float remaining = duration <= 0f ? 0f : 1f - Math.min(1f, deltaTicks / duration);
        float fraction = 1f - remaining * remaining * remaining;
        return from.moved(from.yawTo(target) * fraction, from.pitchTo(target) * fraction);
    }
}
