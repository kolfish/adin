package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.render.gui.Icons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class IconButton extends Clickable {
    private final int icon;

    public IconButton(int x, int y, int size, float scale, int icon, Component label, Runnable action) {
        super(x, y, size, size, scale, label, action);
        this.icon = icon;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float iconSize = getWidth() * 0.7f;
        Icons.draw(graphics, icon,
                getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2,
                iconSize, Theme.DIM);
    }
}
