package dev.koifih.client.feature.render;

import dev.koifih.client.event.WorldExtractEvent;
import dev.koifih.client.feature.Category;
import dev.koifih.client.feature.Feature;
import dev.koifih.client.feature.setting.BoolSetting;
import dev.koifih.client.feature.setting.ColorSetting;
import dev.koifih.client.feature.setting.EntitySetting;
import dev.koifih.client.feature.setting.EnumSetting;
import dev.koifih.client.feature.setting.Measure;
import dev.koifih.client.feature.setting.MultiSetting;
import dev.koifih.client.feature.setting.SliderSetting;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.rendering.world.BoxStyle;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class Esp extends Feature {
    private static final String[] MODES = {"3D"};
    private static final String[] TARGETS = {"Self", "Players", "Entities"};
    private static final int SELF = 0;
    private static final int PLAYERS = 1;
    private static final int ENTITIES = 2;
    private static final int FILL_ALPHA = 0x40;
    private static final float LINE_WIDTH = 2f;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final float OUTLINE_WIDTH = LINE_WIDTH + 2f;
    private static final double FULL_WIDTH_DISTANCE = 10.0;

    private final EnumSetting mode = add(new EnumSetting("mode", 0, MODES));
    private final SliderSetting distance = add(new SliderSetting("distance", 64, 8, 256, Measure.DISTANCE));
    private final ColorSetting color = add(new ColorSetting("color", Theme.DEFAULT_ACCENT));
    private final BoolSetting fill = add(new BoolSetting("fill", true));
    private final BoolSetting outline = add(new BoolSetting("outline", true));
    private final MultiSetting targets = add(new MultiSetting("targets", TARGETS, PLAYERS, ENTITIES));
    private final EntitySetting entities = add(new EntitySetting("entities"));

    public Esp() {
        super("esp", Category.RENDER);
        entities.visibleWhen(() -> targets.get().contains(ENTITIES));
    }

    @Override
    protected void onEnable() {
        listen(WorldExtractEvent.class, this::onExtract);
    }

    private void onExtract(WorldExtractEvent event) {
        Vec3 origin = event.camera().position();
        double range = (double) distance.get() * distance.get();
        BoxStyle style = style();
        for (Entity entity : event.level().entitiesForRendering()) {
            if (!targeted(entity, event.camera())) continue;
            double distanceSq = entity.distanceToSqr(origin);
            if (distanceSq > range) continue;
            event.shapes().box(event.interpolatedBounds(entity), style.scaled(perspective(distanceSq)));
        }
    }

    private boolean targeted(Entity entity, Camera camera) {
        if (entity.isRemoved() || entity.isSpectator()) return false;
        if (entity == camera.entity() && !camera.isDetached()) return false;
        if (entity instanceof LocalPlayer) return targets.get().contains(SELF);
        if (entity instanceof Player) return targets.get().contains(PLAYERS);
        return targets.get().contains(ENTITIES) && entities.get().contains(typeId(entity));
    }

    private static float perspective(double distanceSq) {
        if (distanceSq <= FULL_WIDTH_DISTANCE * FULL_WIDTH_DISTANCE) return 1f;
        return (float) (FULL_WIDTH_DISTANCE / Math.sqrt(distanceSq));
    }

    private BoxStyle style() {
        int rgb = color.get();
        BoxStyle style = BoxStyle.stroke(0xFF000000 | rgb, LINE_WIDTH);
        if (fill.get()) style = style.withFill((FILL_ALPHA << 24) | rgb);
        if (outline.get()) style = style.withOutline(OUTLINE_COLOR, OUTLINE_WIDTH);
        return style;
    }

    private static String typeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
