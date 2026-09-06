package dev.koifih.client.render.screen;

import com.mojang.math.Axis;
import dev.koifih.client.render.BoxCorners;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class Projection implements Projector {
    public static final float NEAR = 0.05f;

    private final Matrix4f viewProjection;
    private final Vec3 origin;
    private final float width;
    private final float height;
    private final float pixel;

    public Projection(Matrix4fc viewProjection, Vec3 origin, float width, float height, float pixel) {
        this.viewProjection = new Matrix4f(viewProjection);
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.pixel = pixel;
    }

    public static Projection capture() {
        GameRenderState state = Minecraft.getInstance().gameRenderer.gameRenderState();
        CameraRenderState camera = state.levelRenderState.cameraRenderState;
        WindowRenderState window = state.windowRenderState;
        float pixel = 1f / Math.max(1, window.guiScale);
        return new Projection(viewProjection(camera, state.optionsRenderState), camera.pos,
                window.width * pixel, window.height * pixel, pixel);
    }

    @Override
    public ScreenPoint project(double x, double y, double z) {
        Vector4f clip = clip(x, y, z, new Vector4f());
        return inFront(clip) ? toScreen(clip) : null;
    }

    public ScreenRect bounds(AABB box) {
        Vector4f[] corners = new Vector4f[BoxCorners.COUNT];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = clip(BoxCorners.x(box, i), BoxCorners.y(box, i), BoxCorners.z(box, i), new Vector4f());
        }
        ScreenExtent extent = new ScreenExtent();
        for (Vector4f corner : corners) {
            if (inFront(corner)) extent.add(toScreen(corner));
        }
        for (int i = 0; i < BoxCorners.EDGES.length; i += 2) {
            Vector4f a = corners[BoxCorners.EDGES[i]];
            Vector4f b = corners[BoxCorners.EDGES[i + 1]];
            if (inFront(a) != inFront(b)) extent.add(toScreen(nearIntersection(a, b)));
        }
        return extent.toRect();
    }

    public float pixel() {
        return pixel;
    }

    private Vector4f clip(double x, double y, double z, Vector4f dest) {
        return viewProjection.transform((float) (x - origin.x), (float) (y - origin.y), (float) (z - origin.z), 1f, dest);
    }

    private ScreenPoint toScreen(Vector4fc clip) {
        float inverse = 1f / clip.w();
        return new ScreenPoint((clip.x() * inverse + 1f) * 0.5f * width, (1f - clip.y() * inverse) * 0.5f * height, clip.w());
    }

    private static boolean inFront(Vector4fc clip) {
        return clip.w() > NEAR;
    }

    private static Vector4f nearIntersection(Vector4f a, Vector4f b) {
        return new Vector4f(a).lerp(b, (NEAR - a.w) / (b.w - a.w));
    }

    private static Matrix4f viewProjection(CameraRenderState camera, OptionsRenderState options) {
        Matrix4f bob = new Matrix4f();
        hurtTilt(camera.entityRenderState, options, bob);
        if (options.bobView) viewBob(camera.entityRenderState, bob);
        return new Matrix4f(camera.projectionMatrix).mul(bob).mul(camera.viewRotationMatrix);
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
}
