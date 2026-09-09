package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Scissor;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

abstract class RowPopup extends Popup {
    private static final int CHEVRON_ICON = 0xe5cf;

    private int icon = -1;

    protected RowPopup(int x, int y, int width, int height, float scale, Component label) {
        super(x, y, width, height, scale, label);
    }

    public void setIcon(int codepoint) {
        icon = codepoint;
    }

    protected abstract void drawRowValue(GuiGraphicsExtractor graphics, float rightLimit, float available);

    protected abstract void drawContents(GuiGraphicsExtractor graphics, float x, float top, float width, float height);

    protected boolean opensBelow() {
        return getBottom() + popupHeight() <= bottomLimit();
    }

    @Override
    protected float popupY() {
        return opensBelow() ? getBottom() : getY() - popupHeight();
    }

    private void drawRow(GuiGraphicsExtractor graphics, float shown) {
        float labelX = getX() + px(10);
        if (icon >= 0) {
            float size = px(9);
            Draw.icon(graphics, icon, getX() + px(8), centerY() - size / 2, size, Theme.DIM);
            labelX += px(13);
        }
        text(graphics, getMessage().getString(), labelX, centerY(), px(7), Theme.TEXT);
        float iconSize = px(9);
        float iconCenterX = getRight() - px(8) - iconSize / 2;
        Transform.rotatedAbout(graphics, (float) Math.PI * shown, iconCenterX, centerY(), () ->
                Draw.icon(graphics, CHEVRON_ICON, iconCenterX - iconSize / 2, centerY() - iconSize / 2, iconSize, Theme.DIM));
        rect(graphics, getRight() - px(24), centerY() - px(5), Math.max(0.5f, scale), px(10), 0, Theme.CONTROL);
        drawRowValue(graphics, getRight() - px(30), getWidth() / 2f - px(30));
    }

    @Override
    protected void drawValue(GuiGraphicsExtractor graphics) {
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(5), Theme.ROW);
        drawRow(graphics, revealed());
    }

    @Override
    protected void drawPopup(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                             float shown, int mouseX, int mouseY) {
        float listHeight = height * shown;
        boolean below = opensBelow();
        float listTop = below ? getBottom() : getY() - listHeight;
        float boxTop = below ? getY() : listTop;
        float boxBottom = below ? getBottom() + listHeight : getBottom();
        rect(graphics, x, boxTop, width, boxBottom - boxTop, px(5), Theme.ROW);
        drawRow(graphics, shown);
        var listArea = new ScreenRectangle(Math.round(x), (int) Math.floor(listTop), Math.round(width), (int) Math.ceil(listHeight));
        Scissor.clip(listArea, () -> Opacity.with(shown, () -> drawContents(graphics, x, listTop, width, height)));
    }
}
