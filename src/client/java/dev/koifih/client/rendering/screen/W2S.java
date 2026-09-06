package dev.koifih.client.rendering.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class W2S {
    public static final float NEAR = 0.05f;

    private final Matrix4f viewProjection;
    private final Vec3 origin;
    private final float width;
    private final float height;
    private final float pixel;

    public W2S(Matrix4fc viewProjection, Vec3 origin, float width, float height, float pixel) {
        this.viewProjection = new Matrix4f(viewProjection);
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.pixel = pixel;
    }

    public static W2S capture() {
        GameRenderState state = Minecraft.getInstance().gameRenderer.gameRenderState();
        CameraRenderState camera = state.levelRenderState.cameraRenderState;
        WindowRenderState window = state.windowRenderState;
        Matrix4fc viewProjection = Projections.levelViewProjection(camera, state.optionsRenderState);
        float pixel = 1f / Math.max(1, window.guiScale);
        return new W2S(viewProjection, camera.pos, window.width * pixel, window.height * pixel, pixel);
    }

    public Vector4f clip(double x, double y, double z, Vector4f dest) {
        return viewProjection.transform((float) (x - origin.x), (float) (y - origin.y), (float) (z - origin.z), 1f, dest);
    }

    public ScreenPoint project(double x, double y, double z) {
        Vector4f clip = clip(x, y, z, new Vector4f());
        return inFront(clip) ? toScreen(clip) : null;
    }

    public ScreenPoint project(Vec3 position) {
        return project(position.x, position.y, position.z);
    }

    public ScreenPoint toScreen(Vector4fc clip) {
        float inverse = 1f / clip.w();
        float x = (clip.x() * inverse + 1f) * 0.5f * width;
        float y = (1f - clip.y() * inverse) * 0.5f * height;
        return new ScreenPoint(x, y, clip.w());
    }

    public static boolean inFront(Vector4fc clip) {
        return clip.w() > NEAR;
    }

    public boolean onScreen(ScreenPoint point) {
        return point != null && point.x() >= 0f && point.x() <= width && point.y() >= 0f && point.y() <= height;
    }

    public Vec3 origin() {
        return origin;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float pixel() {
        return pixel;
    }
}
