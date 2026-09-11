package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class AccentPicker extends Popup {
    private static final int[] PRESETS = {0xB8DDB0, 0xB7CCE8, 0xD2BCE7, 0xE5B6C4, 0xE6D5AD};
    private static final float DOT = 8f;
    private static final float GAP = 6f;
    private static final float WINDOW_GAP = 4f;

    private final IntSupplier get;
    private final IntConsumer set;
    private final HsvWindow window;
    private final Transition ring;

    public AccentPicker(int x, int y, int height, float scale, IntSupplier get, IntConsumer set) {
        super(x, y, Math.round((PRESETS.length + 1) * (DOT + GAP) * scale + GAP * scale), height, scale,
                Component.literal(Lang.get("accent")));
        this.get = get;
        this.set = set;
        this.window = new HsvWindow(this, get, set);
        this.ring = new Transition(ringTarget(), 150);
    }

    private float ringTarget() {
        int selected = presetIndex();
        return selected < 0 ? PRESETS.length + 1 : selected;
    }

    private float dotX(int index) {
        return getX() + index * px(DOT + GAP);
    }

    private float customX() {
        return dotX(PRESETS.length) + px(GAP);
    }

    private int presetIndex() {
        int current = Colors.rgb(get.getAsInt());
        for (int i = 0; i < PRESETS.length; i++) if (PRESETS[i] == current) return i;
        return -1;
    }

    private void drawSelection(GuiGraphicsExtractor graphics, float x) {
        float ring = px(DOT + 3);
        float gap = px(DOT + 1.5f);
        float centerX = x + px(DOT) / 2;
        rect(graphics, centerX - ring / 2, centerY() - ring / 2, ring, ring, ring / 2, Theme.TEXT);
        rect(graphics, centerX - gap / 2, centerY() - gap / 2, gap, gap, gap / 2, Theme.OVERLAY);
    }

    @Override
    protected void drawValue(GuiGraphicsExtractor graphics) {
        ring.set(ringTarget());
        float position = ring.value();
        float ringX = position <= PRESETS.length
                ? getX() + position * px(DOT + GAP)
                : customX() - (PRESETS.length + 1 - position) * px(DOT + 2 * GAP);
        drawSelection(graphics, ringX);
        float dot = px(DOT);
        float top = centerY() - dot / 2;
        for (int i = 0; i < PRESETS.length; i++) {
            rect(graphics, dotX(i), top, dot, dot, dot / 2, Colors.opaque(PRESETS[i]));
        }
        float dividerX = dotX(PRESETS.length) + px(GAP) / 2 - px(3);
        rect(graphics, dividerX, centerY() - px(4), Math.max(0.5f, scale), px(8), 0, Theme.CONTROL);
        Draw.hueBar(graphics, Math.round(customX()), Math.round(top), Math.round(dot), Math.round(dot), Math.round(dot) / 2);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        float dot = px(DOT);
        for (int i = 0; i < PRESETS.length; i++) {
            if (event.x() >= dotX(i) - px(2) && event.x() < dotX(i) + dot + px(2)) {
                set.accept(PRESETS[i]);
                return;
            }
        }
        if (event.x() >= customX() - px(2)) open();
    }

    @Override
    protected void onOpen() {
        window.readColor();
    }

    @Override
    protected float popupWidth() {
        return px(HsvWindow.WIDTH);
    }

    @Override
    protected float popupX() {
        return getRight() - popupWidth();
    }

    @Override
    protected float popupHeight() {
        return px(HsvWindow.HEIGHT);
    }

    private boolean opensBelow() {
        return getBottom() + px(WINDOW_GAP) + popupHeight() <= bottomLimit();
    }

    @Override
    protected float popupY() {
        return opensBelow() ? getBottom() + px(WINDOW_GAP) : getY() - px(WINDOW_GAP) - popupHeight();
    }

    @Override
    protected void drawPopup(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                             float shown, int mouseX, int mouseY) {
        window.draw(graphics, x, y, shown, !opensBelow());
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
    public boolean keyPressed(KeyEvent event) {
        if (active && isFocused() && !isOpen() && (event.isLeft() || event.isRight())) {
            int next = Math.floorMod(presetIndex() + (event.isRight() ? 1 : -1), PRESETS.length);
            set.accept(PRESETS[next]);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(Lang.get("accent") + ": " + Colors.hex(get.getAsInt())));
        output.add(NarratedElementType.USAGE, Component.literal(
                "Left and right pick a preset. Enter opens the custom picker."));
    }
}
