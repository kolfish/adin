package dev.koifih.client.ui.preview;

import dev.koifih.client.render.AdinIcon;
import dev.koifih.client.ui.component.Segmented;
import dev.koifih.client.util.Time;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

public enum PreviewPose {
    IDLE(AdinIcon.IDLE),
    WALK(AdinIcon.WALK),
    SNEAK(AdinIcon.SNEAK),
    SWIM(AdinIcon.SWIM),
    FLY(AdinIcon.FLY);

    public static final float WALK_RATE = 10f;
    public static final float WALK_SPEED = 0.8f;

    private static final PreviewPose[] ALL = values();
    private static PreviewPose selected = IDLE;

    private final AdinIcon icon;

    PreviewPose(AdinIcon icon) {
        this.icon = icon;
    }

    public static int index() {
        return selected.ordinal();
    }

    public static void select(int index) {
        selected = ALL[Math.clamp(index, 0, ALL.length - 1)];
    }

    public static Segmented.Segment[] segments() {
        Segmented.Segment[] segments = new Segmented.Segment[ALL.length];
        for (int i = 0; i < ALL.length; i++) segments[i] = Segmented.Segment.of(ALL[i].icon);
        return segments;
    }

    public static void apply(EntityRenderState state) {
        float now = Time.seconds();
        if (state instanceof HumanoidRenderState humanoid) selected.pose(humanoid, now);
        if (state instanceof AvatarRenderState avatar) selected.cape(avatar, now);
    }

    private void pose(HumanoidRenderState humanoid, float now) {
        switch (this) {
            case WALK -> {
                humanoid.walkAnimationPos = now * WALK_RATE;
                humanoid.walkAnimationSpeed = WALK_SPEED;
            }
            case SNEAK -> {
                humanoid.isCrouching = true;
                humanoid.pose = Pose.CROUCHING;
            }
            case SWIM -> {
                humanoid.isVisuallySwimming = true;
                humanoid.swimAmount = 1f;
                humanoid.pose = Pose.SWIMMING;
                humanoid.walkAnimationPos = now * 6f;
                humanoid.walkAnimationSpeed = 0.5f;
            }
            case FLY -> {
                humanoid.isFallFlying = true;
                humanoid.pose = Pose.FALL_FLYING;
            }
            default -> {
            }
        }
    }

    private void cape(AvatarRenderState avatar, float now) {
        switch (this) {
            case WALK -> {
                avatar.capeFlap = 22f + 7f * Mth.sin(now * WALK_RATE);
                avatar.capeLean = 10f;
                avatar.capeLean2 = 3f * Mth.sin(now * WALK_RATE * 0.5f);
            }
            case SNEAK -> {
                avatar.capeFlap = 12f + 2f * Mth.sin(now * 2f);
                avatar.capeLean = 5f;
            }
            case SWIM -> {
                avatar.capeFlap = 34f + 5f * Mth.sin(now * 2.2f);
                avatar.capeLean = 10f + 2f * Mth.sin(now * 1.4f);
                avatar.capeLean2 = 3f * Mth.sin(now * 1.1f);
            }
            case FLY -> {
                avatar.capeFlap = 48f + 10f * Mth.sin(now * 7f) + 4f * Mth.sin(now * 13f);
                avatar.capeLean = 16f + 3f * Mth.sin(now * 9f);
                avatar.capeLean2 = 6f * Mth.sin(now * 5.5f);
            }
            default -> {
                avatar.capeFlap = 5f + 3f * Mth.sin(now * 1.4f);
                avatar.capeLean = 2f + 1.5f * Mth.sin(now * 0.9f);
                avatar.capeLean2 = 0f;
            }
        }
    }
}
