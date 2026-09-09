package dev.koifih.client.module.impl.render.esp;

import dev.koifih.client.render.Rect;
import dev.koifih.client.render.entity.EntityFill;
import dev.koifih.client.render.screen.Projection;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class Shader {
    private static final String[] MODES = {"Solid", "Gradient", "Shader"};
    private static final int GRADIENT = 1;
    private static final int EFFECT = 2;
    private static final String[] EFFECTS = {"Galaxy", "Metallic", "Aurora", "Ocean", "Lava", "Sky"};
    private static final int VISIBLE_COLOR = 0xB8DDB0;
    private static final int VISIBLE_COLOR_END = 0xB7CCE8;
    private static final int INVISIBLE_COLOR = 0xE5B6C4;
    private static final int INVISIBLE_COLOR_END = 0xD2BCE7;

    private final EnumSetting mode;
    private final EnumSetting effect;
    private final BoolSetting invisible;
    private final ColorSetting visibleColor;
    private final ColorSetting invisibleColor;
    private final ColorSetting handColor;

    Shader(Esp esp) {
        mode = esp.setting(new EnumSetting("shaderMode", 0, MODES));
        effect = esp.setting(new EnumSetting("shader", 0, EFFECTS));
        invisible = esp.setting(new BoolSetting("invisible", true));
        visibleColor = esp.setting(new ColorSetting("visibleColor", VISIBLE_COLOR, VISIBLE_COLOR_END));
        invisibleColor = esp.setting(new ColorSetting("invisibleColor", INVISIBLE_COLOR, INVISIBLE_COLOR_END));
        handColor = esp.setting(new ColorSetting("handColor", VISIBLE_COLOR, VISIBLE_COLOR_END));
        mode.visibleWhen(esp::shaded);
        effect.visibleWhen(() -> esp.shaded() && effect());
        invisible.visibleWhen(esp::shaded);
        visibleColor.visibleWhen(esp::shaded);
        invisibleColor.visibleWhen(() -> esp.shaded() && invisible.get());
        handColor.visibleWhen(() -> esp.shaded() && esp.handTargeted());
        visibleColor.gradientWhen(() -> esp.shaded() && gradient());
        invisibleColor.gradientWhen(() -> esp.shaded() && gradient());
        handColor.gradientWhen(() -> esp.shaded() && gradient());
    }

    private boolean gradient() {
        return mode.get() == GRADIENT;
    }

    private boolean effect() {
        return mode.get() == EFFECT;
    }

    private int effectIndex() {
        return effect() ? effect.get() + 1 : 0;
    }

    EntityFill entityFill(Vec3 position, Camera camera, EntityRenderState state) {
        int visible = Colors.opaque(visibleColor.get());
        int occluded = invisible.get() ? Colors.opaque(invisibleColor.get()) : 0;
        if (effect()) return EntityFill.effect(visible, occluded, effectIndex(), position.subtract(camera.position()), 1f);
        if (!gradient()) return EntityFill.solid(visible, occluded);
        Projection projection = Projection.capture();
        double half = state.boundingBoxWidth / 2.0;
        Rect rect = projection.bounds(new AABB(position.x - half, position.y, position.z - half,
                position.x + half, position.y + state.boundingBoxHeight, position.z + half));
        int minY = rect == null ? 0 : projection.framebufferY(rect.maxY());
        int maxY = rect == null ? 0 : projection.framebufferY(rect.minY());
        return EntityFill.gradient(visible, visibleColor.secondary(), occluded, invisibleColor.secondary(), minY, maxY);
    }

    EntityFill handFill() {
        int color = Colors.opaque(handColor.get());
        if (effect()) return EntityFill.effect(color, 0, effectIndex(), Vec3.ZERO, 1f);
        if (!gradient()) return EntityFill.solid(color, 0);
        return EntityFill.gradient(color, handColor.secondary(), 0, 0, 0, Minecraft.getInstance().getWindow().getHeight());
    }

    PreviewSetting.Shade shade(Setting<?> highlighted) {
        if (highlighted != visibleColor && highlighted != invisibleColor && highlighted != handColor) return null;
        ColorSetting color = (ColorSetting) highlighted;
        return new PreviewSetting.Shade(color.get(), color.secondary(), gradient(), effectIndex());
    }
}
