package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public abstract class Control extends AbstractWidget {
    protected final float scale;

    protected Control(int x, int y, int width, int height, float scale, Component label) {
        super(x, y, width, height, label);
        this.scale = scale;
    }

    public boolean capturesInput() {
        return false;
    }

    public void dismiss() {
    }

    float px(float units) {
        return units * scale;
    }

    protected float centerY() {
        return getY() + getHeight() * 0.5f;
    }

    void rect(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float radius, int color) {
        Draw.rect(graphics, x, y, width, Math.max(1, Math.round(height)), Math.max(0, Math.round(radius)), color);
    }

    protected void text(GuiGraphicsExtractor graphics, String text, float x, float centerY, float size, int color) {
        Text.drawCentered(graphics, text, x, centerY, size, color);
    }

    protected void field(GuiGraphicsExtractor graphics) {
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(4), Theme.FIELD);
    }

    protected static String fit(String text, float width, float size) {
        return Text.fit(text, width, size);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
