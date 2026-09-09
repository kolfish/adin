package dev.koifih.client.rotation;

import dev.koifih.client.util.Maths;
import java.util.concurrent.ThreadLocalRandom;

final class WindMouse implements Rotator {
    private static final float FASTEST = 14f;
    private static final float SLOWEST = 1f;
    private static final float GRAVITY = 0.6f;
    private static final float WIND = 0.25f;
    private static final float SETTLE_TICKS = 1f;
    private static final float MIN_CAP = 0.2f;
    private static final float DONE = 0.02f;
    private static final float FULL_TURN = 60f;
    private static final float SHORT_PACE = 0.4f;
    private static final float PACE_JITTER = 0.15f;
    private static final float SQRT3 = 1.7320508f;
    private static final float SQRT5 = 2.236068f;

    private float velocityYaw;
    private float velocityPitch;
    private float windYaw;
    private float windPitch;
    private float cap;
    private float pace = SHORT_PACE;
    private boolean travelling;

    @Override
    public Rotation step(Rotation from, Rotation target, RotationConfig config, float deltaTicks) {
        float deltaYaw = from.yawTo(target);
        float deltaPitch = from.pitchTo(target);
        float distance = Maths.length(deltaYaw, deltaPitch);
        if (distance < DONE) {
            velocityYaw = 0f;
            velocityPitch = 0f;
            travelling = false;
            return from.moved(deltaYaw, deltaPitch);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float base = config.scaled(SLOWEST, FASTEST);
        float settle = base * SETTLE_TICKS;
        if (!travelling && distance >= settle) {
            float reach = Math.min(1f, distance / FULL_TURN);
            pace = (SHORT_PACE + (1f - SHORT_PACE) * reach) * random.nextFloat(1f - PACE_JITTER, 1f + PACE_JITTER);
            travelling = true;
        }
        float speed = base * pace;
        float decay = (float) Math.pow(1f / SQRT3, deltaTicks);
        if (distance >= settle) {
            cap = speed;
            float gust = Math.min(speed * WIND, distance) / SQRT5 * (float) Math.sqrt(deltaTicks);
            windYaw = windYaw * decay + random.nextFloat(-1f, 1f) * gust;
            windPitch = windPitch * decay + random.nextFloat(-1f, 1f) * gust;
        } else {
            windYaw *= decay;
            windPitch *= decay;
            cap = Math.max(Math.max(speed * MIN_CAP, distance), cap / (float) Math.pow(SQRT5, deltaTicks));
            travelling = false;
        }
        float gravity = speed * GRAVITY;
        velocityYaw += (windYaw + gravity * deltaYaw / distance) * deltaTicks;
        velocityPitch += (windPitch + gravity * deltaPitch / distance) * deltaTicks;
        float magnitude = Maths.length(velocityYaw, velocityPitch);
        if (magnitude > cap) {
            float clipped = cap * (0.5f + 0.5f * random.nextFloat());
            velocityYaw *= clipped / magnitude;
            velocityPitch *= clipped / magnitude;
            magnitude = clipped;
        }
        float length = magnitude * deltaTicks;
        if (distance < settle && length > distance) {
            float scale = distance / length;
            velocityYaw *= scale;
            velocityPitch *= scale;
        }
        return from.moved(velocityYaw * deltaTicks, velocityPitch * deltaTicks);
    }
}
