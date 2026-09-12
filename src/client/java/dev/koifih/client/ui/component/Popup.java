package dev.koifih.client.ui.component;

import dev.koifih.client.ui.Transition;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class Popup extends Control {
    private boolean open;
    private final Transition reveal = new Transition(0f, 150);
    @Setter
    private int bottomLimit = Integer.MAX_VALUE;

    protected Popup(int x, int y, int width, int height, float scale, Component label) {
        super(x, y, width, height, scale, label);
    }

    public boolean isOpen() {
        return open;
    }

    protected int bottomLimit() {
        return bottomLimit;
    }

    @Override
    public boolean capturesInput() {
        return active && visible && open;
    }

    @Override
    public void dismiss() {
        open = false;
        reveal.set(0f);
        releasePopup();
    }

    protected void open() {
        open = true;
        reveal.set(1f);
        onOpen();
    }

    protected float revealed() {
        return reveal.value();
    }

    protected void onOpen() {
    }

    protected float popupX() {
        return getX();
    }

    protected float popupWidth() {
        return getWidth();
    }

    protected abstract float popupHeight();

    protected float popupY() {
        float below = getBottom() + px(3);
        return below + popupHeight() <= bottomLimit ? below : getY() - popupHeight() - px(3);
    }

    protected boolean insidePopup(double x, double y) {
        return x >= popupX() && x < popupX() + popupWidth() && y >= popupY() && y < popupY() + popupHeight();
    }

    protected abstract void drawValue(GuiGraphicsExtractor graphics);

    protected abstract void drawPopup(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                                      float shown, int mouseX, int mouseY);

    protected abstract void clickPopup(double x, double y);

    protected void dragPopup(double x, double y) {
    }

    protected void releasePopup() {
    }

    protected abstract void popupKeyPressed(KeyEvent event);

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawValue(graphics);
    }

    public void renderPopup(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        float shown = reveal.value();
        if (!open && shown <= 0f) return;
        graphics.nextStratum();
        drawPopup(graphics, popupX(), popupY(), popupWidth(), popupHeight(), shown, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!active || !visible) return false;
        if (open) {
            if (insidePopup(event.x(), event.y())) {
                clickPopup(event.x(), event.y());
                return true;
            }
            dismiss();
            return isMouseOver(event.x(), event.y());
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        open();
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (open) dragPopup(event.x(), event.y());
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        releasePopup();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (open) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_TAB) dismiss();
            else popupKeyPressed(event);
            return true;
        }
        if (event.isSelection()) {
            open();
            return true;
        }
        return super.keyPressed(event);
    }
}
