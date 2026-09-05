package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.utils.Colors;
import dev.koifih.client.rendering.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class ColorPicker extends RowPopup {
    private static final float WINDOW_GAP = 4f;

    private final IntSupplier get;
    private final HsvWindow window;

    public ColorPicker(int x, int y, int width, int height, float scale, Component label,
                       IntSupplier get, IntConsumer set) {
        super(x, y, width, height, scale, label);
        this.get = get;
        this.window = new HsvWindow(this, get, set);
    }

    @Override
    protected void onOpen() {
        window.readColor();
    }

    @Override
    protected float popupWidth() {
        return Math.min(getWidth(), px(HsvWindow.WIDTH));
    }

    @Override
    protected float popupX() {
        return getRight() - popupWidth();
    }

    @Override
    protected float popupHeight() {
        return px(HsvWindow.HEIGHT);
    }

    @Override
    protected boolean opensBelow() {
        return getBottom() + px(WINDOW_GAP) + popupHeight() <= bottomLimit();
    }

    @Override
    protected float popupY() {
        return opensBelow() ? getBottom() + px(WINDOW_GAP) : getY() - px(WINDOW_GAP) - popupHeight();
    }

    @Override
    protected void drawRowValue(GuiGraphicsExtractor graphics, float rightLimit, float available) {
        float size = px(7);
        String label = Colors.hex(get.getAsInt());
        float labelWidth = TextRenderer.width(label, size);
        text(graphics, label, rightLimit - labelWidth, centerY(), size, Theme.TEXT);
        float swatch = px(9);
        rect(graphics, rightLimit - labelWidth - px(5) - swatch, centerY() - swatch / 2, swatch, swatch, px(2),
                Colors.opaque(get.getAsInt()));
    }

    @Override
    protected void drawPopup(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                             float shown, int mouseX, int mouseY) {
        window.draw(graphics, x, y, shown, !opensBelow());
    }

    @Override
    protected void drawContents(GuiGraphicsExtractor graphics, float x, float top, float width, float height) {
    }

    @Override
    protected void clickPopup(double x, double y) {
        window.click(x, y, popupX(), popupY());
    }

    @Override
    protected void dragPopup(double x, double y) {
        window.drag(x, y, popupX(), popupY());
    }

    @Override
    protected void releasePopup() {
        window.release();
    }

    @Override
    protected void popupKeyPressed(KeyEvent event) {
        window.key(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + Colors.hex(get.getAsInt())));
        output.add(NarratedElementType.USAGE, Component.literal(
                "Enter opens the picker. Left and right change hue, up and down change brightness."));
    }
}
