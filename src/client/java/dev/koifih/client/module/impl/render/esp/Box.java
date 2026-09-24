package dev.koifih.client.module.impl.render.esp;

import dev.koifih.client.render.Edges;
import dev.koifih.client.render.Style;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.util.Colors;
import net.minecraft.world.phys.AABB;

final class Box {
    static final int SHOW_BOX = 0;
    static final int SHOW_FILL = 1;
    static final int SHOW_OUTLINE = 2;
    static final int SHOW_HEALTH = 3;

    private static final String[] TYPES = {"Full", "Cornered"};
    private static final int TYPE_CORNERED = 1;
    private static final String[] SHOW = {"Box", "Fill", "Outline", "Health"};
    private static final float WORLD_LINE_WIDTH = 2f;
    private static final float WORLD_OUTLINE_WIDTH = WORLD_LINE_WIDTH + 2f;
    private static final float SCREEN_LINE_WIDTH = 1f;
    private static final float SCREEN_OUTLINE_WIDTH = SCREEN_LINE_WIDTH + 1.5f;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final double FULL_WIDTH_DISTANCE = 10.0;
    private static final double PLAYER_SIDE_PADDING = 0.12;
    private static final double PLAYER_TOP_PADDING = 0.06;

    private final Esp esp;
    private final EnumSetting type;
    private final ColorSetting color;
    private final MultiSetting show;
    private final SliderSetting fillOpacity;

    Box(Esp esp) {
        this.esp = esp;
        type = esp.setting(new EnumSetting("type", 0, TYPES));
        color = esp.setting(new ColorSetting("color", Theme.DEFAULT_ACCENT));
        show = esp.setting(new MultiSetting("show", SHOW, SHOW_BOX, SHOW_FILL, SHOW_OUTLINE));
        fillOpacity = esp.setting(new SliderSetting("fillOpacity", 25, 0, 100, Measure.PERCENT));
        type.visibleWhen(esp::flat);
        color.visibleWhen(() -> !esp.shaded());
        show.visibleWhen(() -> !esp.shaded() && !esp.outlined());
        show.optionVisibleWhen(option -> switch (option) {
            case SHOW_FILL -> esp.boxed() || show.get().contains(SHOW_BOX);
            case SHOW_OUTLINE -> show.get().contains(SHOW_BOX);
            default -> true;
        });
        fillOpacity.visibleWhen(() -> !esp.shaded() && (esp.outlined() ? esp.outlineFilled() : shows(SHOW_FILL)));
    }

    boolean shows(int element) {
        return show.has(element);
    }

    boolean shown() {
        return !esp.outlined() && shows(SHOW_BOX);
    }

    int rgb() {
        return color.get();
    }

    float fillOpacity() {
        return fillOpacity.get() / 100f;
    }

    Style worldStyle() {
        return styled(WORLD_LINE_WIDTH, WORLD_OUTLINE_WIDTH);
    }

    Style screenStyle() {
        Style style = styled(SCREEN_LINE_WIDTH, SCREEN_OUTLINE_WIDTH);
        return type.get() == TYPE_CORNERED ? style.withEdges(Edges.CORNERED) : style;
    }

    private Style styled(float lineWidth, float outlineWidth) {
        int rgb = Colors.opaque(color.get());
        if (esp.shaded() || esp.outlined()) return Style.EMPTY;
        Style style = shown() ? Style.stroke(rgb, lineWidth) : Style.EMPTY;
        if (shows(SHOW_FILL)) style = style.withFill(Colors.withAlpha(rgb, fillOpacity.get() / 100f));
        if (shows(SHOW_OUTLINE)) style = style.withOutline(OUTLINE_COLOR, outlineWidth);
        return style;
    }

    static float perspective(double distanceSq) {
        if (distanceSq <= FULL_WIDTH_DISTANCE * FULL_WIDTH_DISTANCE) return 1f;
        return (float) (FULL_WIDTH_DISTANCE / Math.sqrt(distanceSq));
    }

    static AABB fitPlayer(AABB bounds) {
        return new AABB(bounds.minX - PLAYER_SIDE_PADDING, bounds.minY, bounds.minZ - PLAYER_SIDE_PADDING,
                bounds.maxX + PLAYER_SIDE_PADDING, bounds.maxY + PLAYER_TOP_PADDING, bounds.maxZ + PLAYER_SIDE_PADDING);
    }
}
