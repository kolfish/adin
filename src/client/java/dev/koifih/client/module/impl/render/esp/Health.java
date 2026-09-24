package dev.koifih.client.module.impl.render.esp;

import dev.koifih.client.render.Rect;
import dev.koifih.client.render.screen.HealthBar;
import dev.koifih.client.render.screen.ScreenBuffer;
import dev.koifih.client.setting.EnumSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

final class Health {
    private static final String[] POSITIONS = {"Left", "Right", "Top", "Bottom"};
    private static final HealthBar.Side[] SIDES = {HealthBar.Side.LEFT, HealthBar.Side.RIGHT, HealthBar.Side.TOP, HealthBar.Side.BOTTOM};

    private final Esp esp;
    private final Box box;
    private final EnumSetting position;

    Health(Esp esp, Box box) {
        this.esp = esp;
        this.box = box;
        position = esp.setting(new EnumSetting("healthPosition", 0, POSITIONS));
        position.visibleWhen(() -> esp.flat() && box.shows(Box.SHOW_HEALTH));
    }

    HealthBar.Side side() {
        if (esp.shaded() || esp.outlined() || !box.shows(Box.SHOW_HEALTH)) return null;
        return esp.flat() ? SIDES[position.get()] : HealthBar.Side.LEFT;
    }

    static void collect(ScreenBuffer buffer, Entity entity, Rect rect, HealthBar.Side side, float clearance, float pixel) {
        if (!(entity instanceof LivingEntity living)) return;
        float fraction = living.getMaxHealth() <= 0f ? 0f : living.getHealth() / living.getMaxHealth();
        HealthBar.collect(buffer, rect, side, fraction, clearance, pixel);
    }
}
