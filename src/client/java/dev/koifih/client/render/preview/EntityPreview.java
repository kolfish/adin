package dev.koifih.client.render.preview;

import dev.koifih.Adin;
import dev.koifih.client.mixin.FallingBlockEntityAccessor;
import dev.koifih.client.render.screen.Projector;
import dev.koifih.client.render.screen.ScreenPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EntityPreview {
    private static final Map<EntityType<?>, Optional<Entity>> ENTITIES = new HashMap<>();
    private static int nextId = -1_000_000;
    private static FallingBlockEntity fallingBlock;

    private EntityPreview() {}

    public static void clear() {
        ENTITIES.clear();
        fallingBlock = null;
    }

    private static Optional<Entity> entity(EntityType<?> type) {
        return ENTITIES.computeIfAbsent(type, EntityPreview::create);
    }

    private static Optional<Entity> create(EntityType<?> type) {
        var level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        try {
            Entity entity = type.create(level, EntitySpawnReason.LOAD);
            if (entity == null) return Optional.empty();
            entity.setId(nextId--);
            return Optional.of(entity);
        } catch (Exception exception) {
            Adin.LOGGER.debug("Cannot preview entity {}", type, exception);
            return Optional.empty();
        }
    }

    public static boolean canPreviewEntity(EntityType<?> type) {
        return entity(type).isPresent();
    }

    public static void drawEntity(GuiGraphicsExtractor graphics, EntityType<?> type, int x0, int y0, int x1, int y1, float yawDegrees) {
        Entity entity = entity(type).orElse(null);
        if (entity == null) return;
        entity.setYRot(yawDegrees);
        entity.setXRot(0f);
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yawDegrees;
            living.yBodyRotO = yawDegrees;
            living.yHeadRot = yawDegrees;
            living.yHeadRotO = yawDegrees;
        }
        if (!render(graphics, entity, x0, y0, x1, y1, 0f, null)) ENTITIES.put(type, Optional.empty());
    }

    public static void drawBlock(GuiGraphicsExtractor graphics, Block block, int x0, int y0, int x1, int y1, float yawDegrees) {
        if (fallingBlock == null) {
            Entity entity = entity(EntityTypes.FALLING_BLOCK).orElse(null);
            if (!(entity instanceof FallingBlockEntity falling)) return;
            fallingBlock = falling;
        }
        ((FallingBlockEntityAccessor) fallingBlock).adin$setBlockState(block.defaultBlockState());
        render(graphics, fallingBlock, x0, y0, x1, y1, yawDegrees, null);
    }

    public static boolean drawPlayer(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float sceneYawDegrees, PlayerSkin skin) {
        var player = Minecraft.getInstance().player;
        return player != null && render(graphics, player, x0, y0, x1, y1, sceneYawDegrees, skin);
    }

    public static Projector playerProjector(int x0, int y0, int x1, int y1, float sceneYawDegrees) {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        float scale = fitScale(player, x0, y0, x1, y1);
        Quaternionf rotation = sceneRotation(sceneYawDegrees);
        Vector3f lift = new Vector3f(0f, player.getBbHeight() / 2f, 0f);
        float centerX = (x0 + x1) * 0.5f;
        float centerY = (y0 + y1) * 0.5f;
        return (x, y, z) -> {
            Vector3f point = rotation.transform(new Vector3f((float) x, (float) y, (float) z)).add(lift).mul(scale);
            return new ScreenPoint(centerX + point.x, centerY + point.y, point.z);
        };
    }

    public static AABB playerLocalBounds() {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        double half = player.getBbWidth() / 2.0;
        return new AABB(-half, 0.0, -half, half, player.getBbHeight(), half);
    }

    private static float fitScale(Entity entity, int x0, int y0, int x1, int y1) {
        float height = Math.max(0.3f, entity.getBbHeight());
        float width = Math.max(0.3f, entity.getBbWidth());
        return Math.min((y1 - y0) * 0.5f / height, (x1 - x0) * 0.55f / width);
    }

    private static Quaternionf sceneRotation(float sceneYawDegrees) {
        Quaternionf camera = new Quaternionf().rotateX((float) Math.toRadians(15));
        return new Quaternionf().rotateZ((float) Math.PI).mul(camera).rotateY((float) Math.toRadians(sceneYawDegrees));
    }

    private static boolean render(GuiGraphicsExtractor graphics, Entity entity, int x0, int y0, int x1, int y1, float sceneYawDegrees, PlayerSkin skin) {
        var player = Minecraft.getInstance().player;
        if (player != null && entity != player) entity.setPos(player.getX(), player.getY(), player.getZ());
        EntityRenderState state;
        try {
            EntityRenderer<? super Entity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            state = renderer.createRenderState(entity, 1f);
        } catch (Exception exception) {
            Adin.LOGGER.debug("Cannot extract preview state for {}", entity.getType(), exception);
            return false;
        }
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (skin != null && state instanceof AvatarRenderState avatar) avatar.skin = skin;
        float scale = fitScale(entity, x0, y0, x1, y1);
        Quaternionf camera = new Quaternionf().rotateX((float) Math.toRadians(15));
        graphics.entity(state, scale, new Vector3f(0f, entity.getBbHeight() / 2f, 0f), sceneRotation(sceneYawDegrees), camera, x0, y0, x1, y1);
        return true;
    }
}
