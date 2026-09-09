package dev.koifih.client.ui.component;

import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public final class RangeSlider extends Control {
    private final int min;
    private final int max;
    private final IntSupplier lowGet;
    private final IntConsumer lowSet;
    private final IntSupplier highGet;
    private final IntConsumer highSet;
    private boolean draggingHigh;
    private boolean dragging;
    private IntFunction<String> format = Integer::toString;
    private final Transition lowPosition;
    private final Transition highPosition;

    public RangeSlider(int x, int y, int width, int height, float scale, Component label, int min, int max,
                       IntSupplier lowGet, IntConsumer lowSet, IntSupplier highGet, IntConsumer highSet) {
        super(x, y, width, height, scale, label);
        this.min = min;
        this.max = max;
        this.lowGet = lowGet;
        this.lowSet = lowSet;
        this.highGet = highGet;
        this.highSet = highSet;
        this.lowPosition = new Transition(lowGet.getAsInt(), 120, Easing.EASE_OUT_CUBIC);
        this.highPosition = new Transition(highGet.getAsInt(), 120, Easing.EASE_OUT_CUBIC);
    }

    public void setFormat(IntFunction<String> format) {
        this.format = format;
    }

    private float knobWidth() {
        return px(10);
    }

    private float valueWidth() {
        return px(30);
    }

    private float trackWidth() {
        return getWidth() - valueWidth();
    }

    private float travel() {
        return Math.max(1f, trackWidth() - knobWidth());
    }

    private float knobX(float value) {
        return getX() + travel() * (value - min) / (max - min);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float trackHeight = px(3);
        float trackY = centerY() - trackHeight / 2;
        float knobHeight = px(6);
        if (dragging && !draggingHigh) lowPosition.snap(lowGet.getAsInt());
        else lowPosition.set(lowGet.getAsInt());
        if (dragging && draggingHigh) highPosition.snap(highGet.getAsInt());
        else highPosition.set(highGet.getAsInt());
        float lowX = knobX(lowPosition.value());
        float highX = knobX(highPosition.value());
        rect(graphics, getX(), trackY, trackWidth(), trackHeight, trackHeight / 2, Theme.CONTROL);
        rect(graphics, lowX + knobWidth() / 2, trackY, highX - lowX, trackHeight, trackHeight / 2, Theme.ACCENT);
        rect(graphics, lowX, centerY() - knobHeight / 2, knobWidth(), knobHeight, px(2), Theme.ACCENT);
        rect(graphics, highX, centerY() - knobHeight / 2, knobWidth(), knobHeight, px(2), Theme.ACCENT);
        text(graphics, format.apply(lowGet.getAsInt()) + "-" + format.apply(highGet.getAsInt()), getRight() - valueWidth() + px(6), centerY(),
                px(7), Theme.TEXT);
    }

    private int valueAt(double mouseX) {
        float fraction = (float) ((mouseX - getX() - knobWidth() / 2) / travel());
        return Math.clamp(Math.round(min + fraction * (max - min)), min, max);
    }

    private void move(int value) {
        value = Math.clamp(value, min, max);
        int low = lowGet.getAsInt();
        int high = highGet.getAsInt();
        if (draggingHigh && value < low) {
            lowSet.accept(value);
            highSet.accept(low);
            draggingHigh = false;
        } else if (!draggingHigh && value > high) {
            highSet.accept(value);
            lowSet.accept(high);
            draggingHigh = true;
        } else if (draggingHigh) {
            highSet.accept(value);
        } else {
            lowSet.accept(value);
        }
    }

    private void drag(double mouseX) {
        move(valueAt(mouseX));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int value = valueAt(event.x());
        int low = lowGet.getAsInt();
        int high = highGet.getAsInt();
        draggingHigh = Math.abs(value - high) < Math.abs(value - low) || (value > high);
        dragging = true;
        drag(event.x());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (dragging) drag(event.x());
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        dragging = false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (event.isUp() || event.isDown()) {
            draggingHigh = !draggingHigh;
            return true;
        }
        if (event.isLeft() || event.isRight()) {
            int step = (event.isRight() ? 1 : -1) * (event.hasShiftDown() ? 10 : 1);
            move((draggingHigh ? highGet.getAsInt() : lowGet.getAsInt()) + step);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(
                getMessage().getString() + ": " + format.apply(lowGet.getAsInt()) + " to " + format.apply(highGet.getAsInt())));
        output.add(NarratedElementType.USAGE, Component.literal(
                "Up and down pick a knob. Left and right move it."));
    }
}
