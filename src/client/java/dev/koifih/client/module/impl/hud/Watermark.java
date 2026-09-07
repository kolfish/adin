package dev.koifih.client.module.impl.hud;

import dev.koifih.Adin;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.UiScale;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.gui.Rects;
import dev.koifih.client.render.gui.Text;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.setting.TextColorSettings;
import dev.koifih.client.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class Watermark extends Module {
    private static final String NAME = "adin";
    private static final String SUFFIX = ".lol";
    private static final Identifier STAR = Adin.id("textures/hud/logo_star.png");
    private static final Identifier BAND = Adin.id("textures/hud/logo_band.png");
    private static final int TEXTURE_SIZE = 64;
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
        super("watermark", Category.HUD);
    }

    @Override
    protected void onEnable() {
        listen(HudRenderEvent.class, this::onHudRender);
    }

    private void onHudRender(HudRenderEvent event) {
        Theme.update();
        GuiGraphicsExtractor graphics = event.graphics();
        float scale = UiScale.current().factor() * size.get() / 100f;
        int margin = Math.round(MARGIN * scale);
        int box = Math.max(1, Math.round(HEIGHT * scale));
        int radius = Math.round(RADIUS * scale);
        int inset = Math.round(LOGO_INSET * scale);
        float padding = PADDING * scale;
        float textSize = TEXT_SIZE * scale;
        int background = Colors.withAlpha(Theme.MAIN, ALPHA);
        Text.ColorAt color = colors.colorAt(scale);
        Rects.draw(graphics, margin, margin, box, box, radius, background);
        int logo = box - 2 * inset;
        int logoX = margin + inset;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BAND, logoX, logoX, 0f, 0f, logo, logo, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, 0xFFFFFFFF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, STAR, logoX, logoX, 0f, 0f, logo, logo, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color.at(logoX + logo * 0.5f));
        Text.Ink ink = Text.ink(NAME + SUFFIX, textSize);
        int textBox = margin + box + Math.round(GAP * scale);
        Rects.draw(graphics, textBox, margin, Math.round(ink.width() + 2f * padding), box, radius, background);
        float textX = textBox + padding - ink.left();
        float centerY = margin + box * 0.5f;
        Text.drawCentered(graphics, NAME, textX, centerY, textSize, color);
        Text.drawCentered(graphics, SUFFIX, textX + Text.width(NAME, textSize), centerY, textSize, Theme.MUTED);
    }
}
