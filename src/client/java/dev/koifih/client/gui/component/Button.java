package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.rendering.IconRenderer;
import dev.koifih.client.rendering.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class Button extends Clickable {
    private final int icon;

    public Button(int x, int y, int width, int height, float scale, Component label, Runnable action) {
        this(x, y, width, height, scale, label, -1, action);
    }

    public Button(int x, int y, int width, int height, float scale, Component label, int icon, Runnable action) {
        super(x, y, width, height, scale, label, action);
        this.icon = icon;
    }

    public static int preferredWidth(String label, float scale) {
        return (int) Math.ceil(TextRenderer.width(label, 7 * scale) + 24 * scale);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (icon >= 0) {
            rect(graphics, getX(), getY(), getWidth(), getHeight(), px(5), Theme.ROW);
            float iconSize = px(10);
            IconRenderer.draw(graphics, icon, getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2,
                    iconSize, Theme.DIM);
            return;
        }
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(5), Theme.ACCENT);
        float size = px(7);
        String label = fit(getMessage().getString(), getWidth() - px(12), size);
        text(graphics, label, getX() + (getWidth() - TextRenderer.width(label, size)) / 2, centerY(), size, Theme.ON_ACCENT);
    }
}
