package dev.koifih.client.rotation;

import dev.koifih.client.util.Maths;
import java.util.concurrent.ThreadLocalRandom;

final class WindMouse implements Rotator {
    private static final float FASTEST = 14f;
    private static final float SLOWEST = 1f;
    private static final float GRAVITY = 9f / 15f;
    private static final float WIND = 3f / 15f;
    private static final float SETTLE = 12f / 15f;
    private static final float FLOOR = 3f / 15f;
    private static final float FLOOR_TOLERANCE = 1.0001f;
    private static final float DONE = 1f / 15f;
    private static final float FULL_TURN = 60f;
    private static final float SHORT_PACE = 0.4f;
    private static final float PACE_JITTER = 0.15f;
    private static final float SQRT3 = 1.7320508f;
    private static final float SQRT5 = 2.236068f;

    private Rotation node;
    private Rotation next;
    private Rotation expected;
    private float phase;
    private float velocityYaw;
    private float velocityPitch;
    private float windYaw;
    private float windPitch;
    private float cap;
    private float pace = SHORT_PACE;
    private boolean travelling;
    private boolean settled;

    @Override
    public Rotation step(Rotation from, Rotation target, RotationConfig config, float deltaTicks) {
        if (config.smoothness() <= 0f) {
            rest();
            next = null;
            return from.moved(from.yawTo(target), from.pitchTo(target));
        }
        if (next == null) {
            node = from;
            next = advance(from, target, config);
            phase = 0f;
        } else {
            float driftYaw = expected.yawTo(from);
            float driftPitch = expected.pitchTo(from);
            if (driftYaw != 0f || driftPitch != 0f) {
                node = node.moved(driftYaw, driftPitch);
                next = next.moved(driftYaw, driftPitch);
            }
        }
        phase += deltaTicks;
        while (phase >= 1f) {
            phase -= 1f;
            node = next;
            next = advance(node, target, config);
        }
        expected = lerp(node, next, phase);
        return expected;
    }

    private Rotation advance(Rotation from, Rotation target, RotationConfig config) {
        float deltaYaw = from.yawTo(target);
        float deltaPitch = from.pitchTo(target);
        float distance = Maths.length(deltaYaw, deltaPitch);
        float base = config.scaled(SLOWEST, FASTEST);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (!travelling || settled && distance >= base * SETTLE) {
            float reach = Math.min(1f, distance / FULL_TURN);
            pace = (SHORT_PACE + (1f - SHORT_PACE) * reach) * random.nextFloat(1f - PACE_JITTER, 1f + PACE_JITTER);
            cap = base * pace;
            travelling = true;
            settled = false;
        }
        float speed = base * pace;
        if (distance < speed * DONE) {
            rest();
            return from.moved(deltaYaw, deltaPitch);
        }
        if (distance >= speed * SETTLE) {
            float gust = Math.min(speed * WIND, distance) / SQRT5;
            windYaw = windYaw / SQRT3 + random.nextFloat(-1f, 1f) * gust;
            windPitch = windPitch / SQRT3 + random.nextFloat(-1f, 1f) * gust;
        } else {
            windYaw /= SQRT3;
            windPitch /= SQRT3;
            if (cap < speed * FLOOR * FLOOR_TOLERANCE) cap = speed * (FLOOR + FLOOR * random.nextFloat());
            else cap /= SQRT5;
            settled = true;
        }
        float gravity = speed * GRAVITY;
        velocityYaw += windYaw + gravity * deltaYaw / distance;
        velocityPitch += windPitch + gravity * deltaPitch / distance;
        float magnitude = Maths.length(velocityYaw, velocityPitch);
        if (magnitude > cap) {
            float clipped = cap * (0.5f + 0.5f * random.nextFloat());
            velocityYaw *= clipped / magnitude;
            velocityPitch *= clipped / magnitude;
        }
        return from.moved(velocityYaw, velocityPitch);
    }

    private void rest() {
        windYaw = 0f;
        windPitch = 0f;
        velocityYaw = 0f;
        velocityPitch = 0f;
        travelling = false;
        settled = false;
    }

    private static Rotation lerp(Rotation from, Rotation to, float phase) {
        if (phase <= 0f) return from;
        return new Rotation(from.yaw() + (to.yaw() - from.yaw()) * phase, from.pitch() + (to.pitch() - from.pitch()) * phase);
    }
}
