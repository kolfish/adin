package dev.koifih.client.render.cape;

import net.minecraft.util.Mth;

public final class ClothSimulation {
    public record Motion(float forward, float lateral, float vertical, float spin, boolean crouching) {}

    static final int COLS = 8;
    static final int ROWS = 12;

    private static final float WIDTH = 0.62f;
    private static final float LENGTH = 1.05f;
    private static final float CELL_WIDTH = WIDTH / (COLS - 1);
    private static final float CELL_HEIGHT = LENGTH / (ROWS - 1);
    private static final float STEP = 1f / 90f;
    static final float MAX_FRAME = 0.1f;
    private static final int ITERATIONS = 4;
    private static final float DAMPING = 0.985f;
    private static final float GRAVITY = 16f;
    private static final float FORWARD_DRAG = 4.5f;
    private static final float BACKWARD_SHARE = 0.5f;
    private static final float LATERAL_DRAG = 3f;
    private static final float VERTICAL_DRAG = 3f;
    private static final float SPIN_DRAG = 0.02f;
    private static final float REST_PUSH = 0.55f;
    private static final float MARGIN = 0.025f;
    private static final float CROUCH_RATE = 6f;
    private static final float FRICTION = 0.97f;
    private static final float SWAY = 0.3f;

    private static final float PIN_Y = 0.02f;
    private static final float PIN_Y_CROUCH = 0.06f;
    private static final float PIN_Z = 0.14f;
    private static final float PIN_Z_CROUCH = 0.05f;
    private static final float BACK_Z = 0.13f;
    private static final float SHOULDER_Y = 1.4f;
    private static final float HIP_Y = 0.8f;
    private static final float CROUCH_TOP_Y = 1.15f;
    private static final float CROUCH_SLOPE = 0.5f;
    private static final float CROUCH_HIP_Z = 0.47f;
    private static final float CROUCH_BACK_Z = 0.37f;
    private static final float OPEN = -1f;

    private final float[] posX = new float[ROWS * COLS];
    private final float[] posY = new float[ROWS * COLS];
    private final float[] posZ = new float[ROWS * COLS];
    private final float[] prevX = new float[ROWS * COLS];
    private final float[] prevY = new float[ROWS * COLS];
    private final float[] prevZ = new float[ROWS * COLS];
    private float accumulator;
    private float time;
    private float crouchAmount;

    public ClothSimulation() {
        reset();
    }

    static int index(int row, int column) {
        return row * COLS + column;
    }

    float x(int i) {
        return posX[i];
    }

    float y(int i) {
        return posY[i];
    }

    float z(int i) {
        return posZ[i];
    }

    public void reset() {
        accumulator = 0f;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int i = index(row, column);
                posX[i] = anchorX(column);
                posY[i] = PIN_Y + row * CELL_HEIGHT;
                posZ[i] = pinZ(0f) + row * 0.01f;
                prevX[i] = posX[i];
                prevY[i] = posY[i];
                prevZ[i] = posZ[i];
            }
        }
    }

    public void advance(Motion motion, float frameSeconds) {
        float frame = Math.clamp(frameSeconds, 0f, MAX_FRAME);
        if (frame <= 0f) return;
        accumulator = Math.min(accumulator + frame, MAX_FRAME);
        while (accumulator >= STEP) {
            accumulator -= STEP;
            step(motion);
        }
    }

    private static float anchorX(int column) {
        return -WIDTH / 2f + column * CELL_WIDTH;
    }

    private static float pinY(float crouch) {
        return PIN_Y + PIN_Y_CROUCH * crouch;
    }

    private static float pinZ(float crouch) {
        return PIN_Z + PIN_Z_CROUCH * crouch;
    }

    private static float backZ(float y, float crouch) {
        float standing = y < SHOULDER_Y ? BACK_Z : OPEN;
        float crouched;
        if (y <= HIP_Y) crouched = Math.min(BACK_Z + CROUCH_SLOPE * y, CROUCH_HIP_Z);
        else if (y <= CROUCH_TOP_Y) crouched = CROUCH_BACK_Z;
        else crouched = OPEN;
        return standing + (crouched - standing) * crouch;
    }

    private void step(Motion motion) {
        time += STEP;
        float crouchTarget = motion.crouching() ? 1f : 0f;
        crouchAmount += Math.clamp(crouchTarget - crouchAmount, -STEP * CROUCH_RATE, STEP * CROUCH_RATE);
        integrate(motion);
        constrain();
    }

    private void integrate(Motion motion) {
        float wind = 0.4f * Mth.sin(time * 1.7f) + 0.25f * Mth.sin(time * 2.9f);
        float forward = motion.forward();
        float accelX = motion.lateral() * LATERAL_DRAG - motion.spin() * SPIN_DRAG;
        float accelY = GRAVITY + motion.vertical() * VERTICAL_DRAG;
        float accelZ = Math.max(0f, forward) * FORWARD_DRAG - Math.min(0f, forward) * FORWARD_DRAG * BACKWARD_SHARE
                + wind + REST_PUSH;
        float dtSquared = STEP * STEP;
        for (int i = COLS; i < ROWS * COLS; i++) {
            float sway = 1f + SWAY * Mth.sin(time * 2.3f + posX[i] * 5f);
            float nx = posX[i] + (posX[i] - prevX[i]) * DAMPING + accelX * dtSquared;
            float ny = posY[i] + (posY[i] - prevY[i]) * DAMPING + accelY * dtSquared;
            float nz = posZ[i] + (posZ[i] - prevZ[i]) * DAMPING + accelZ * sway * dtSquared;
            prevX[i] = posX[i];
            prevY[i] = posY[i];
            prevZ[i] = posZ[i];
            posX[i] = nx;
            posY[i] = ny;
            posZ[i] = nz;
        }
    }

    private void constrain() {
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            for (int row = 0; row < ROWS; row++) {
                for (int column = 0; column < COLS; column++) {
                    int i = index(row, column);
                    if (column + 1 < COLS) relax(i, index(row, column + 1), CELL_WIDTH);
                    if (row + 1 < ROWS) relax(i, index(row + 1, column), CELL_HEIGHT);
                }
            }
            pin();
            collide();
        }
    }

    private void pin() {
        for (int column = 0; column < COLS; column++) {
            int i = index(0, column);
            posX[i] = anchorX(column);
            posY[i] = pinY(crouchAmount);
            posZ[i] = pinZ(crouchAmount);
        }
    }

    private void collide() {
        for (int i = 0; i < ROWS * COLS; i++) {
            float floor = backZ(posY[i], crouchAmount);
            if (floor <= 0f || posZ[i] >= floor + MARGIN) continue;
            posZ[i] = floor + MARGIN;
            prevZ[i] = posZ[i];
            prevX[i] = posX[i] + (prevX[i] - posX[i]) * FRICTION;
            prevY[i] = posY[i] + (prevY[i] - posY[i]) * FRICTION;
        }
    }

    private void relax(int a, int b, float rest) {
        float dx = posX[b] - posX[a];
        float dy = posY[b] - posY[a];
        float dz = posZ[b] - posZ[a];
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 1e-6f) return;
        float correction = (distance - rest) / distance * 0.5f;
        posX[a] += dx * correction;
        posY[a] += dy * correction;
        posZ[a] += dz * correction;
        posX[b] -= dx * correction;
        posY[b] -= dy * correction;
        posZ[b] -= dz * correction;
    }
}
