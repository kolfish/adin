package dev.koifih.client.module.impl.hud;

import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.module.HudModule;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.PositionSetting.Anchor;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.setting.TextColorSettings;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Watermark extends HudModule {
    private static final String NAME = "adin";
    private static final String SUFFIX = ".lol";
    private static final float MARGIN = 4f;
    private static final float HEIGHT = 14f;
    private static final float GAP = 3f;
    private static final float PADDING = 4f;
    private static final float RADIUS = 4f;
    private static final float LOGO_INSET = 1f;
    private static final float TEXT_SIZE = 8f;
    private static final float ALPHA = 0.8f;

    private final SliderSetting size = add(new SliderSetting("scale", 100, 50, 200, Measure.PERCENT));
    private final TextColorSettings colors = add(new TextColorSettings());

    public Watermark() {
        super("watermark", Anchor.START, Anchor.START, MARGIN, MARGIN);
    }

    @Override
    protected void onEnable() {
        listen(HudRenderEvent.class, this::onHudRender);
    }

    private void onHudRender(HudRenderEvent event) {
        Theme.update();
        GuiGraphicsExtractor graphics = event.graphics();
        float scale = UiScale.current().factor() * size.get() / 100f;
        int box = Math.max(1, Math.round(HEIGHT * scale));
        int radius = Math.round(RADIUS * scale);
        int inset = Math.round(LOGO_INSET * scale);
        float padding = PADDING * scale;
        float textSize = TEXT_SIZE * scale;
        int background = Colors.withAlpha(Theme.MAIN, ALPHA);
        Text.ColorAt color = colors.colorAt(scale);
        Text.Ink ink = Text.ink(NAME + SUFFIX, textSize);
        int gap = Math.round(GAP * scale);
        int textWidth = Math.round(ink.width() + 2f * padding);
        var window = Minecraft.getInstance().getWindow();
        int x = Math.round(left(box + gap + textWidth, window.getGuiScaledWidth()));
        int y = Math.round(top(box, window.getGuiScaledHeight()));
        placed(x, y, box + gap + textWidth, box);
        Draw.rect(graphics, x, y, box, box, radius, background);
        int logo = box - 2 * inset;
        Draw.logo(graphics, x + inset, y + inset, logo, color.at(x + box * 0.5f));
        int textBox = x + box + gap;
        Draw.rect(graphics, textBox, y, textWidth, box, radius, background);
        float textX = textBox + padding - ink.left();
        float centerY = y + box * 0.5f;
        Text.drawCentered(graphics, NAME, textX, centerY, textSize, color);
        Text.drawCentered(graphics, SUFFIX, textX + Text.width(NAME, textSize), centerY, textSize, Theme.MUTED);
    }
}
