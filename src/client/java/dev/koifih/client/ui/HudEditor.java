package dev.koifih.client.ui.hud;

import com.mojang.blaze3d.platform.Window;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.module.HudModule;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public final class HudEditor {
    private static final float SNAP = 4f;
    private static final float GUIDE_ALPHA = 0.6f;
    private static HudModule dragging;
    private static float grabX;
    private static float grabY;

    private HudEditor() {}

    public static void init() {
        AdinClient.EVENTS.subscribe(HudRenderEvent.class, Priority.LOWEST, HudEditor::onHudRender);
    }

    public static boolean press(double mouseX, double mouseY) {
        List<Module> modules = AdinClient.MODULES.all();
        for (int i = modules.size() - 1; i >= 0; i--) {
            if (!(modules.get(i) instanceof HudModule hud) || !hud.isEnabled() || !hud.contains(mouseX, mouseY)) continue;
            dragging = hud;
            grabX = (float) mouseX - hud.x();
            grabY = (float) mouseY - hud.y();
            return true;
        }
        return false;
    }

    private static void onHudRender(HudRenderEvent event) {
        if (dragging == null) return;
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        boolean held = client.gui.screen() instanceof ChatScreen
                && GLFW.glfwGetMouseButton(window.handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (!held) {
            dragging = null;
            return;
        }
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        float x = (float) client.mouseHandler.getScaledXPos(window) - grabX;
        float y = (float) client.mouseHandler.getScaledYPos(window) - grabY;
        float width = dragging.width();
        float height = dragging.height();
        x = Mth.clamp(snap(x, width, screenWidth), 0f, screenWidth - width);
        y = Mth.clamp(snap(y, height, screenHeight), 0f, screenHeight - height);
        dragging.moveTo(x, y, screenWidth, screenHeight);
        int guide = Colors.withAlpha(Theme.ACCENT, GUIDE_ALPHA);
        Draw.rect(event.graphics(), screenWidth / 2, 0, 1, screenHeight, 0, guide);
        Draw.rect(event.graphics(), 0, screenHeight / 2, screenWidth, 1, 0, guide);
    }

    private static float snap(float start, float size, float screen) {
        if (Math.abs(start + size * 0.5f - screen * 0.5f) < SNAP) return (screen - size) * 0.5f;
        if (Math.abs(start) < SNAP) return 0f;
        if (Math.abs(start + size - screen) < SNAP) return screen - size;
        return start;
    }
}
