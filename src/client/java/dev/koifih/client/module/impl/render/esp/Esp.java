package dev.koifih.client.module.impl.render;

import dev.koifih.client.event.events.FrameEvent;
import dev.koifih.client.event.events.ScreenRenderEvent;
import dev.koifih.client.event.events.WorldRenderEvent;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Edges;
import dev.koifih.client.render.Style;
import dev.koifih.client.render.screen.HealthBar;
import dev.koifih.client.render.screen.ScreenBuffer;
import dev.koifih.client.render.screen.ScreenRect;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EntitySetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Colors;
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

public final class Esp extends Module {
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
    private static final float WORLD_LINE_WIDTH = 2f;
    private static final float WORLD_OUTLINE_WIDTH = WORLD_LINE_WIDTH + 2f;
    private static final float SCREEN_LINE_WIDTH = 1f;
    private static final float SCREEN_OUTLINE_WIDTH = SCREEN_LINE_WIDTH + 1.5f;
    private static final int OUTLINE_COLOR = 0xFF000000;
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
            this::screenStyle, this::worldStyle, Esp::fitPlayer, this::healthSide));

    private record Target(Entity entity, AABB bounds, double distanceSq) {}

    public Esp() {
        super("esp", Category.RENDER);
        entities.visibleWhen(() -> targets.get().contains(ENTITIES));
        show.optionVisibleWhen(option -> switch (option) {
            case SHOW_FILL -> mode.get() == MODE_3D || show.get().contains(SHOW_BOX);
            case SHOW_OUTLINE -> show.get().contains(SHOW_BOX);
            default -> true;
        });
        fillOpacity.visibleWhen(() -> shows(SHOW_FILL));
        type.visibleWhen(() -> mode.get() == MODE_2D);
        healthPosition.visibleWhen(() -> mode.get() == MODE_2D && shows(SHOW_HEALTH));
    }

    @Override
    protected void onEnable() {
        listen(WorldRenderEvent.class, this::onWorldRender);
        listen(ScreenRenderEvent.class, this::onScreenRender);
    }

    private void onWorldRender(WorldRenderEvent event) {
        if (mode.get() != MODE_3D) return;
        Style style = worldStyle();
        if (!style.hasFill() && !style.hasStroke()) return;
        for (Target target : targets(event)) {
            event.buffer().box(target.bounds(), style.scaled(perspective(target.distanceSq())));
        }
    }

    private void onScreenRender(ScreenRenderEvent event) {
        boolean flat = mode.get() == MODE_2D;
        boolean boxes = flat && shows(SHOW_BOX);
        HealthBar.Side side = healthSide();
        if (!boxes && side == null) return;
        float pixel = event.projection().pixel();
        Style style = screenStyle().scaled(pixel);
        float clearance = boxes ? style.widestStroke() * 0.5f : 0f;
        for (Target target : targets(event)) {
            AABB bounds = flat ? fitted(target.entity(), target.bounds()) : target.bounds();
            ScreenRect rect = event.projection().bounds(bounds);
            if (rect == null) continue;
            if (boxes) event.buffer().rect(rect, style);
            if (side != null) healthBar(event.buffer(), target.entity(), rect, side, clearance, pixel);
        }
    }

    private boolean shows(int element) {
        return show.has(element);
    }

    private HealthBar.Side healthSide() {
        if (!shows(SHOW_HEALTH)) return null;
        return mode.get() == MODE_2D ? HEALTH_SIDES[healthPosition.get()] : HealthBar.Side.LEFT;
    }

    private static void healthBar(ScreenBuffer buffer, Entity entity, ScreenRect rect, HealthBar.Side side, float clearance, float pixel) {
        if (!(entity instanceof LivingEntity living)) return;
        float fraction = living.getMaxHealth() <= 0f ? 0f : living.getHealth() / living.getMaxHealth();
        HealthBar.collect(buffer, rect, side, fraction, clearance, pixel);
    }

    private List<Target> targets(FrameEvent event) {
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

    private Style worldStyle() {
        return styled(WORLD_LINE_WIDTH, WORLD_OUTLINE_WIDTH);
    }

    private Style screenStyle() {
        Style style = styled(SCREEN_LINE_WIDTH, SCREEN_OUTLINE_WIDTH);
        return type.get() == TYPE_CORNERED ? style.withEdges(Edges.CORNERED) : style;
    }

    private Style styled(float lineWidth, float outlineWidth) {
        int rgb = Colors.opaque(color.get());
        Style style = shows(SHOW_BOX) ? Style.stroke(rgb, lineWidth) : Style.EMPTY;
        if (shows(SHOW_FILL)) style = style.withFill(Colors.withAlpha(rgb, fillOpacity.get() / 100f));
        if (shows(SHOW_OUTLINE)) style = style.withOutline(OUTLINE_COLOR, outlineWidth);
        return style;
    }

    private static String typeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
