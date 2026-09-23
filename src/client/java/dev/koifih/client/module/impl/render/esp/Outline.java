package dev.koifih.client.module.impl.render.esp;

import dev.koifih.client.event.events.EntityRenderStateEvent;
import dev.koifih.client.render.Rect;
import dev.koifih.client.render.entity.EntityOutline;
import dev.koifih.client.render.entity.EntityOutlines;
import dev.koifih.client.render.screen.Projection;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

final class Outline {
    private static final int DEFAULT_WIDTH = 2;
    private static final int MIN_WIDTH = 1;
    private static final int MAX_WIDTH = 8;
    private static final String[] SHOW = {"Fill", "Glow"};
    private static final int SHOW_FILL = 0;
    private static final int SHOW_GLOW = 1;
    private static final double RENDER_REACH = 1.0;

    private final Esp esp;
    private final Box box;
    private final SliderSetting width;
    private final MultiSetting show;
    private final SliderSetting intensity;
    private final SliderSetting radius;
    private Projection projection;

    Outline(Esp esp, Box box) {
        this.esp = esp;
        this.box = box;
        width = esp.setting(new SliderSetting("outlineWidth", DEFAULT_WIDTH, MIN_WIDTH, MAX_WIDTH, Measure.NONE));
        show = esp.setting(new MultiSetting("outlineShow", SHOW, SHOW_FILL));
        intensity = esp.setting(new SliderSetting("glowIntensity", 200, 10, 300, Measure.PERCENT));
        radius = esp.setting(new SliderSetting("glowRadius", 6, 1, 16, Measure.NONE));
        width.visibleWhen(esp::outlined);
        show.visibleWhen(esp::outlined);
        intensity.visibleWhen(() -> esp.outlined() && show.has(SHOW_GLOW));
        radius.visibleWhen(() -> esp.outlined() && show.has(SHOW_GLOW));
    }

    boolean filled() {
        return show.has(SHOW_FILL);
    }

    int color() {
        return EntityOutlines.scaled(Colors.opaque(box.rgb()), 1f);
    }

    void include(EntityRenderStateEvent event, double distanceSq) {
        if (projection == null) projection = Projection.capture();
        event.state().outlineColor = EntityOutlines.scaled(event.state().outlineColor, Box.perspective(distanceSq));
        Entity entity = event.entity();
        Rect rect = projection.bounds(entity.getBoundingBox().inflate(RENDER_REACH).move(event.renderPosition().subtract(entity.position())));
        if (rect == null) return;
        EntityOutlines.include(projection.framebufferX(rect.minX()), projection.framebufferY(rect.maxY()),
                projection.framebufferX(rect.maxX()), projection.framebufferY(rect.minY()));
    }

    void endFrame() {
        projection = null;
    }

    EntityOutline spec() {
        return esp.outlined() ? new EntityOutline(color(), texels(), fill(), glow(), glowRadius()) : null;
    }

    float fill() {
        return filled() ? box.fillOpacity() : 0f;
    }

    float glow() {
        return show.has(SHOW_GLOW) ? intensity.get() / 100f : 0f;
    }

    float glowRadius() {
        return (float) radius.get() * Minecraft.getInstance().getWindow().getGuiScale();
    }

    float texels() {
        return (float) width.get() * Minecraft.getInstance().getWindow().getGuiScale();
    }
}
