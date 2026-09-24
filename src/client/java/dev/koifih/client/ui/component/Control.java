package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public abstract class Control extends AbstractWidget {
    protected final float scale;
    private final Runnable action;

    protected Control(int x, int y, int width, int height, float scale, Component label) {
        this(x, y, width, height, scale, label, null);
    }

    protected Control(int x, int y, int width, int height, float scale, Component label, Runnable action) {
        super(x, y, width, height, label);
        this.scale = scale;
        this.action = action;
    }

    public boolean capturesInput() {
        return false;
    }

    public void dismiss() {
    }

    protected void activate() {
        if (action != null) action.run();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        activate();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (action != null && active && isFocused() && event.isSelection()) {
            activate();
            return true;
        }
        return super.keyPressed(event);
    }

    public float px(float units) {
        return units * scale;
    }

    protected float centerY() {
        return getY() + getHeight() * 0.5f;
    }

    public void rect(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float radius, int color) {
        Draw.rect(graphics, x, y, width, Math.max(1, Math.round(height)), Math.max(0, Math.round(radius)), color);
    }

    public void text(GuiGraphicsExtractor graphics, String text, float x, float centerY, float size, int color) {
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
