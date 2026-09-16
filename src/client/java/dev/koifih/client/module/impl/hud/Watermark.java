package dev.koifih.client.module.impl.hud;

import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.module.HudModule;
import dev.koifih.client.render.Draw;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.PositionSetting.Anchor;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.setting.TextColorSettings;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.UiScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

public final class Watermark extends HudModule {
    private static final float MARGIN = 4f;
    private static final float HEIGHT = 17f;
    private static final float MARK = 11f;
    private static final float PADDING = 7f;
    private static final float END = 7f;
    private static final float RADIUS = 4f;
    private static final float LEAN = (float) Math.tan(Math.toRadians(22));
    private static final int BACK = 0xFFA3A3A3;
    private static final int FRONT = 0xFFEAEAEA;

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
        int height = Math.max(1, Math.round(HEIGHT * scale));
        float mark = MARK * scale;
        float markWidth = Draw.wordmarkWidth(mark);
        int body = Math.round((PADDING + END) * scale + markWidth);
        float lean = height * LEAN;
        int width = Math.round(body + lean);
        var window = Minecraft.getInstance().getWindow();
        int x = Math.round(left(width, window.getGuiScaledWidth()));
        int y = Math.round(top(height, window.getGuiScaledHeight()));
        placed(x, y, width, height);
        drawTile(graphics, x, y, body, height, Math.round(RADIUS * scale), lean);
        float markX = x + PADDING * scale;
        int star = colors.colorAt(scale).at(markX + markWidth * 0.5f);
        Draw.wordmark(graphics, markX, y + (height - mark) * 0.5f, mark, BACK, star, FRONT, Theme.SIDEBAR);
    }

    private static void drawTile(GuiGraphicsExtractor graphics, int x, int y, int body, int height, int radius, float lean) {
        int cap = (int) Math.ceil(lean) + 2 * radius + 2;
        int capLeft = x + body - cap;
        int overlap = capLeft + (int) Math.ceil(lean) + 1;
        Draw.rect(graphics, x, y, overlap - x, height, radius, Draw.TOP_LEFT | Draw.BOTTOM_LEFT, Theme.SIDEBAR);
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(capLeft + lean, y).mul(new Matrix3x2f(1f, 0f, -lean / height, 1f, 0f, 0f));
            Draw.rect(graphics, 0, 0, cap, height, radius, Draw.TOP_RIGHT | Draw.BOTTOM_RIGHT, Theme.SIDEBAR);
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
