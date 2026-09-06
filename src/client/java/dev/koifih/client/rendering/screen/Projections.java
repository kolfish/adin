package dev.koifih.client.rendering.screen;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class Projections {
    private static final int[] EDGES = {
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 2, 1, 3, 4, 6, 5, 7,
            0, 4, 1, 5, 2, 6, 3, 7
    };

    private Projections() {}

    public static Matrix4f levelViewProjection(CameraRenderState camera, OptionsRenderState options) {
        Matrix4f projection = new Matrix4f(camera.projectionMatrix);
        Matrix4f bob = new Matrix4f();
        hurtTilt(camera.entityRenderState, options, bob);
        if (options.bobView) viewBob(camera.entityRenderState, bob);
        return projection.mul(bob).mul(camera.viewRotationMatrix);
    }

    public static ScreenPoint point(W2S w2s, Vec3 position) {
        return w2s.project(position);
    }

    public static ScreenRect bounds(W2S w2s, AABB box) {
        Vector4f[] corners = corners(w2s, box);
        Bounds bounds = new Bounds();
        for (Vector4f corner : corners) {
            if (W2S.inFront(corner)) bounds.add(w2s.toScreen(corner));
        }
        for (int i = 0; i < EDGES.length; i += 2) {
            Vector4f a = corners[EDGES[i]];
            Vector4f b = corners[EDGES[i + 1]];
            if (W2S.inFront(a) != W2S.inFront(b)) bounds.add(w2s.toScreen(nearIntersection(a, b)));
        }
        return bounds.toRect();
    }

    private static Vector4f[] corners(W2S w2s, AABB box) {
        Vector4f[] corners = new Vector4f[8];
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? box.minX : box.maxX;
            double y = (i & 2) == 0 ? box.minY : box.maxY;
            double z = (i & 4) == 0 ? box.minZ : box.maxZ;
            corners[i] = w2s.clip(x, y, z, new Vector4f());
        }
        return corners;
    }

    private static Vector4f nearIntersection(Vector4f a, Vector4f b) {
        float t = (W2S.NEAR - a.w) / (b.w - a.w);
        return new Vector4f(a).lerp(b, t);
    }

    private static void hurtTilt(CameraEntityRenderState entity, OptionsRenderState options, Matrix4f pose) {
        if (!entity.isLiving) return;
        if (entity.isDeadOrDying) {
            float death = Math.min(entity.deathTime, 20f);
            pose.rotate(Axis.ZP.rotationDegrees(40f - 8000f / (death + 200f)));
        }
        if (entity.hurtTime < 0f) return;
        float progress = entity.hurtTime / entity.hurtDuration;
        float tilt = Mth.sin(progress * progress * progress * progress * Mth.PI);
        pose.rotate(Axis.YP.rotationDegrees(-entity.hurtDir));
        pose.rotate(Axis.ZP.rotationDegrees((float) (-tilt * 14.0 * options.damageTiltStrength)));
        pose.rotate(Axis.YP.rotationDegrees(entity.hurtDir));
    }

    private static void viewBob(CameraEntityRenderState entity, Matrix4f pose) {
        if (!entity.isPlayer) return;
        float walk = entity.backwardsInterpolatedWalkDistance;
        float bob = entity.bob;
        pose.translate(Mth.sin(walk * Mth.PI) * bob * 0.5f, -Math.abs(Mth.cos(walk * Mth.PI) * bob), 0f);
        pose.rotate(Axis.ZP.rotationDegrees(Mth.sin(walk * Mth.PI) * bob * 3f));
        pose.rotate(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walk * Mth.PI - 0.2f) * bob) * 5f));
    }

    private static final class Bounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;

        void add(ScreenPoint point) {
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
        }

        ScreenRect toRect() {
            return minX <= maxX ? new ScreenRect(minX, minY, maxX, maxY) : null;
        }
    }
}
