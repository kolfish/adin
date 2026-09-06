package dev.koifih.client.feature.render;

import dev.koifih.client.event.LevelFrameEvent;
import dev.koifih.client.event.ScreenExtractEvent;
import dev.koifih.client.event.WorldExtractEvent;
import dev.koifih.client.feature.Category;
import dev.koifih.client.feature.Feature;
import dev.koifih.client.feature.setting.ColorSetting;
import dev.koifih.client.feature.setting.EntitySetting;
import dev.koifih.client.feature.setting.EnumSetting;
import dev.koifih.client.feature.setting.Measure;
import dev.koifih.client.feature.setting.MultiSetting;
import dev.koifih.client.feature.setting.PreviewSetting;
import dev.koifih.client.feature.setting.SliderSetting;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.rendering.screen.EdgeStyle;
import dev.koifih.client.rendering.screen.HealthBar;
import dev.koifih.client.rendering.screen.OverlayCollector;
import dev.koifih.client.rendering.screen.Projections;
import dev.koifih.client.rendering.screen.RectStyle;
import dev.koifih.client.rendering.screen.ScreenRect;
import dev.koifih.client.rendering.world.BoxStyle;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public final class Esp extends Feature {
    private static final String[] MODES = {"3D", "2D"};
    private static final int MODE_3D = 0;
    private static final int MODE_2D = 1;
    private static final String[] TYPES = {"Full", "Cornered"};
    private static final int TYPE_CORNERED = 1;
    private static final String[] SHOW = {"Box", "Fill", "Outline", "Health"};
    private static final int SHOW_BOX = 0;
    private static final int SHOW_FILL = 1;
    private static final int SHOW_OUTLINE = 2;
    private static final int SHOW_HEALTH = 3;
    private static final String[] HEALTH_POSITIONS = {"Left", "Right", "Top", "Bottom"};
    private static final HealthBar.Side[] HEALTH_SIDES = {HealthBar.Side.LEFT, HealthBar.Side.RIGHT, HealthBar.Side.TOP, HealthBar.Side.BOTTOM};
    private static final String[] TARGETS = {"Self", "Players", "Entities"};
    private static final int SELF = 0;
    private static final int PLAYERS = 1;
    private static final int ENTITIES = 2;
    private static final float LINE_WIDTH = 2f;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final float OUTLINE_WIDTH = LINE_WIDTH + 2f;
    private static final float SCREEN_LINE_WIDTH = 1f;
    private static final float SCREEN_OUTLINE_WIDTH = SCREEN_LINE_WIDTH + 1.5f;
    private static final double FULL_WIDTH_DISTANCE = 10.0;
    private static final double PLAYER_SIDE_PADDING = 0.12;
    private static final double PLAYER_TOP_PADDING = 0.06;

    private final EnumSetting mode = add(new EnumSetting("mode", MODE_3D, MODES));
    private final EnumSetting type = add(new EnumSetting("type", 0, TYPES));
    private final SliderSetting distance = add(new SliderSetting("distance", 64, 8, 256, Measure.DISTANCE));
    private final ColorSetting color = add(new ColorSetting("color", Theme.DEFAULT_ACCENT));
    private final MultiSetting show = add(new MultiSetting("show", SHOW, SHOW_BOX, SHOW_FILL, SHOW_OUTLINE));
    private final SliderSetting fillOpacity = add(new SliderSetting("fillOpacity", 25, 0, 100, Measure.PERCENT));
    private final EnumSetting healthPosition = add(new EnumSetting("healthPosition", 0, HEALTH_POSITIONS));
    private final MultiSetting targets = add(new MultiSetting("targets", TARGETS, PLAYERS, ENTITIES));
    private final EntitySetting entities = add(new EntitySetting("entities"));
    private final PreviewSetting preview = add(new PreviewSetting("preview", () -> mode.get() == MODE_2D, () -> shows(SHOW_BOX),
            this::rectStyle, this::boxStyle, Esp::fitPlayer, this::healthSide));

    private record Target(Entity entity, AABB bounds, double distanceSq) {}

    public Esp() {
        super("esp", Category.RENDER);
        entities.visibleWhen(() -> targets.get().contains(ENTITIES));
        show.optionVisibleWhen(option -> (option != SHOW_FILL && option != SHOW_OUTLINE) || mode.get() == MODE_3D || show.get().contains(SHOW_BOX));
        fillOpacity.visibleWhen(() -> shows(SHOW_FILL));
        type.visibleWhen(() -> mode.get() == MODE_2D);
        healthPosition.visibleWhen(() -> mode.get() == MODE_2D && shows(SHOW_HEALTH));
    }

    @Override
    protected void onEnable() {
        listen(WorldExtractEvent.class, this::onWorldExtract);
        listen(ScreenExtractEvent.class, this::onScreenExtract);
    }

    private void onWorldExtract(WorldExtractEvent event) {
        if (mode.get() != MODE_3D) return;
        BoxStyle style = boxStyle();
        for (Target target : targets(event)) {
            event.shapes().box(target.bounds(), style.scaled(perspective(target.distanceSq())));
        }
    }

    private void onScreenExtract(ScreenExtractEvent event) {
        boolean flat = mode.get() == MODE_2D;
        boolean boxes = flat && shows(SHOW_BOX);
        HealthBar.Side side = healthSide();
        if (!boxes && side == null) return;
        RectStyle style = rectStyle();
        float clearance = boxes ? style.widestStroke() * 0.5f * event.w2s().pixel() : 0f;
        for (Target target : targets(event)) {
            AABB bounds = flat ? fitted(target.entity(), target.bounds()) : target.bounds();
            ScreenRect rect = Projections.bounds(event.w2s(), bounds);
            if (rect == null) continue;
            if (boxes) event.shapes().rect(rect, style);
            if (side != null) healthBar(event.shapes(), target.entity(), rect, side, clearance, event.w2s().pixel());
        }
    }

    private boolean shows(int element) {
        return show.has(element);
    }

    private HealthBar.Side healthSide() {
        if (!shows(SHOW_HEALTH)) return null;
        return mode.get() == MODE_2D ? HEALTH_SIDES[healthPosition.get()] : HealthBar.Side.LEFT;
    }

    private static void healthBar(OverlayCollector shapes, Entity entity, ScreenRect rect, HealthBar.Side side, float clearance, float pixel) {
        if (!(entity instanceof LivingEntity living)) return;
        float fraction = living.getMaxHealth() <= 0f ? 0f : living.getHealth() / living.getMaxHealth();
        HealthBar.collect(shapes, rect, side, fraction, clearance, pixel);
    }

    private List<Target> targets(LevelFrameEvent event) {
        Vec3 origin = event.camera().position();
        double range = (double) distance.get() * distance.get();
        List<Target> found = new ArrayList<>();
        for (Entity entity : event.level().entitiesForRendering()) {
            if (!targeted(entity, event.camera())) continue;
            double distanceSq = entity.distanceToSqr(origin);
            if (distanceSq <= range) found.add(new Target(entity, event.interpolatedBounds(entity), distanceSq));
        }
        return found;
    }

    private static AABB fitted(Entity entity, AABB bounds) {
        return entity instanceof Player ? fitPlayer(bounds) : bounds;
    }

    private static AABB fitPlayer(AABB bounds) {
        return new AABB(bounds.minX - PLAYER_SIDE_PADDING, bounds.minY, bounds.minZ - PLAYER_SIDE_PADDING,
                bounds.maxX + PLAYER_SIDE_PADDING, bounds.maxY + PLAYER_TOP_PADDING, bounds.maxZ + PLAYER_SIDE_PADDING);
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

    private BoxStyle boxStyle() {
        int rgb = color.get();
        BoxStyle style = BoxStyle.stroke(0xFF000000 | rgb, LINE_WIDTH);
        if (shows(SHOW_FILL)) style = style.withFill(fillColor(rgb));
        if (shows(SHOW_OUTLINE)) style = style.withOutline(OUTLINE_COLOR, OUTLINE_WIDTH);
        return style;
    }

    private RectStyle rectStyle() {
        int rgb = color.get();
        RectStyle style = RectStyle.stroke(0xFF000000 | rgb, SCREEN_LINE_WIDTH);
        if (shows(SHOW_FILL)) style = style.withFill(fillColor(rgb));
        if (shows(SHOW_OUTLINE)) style = style.withOutline(OUTLINE_COLOR, SCREEN_OUTLINE_WIDTH);
        if (type.get() == TYPE_CORNERED) style = style.withEdges(EdgeStyle.CORNERED);
        return style;
    }

    private int fillColor(int rgb) {
        int alpha = Math.round(255f * fillOpacity.get() / 100f);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static String typeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
