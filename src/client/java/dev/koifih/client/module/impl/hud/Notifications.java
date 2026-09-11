package dev.koifih.client.module.impl.hud;

import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.event.events.ModuleToggleEvent;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.ui.Transition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

public final class Notifications extends Module {
    private static final int CHECK_ICON = 0xe5ca;
    private static final int CROSS_ICON = 0xe5cd;
    private static final long SHOW_NANOS = 2_500_000_000L;
    private static final int REVEAL_MILLIS = 200;
    private static final float HEIGHT = 20f;
    private static final float PADDING = 8f;
    private static final float GAP = 6f;
    private static final float MARGIN = 10f;
    private static final float BADGE_SIZE = 14f;
    private static final float ICON_SIZE = 9f;
    private static final float TEXT_SIZE = 8f;
    private static final float DIVIDER_WIDTH = 1f;
    private static final float DIVIDER_HEIGHT = 10f;
    private static final float RADIUS = 4f;

    private record Toast(String text, boolean enabled, long shownAt, Transition reveal) {}

    private final List<Toast> toasts = new ArrayList<>();

    public Notifications() {
        super("notifications");
    }

    @Override
    protected void onEnable() {
        listen(ModuleToggleEvent.class, event -> push(event.module().name(), event.enabled()));
        listen(HudRenderEvent.class, this::onHudRender);
    }

    @Override
    protected void onDisable() {
        toasts.clear();
    }

    private void push(String text, boolean enabled) {
        Transition reveal = new Transition(0f, REVEAL_MILLIS);
        reveal.set(1f);
        toasts.add(new Toast(text, enabled, System.nanoTime(), reveal));
    }

    private void onHudRender(HudRenderEvent event) {
        long now = System.nanoTime();
        toasts.removeIf(toast -> toast.reveal().target() == 0f && toast.reveal().value() <= 0f);
        if (toasts.isEmpty()) return;
        Theme.update();
        float scale = UiScale.current().factor();
        var window = Minecraft.getInstance().getWindow();
        float right = window.getGuiScaledWidth() - MARGIN * scale;
        float bottom = window.getGuiScaledHeight() - MARGIN * scale;
        float stride = (HEIGHT + GAP) * scale;
        float offset = 0f;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            Toast toast = toasts.get(i);
            if (now - toast.shownAt() >= SHOW_NANOS) toast.reveal().set(0f);
            float shown = toast.reveal().value();
            if (shown <= 0f) continue;
            draw(event.graphics(), toast, right, bottom - HEIGHT * scale - offset * stride, scale, shown);
            offset += shown;
        }
    }

    private void draw(GuiGraphicsExtractor graphics, Toast toast, float right, float y, float scale, float shown) {
        float height = HEIGHT * scale;
        float padding = PADDING * scale;
        int badge = Math.max(1, Math.round(BADGE_SIZE * scale));
        float icon = ICON_SIZE * scale;
        float divider = DIVIDER_WIDTH * scale;
        float textSize = TEXT_SIZE * scale;
        float width = padding + badge + padding + divider + padding + Text.width(toast.text(), textSize) + padding;
        float x = right - width + (1f - shown) * (width + MARGIN * scale);
        float centerY = y + height * 0.5f;
        float badgeX = x + padding;
        float dividerX = badgeX + badge + padding;
        int dividerHeight = Math.max(1, Math.round(DIVIDER_HEIGHT * scale));
        Opacity.with(shown, () -> {
            Draw.bordered(graphics, x, y, width, height, RADIUS * scale, Theme.POPUP, Theme.POPUP_BORDER);
            Draw.rect(graphics, badgeX, centerY - badge * 0.5f, badge, badge, badge / 2, Theme.CONTROL);
            Draw.icon(graphics, toast.enabled() ? CHECK_ICON : CROSS_ICON, badgeX + (badge - icon) * 0.5f, centerY - icon * 0.5f, icon,
                    toast.enabled() ? Theme.ACCENT : Theme.MUTED);
            Draw.rect(graphics, dividerX, centerY - dividerHeight * 0.5f, divider, dividerHeight, 0, Theme.UNCHECKED);
            Text.drawCentered(graphics, toast.text(), dividerX + divider + padding, centerY, textSize, Theme.TEXT);
        });
    }
}
