package dev.koifih.client.render.cape;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.koifih.Adin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.ClientAsset;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClothCape {
    public static boolean enabled;

    private static final float TICKS_PER_SECOND = 20f;
    private static final float MIN_FRAME = 1e-3f;

    private static final ClothSimulation CLOTH = new ClothSimulation();
    private static boolean started;
    private static double lastTime;
    private static float lastBodyYaw;

    public static boolean render(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state) {
        if (!wearsClothCape(state)) return false;
        advance(state);
        ClientAsset.Texture cape = state.skin.cape();
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(cape.texturePath()),
                (entry, consumer) -> ClothMesh.emit(CLOTH, entry, consumer, light));
        return true;
    }

    private static boolean wearsClothCape(AvatarRenderState state) {
        if (!enabled || !state.showCape) return false;
        if (state.isFallFlying || state.isVisuallySwimming) return false;
        ClientAsset.Texture cape = state.skin.cape();
        return cape != null && Adin.MOD_ID.equals(cape.texturePath().getNamespace());
    }

    private static void advance(AvatarRenderState state) {
        double now = System.nanoTime() / 1.0e9;
        if (!started) {
            started = true;
            lastTime = now;
            lastBodyYaw = state.bodyRot;
            CLOTH.reset();
        }
        float frame = (float) Math.clamp(now - lastTime, 0.0, ClothSimulation.MAX_FRAME);
        lastTime = now;
        CLOTH.advance(motion(state, frame), frame);
    }

    private static ClothSimulation.Motion motion(AvatarRenderState state, float frame) {
        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 velocity = player == null ? Vec3.ZERO : player.getDeltaMovement();
        float yaw = (float) Math.toRadians(state.bodyRot);
        float facingX = -Mth.sin(yaw);
        float facingZ = Mth.cos(yaw);
        float forward = (float) (velocity.x * facingX + velocity.z * facingZ) * TICKS_PER_SECOND;
        float lateral = (float) (velocity.x * facingZ - velocity.z * facingX) * TICKS_PER_SECOND;
        float vertical = (float) velocity.y * TICKS_PER_SECOND;
        float spin = Mth.wrapDegrees(state.bodyRot - lastBodyYaw) / Math.max(frame, MIN_FRAME);
        lastBodyYaw = state.bodyRot;
        return new ClothSimulation.Motion(forward, lateral, vertical, spin, state.isCrouching);
    }
}
