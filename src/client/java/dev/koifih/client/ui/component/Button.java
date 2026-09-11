package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class Button extends Control {
    private enum Style { FILLED, TILE, ICON }

    private final Style style;
    private final int icon;

    public Button(int x, int y, int width, int height, float scale, Component label, Runnable action) {
        this(x, y, width, height, scale, label, Style.FILLED, -1, action);
    }

    private Button(int x, int y, int width, int height, float scale, Component label, Style style, int icon, Runnable action) {
        super(x, y, width, height, scale, label, action);
        this.style = style;
        this.icon = icon;
    }

    public static Button tile(int x, int y, int width, int height, float scale, Component label, int icon, Runnable action) {
        return new Button(x, y, width, height, scale, label, Style.TILE, icon, action);
    }

    public static Button icon(int x, int y, int size, float scale, int icon, Component label, Runnable action) {
        return new Button(x, y, size, size, scale, label, Style.ICON, icon, action);
    }

    public static int preferredWidth(String label, float scale) {
        return (int) Math.ceil(Text.width(label, 7 * scale) + 24 * scale);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        switch (style) {
            case ICON -> {
                float iconSize = getWidth() * 0.7f;
                Draw.icon(graphics, icon, getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2, iconSize, Theme.DIM);
            }
            case TILE -> {
                rect(graphics, getX(), getY(), getWidth(), getHeight(), px(5), Theme.ROW);
                float iconSize = px(10);
                Draw.icon(graphics, icon, getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2, iconSize, Theme.DIM);
            }
            case FILLED -> {
                rect(graphics, getX(), getY(), getWidth(), getHeight(), px(5), Theme.ACCENT);
                float size = px(7);
                String label = fit(getMessage().getString(), getWidth() - px(12), size);
                text(graphics, label, getX() + (getWidth() - Text.width(label, size)) / 2, centerY(), size, Theme.ON_ACCENT);
            }
        }
    }
}
