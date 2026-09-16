package dev.koifih.client.module.impl.hud;

import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.module.HudModule;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.AdinIcon;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.PositionSetting.Anchor;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KeybindList extends HudModule {
    private static final float MARGIN = 4f;
    private static final float DEFAULT_TOP = 22f;
    private static final float HEADER = 15f;
    private static final float ROW = 12f;
    private static final float PADDING = 5f;
    private static final float GAP = 5f;
    private static final float BOTTOM = 3f;
    private static final float RADIUS = 4f;
    private static final float TEXT_SIZE = 8f;
    private static final float ICON_SIZE = 9f;
    private static final float HEADER_ICON_WIDTH = 12f;
    private static final float DIVIDER = 1f;
    private static final float ALPHA = 0.8f;

    private final SliderSetting size = add(new SliderSetting("scale", 100, 50, 200, Measure.PERCENT));

    public KeybindList() {
        super("keybinds", Anchor.START, Anchor.START, MARGIN, DEFAULT_TOP);
    }

    @Override
    protected void onEnable() {
        listen(HudRenderEvent.class, this::onHudRender);
    }

    private void onHudRender(HudRenderEvent event) {
        List<Module> bound = new ArrayList<>();
        for (Module module : AdinClient.MODULES.all()) {
            if (module.key() != InputConstants.UNKNOWN) bound.add(module);
        }
        if (bound.isEmpty()) {
            placed(0f, 0f, 0f, 0f);
            return;
        }
        bound.sort(Comparator.comparing(Module::name));
        Theme.update();
        GuiGraphicsExtractor graphics = event.graphics();
        float scale = UiScale.current().factor() * size.get() / 100f;
        float textSize = TEXT_SIZE * scale;
        float padding = PADDING * scale;
        float gap = GAP * scale;
        float icon = ICON_SIZE * scale;
        String title = Lang.get("keybinds");
        float width = Text.width(title, textSize) + gap + HEADER_ICON_WIDTH * scale;
        for (Module module : bound) {
            width = Math.max(width, Text.width(module.name(), textSize) + gap + icon + gap + Text.width(keyName(module), textSize));
        }
        int boxWidth = Math.round(width + 2f * padding);
        int header = Math.round(HEADER * scale);
        int row = Math.round(ROW * scale);
        int height = header + bound.size() * row + Math.round(BOTTOM * scale);
        var window = Minecraft.getInstance().getWindow();
        int x = Math.round(left(boxWidth, window.getGuiScaledWidth()));
        int y = Math.round(top(height, window.getGuiScaledHeight()));
        placed(x, y, boxWidth, height);
        int right = x + boxWidth;
        Draw.rect(graphics, x, y, boxWidth, height, Math.round(RADIUS * scale), Colors.withAlpha(Theme.MAIN, ALPHA));
        Text.drawCentered(graphics, title, x + padding, y + header * 0.5f, textSize, Theme.TEXT);
        float headerIcon = HEADER_ICON_WIDTH * scale;
        Draw.icon(graphics, AdinIcon.KEYBINDS, right - padding - headerIcon, y + (header - headerIcon) * 0.5f, headerIcon,
                Theme.DIM, Theme.ACCENT, Theme.TEXT, Theme.MAIN);
        Draw.rect(graphics, x, y + header, boxWidth, Math.max(1, Math.round(DIVIDER * scale)), 0, Theme.UNCHECKED);
        float rowY = y + header + Math.round(DIVIDER * scale);
        for (Module module : bound) {
            float centerY = rowY + row * 0.5f;
            String key = keyName(module);
            float keyX = right - padding - Text.width(key, textSize);
            Text.drawCentered(graphics, module.name(), x + padding, centerY, textSize, module.isEnabled() ? Theme.TEXT : Theme.DIM);
            if (!module.activatable()) {
                Draw.icon(graphics, module.hold() ? AdinIcon.HOLD : AdinIcon.TOGGLE, keyX - gap - icon, centerY - icon * 0.5f, icon,
                        Theme.DIM, Theme.ACCENT, Theme.TEXT, Theme.MAIN);
            }
            Text.drawCentered(graphics, key, keyX, centerY, textSize, Theme.MUTED);
            rowY += row;
        }
    }

    private static String keyName(Module module) {
        return module.key().getDisplayName().getString();
    }
}
