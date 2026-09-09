package dev.koifih.client.ui.component;

import dev.koifih.client.render.Text;
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

public final class Slider extends Control {
    private final int min;
    private final int max;
    private final IntSupplier get;
    private final IntConsumer set;
    private boolean dragging;
    private final Transition position;
    private IntFunction<String> format = Integer::toString;

    public Slider(int x, int y, int width, int height, float scale, Component label,
                  int min, int max, IntSupplier get, IntConsumer set) {
        super(x, y, width, height, scale, label);
        this.min = min;
        this.max = max;
        this.get = get;
        this.set = set;
        this.position = new Transition(get.getAsInt(), 120, Easing.EASE_OUT_CUBIC);
    }

    public void setFormat(IntFunction<String> format) {
        this.format = format;
    }

    private float knobWidth() {
        return px(10);
    }

    private float valueWidth() {
        float widest = Math.max(Text.width(format.apply(min), px(7)), Text.width(format.apply(max), px(7)));
        return Math.max(px(18), widest + px(8));
    }

    private float trackWidth() {
        return getWidth() - valueWidth();
    }

    private float travel() {
        return Math.max(1f, trackWidth() - knobWidth());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (dragging) position.snap(get.getAsInt());
        else position.set(get.getAsInt());
        float fraction = (position.value() - min) / (max - min);
        float trackHeight = px(3);
        float trackY = centerY() - trackHeight / 2;
        float knobHeight = px(6);
        float knobX = getX() + travel() * fraction;
        rect(graphics, getX(), trackY, trackWidth(), trackHeight, trackHeight / 2, Theme.CONTROL);
        rect(graphics, getX(), trackY, knobX + knobWidth() / 2 - getX(), trackHeight, trackHeight / 2, Theme.ACCENT);
        rect(graphics, knobX, centerY() - knobHeight / 2, knobWidth(), knobHeight, px(2), Theme.ACCENT);
        text(graphics, format.apply(get.getAsInt()), getRight() - valueWidth() + px(6), centerY(), px(7), Theme.TEXT);
    }

    private void drag(double mouseX) {
        float fraction = (float) ((mouseX - getX() - knobWidth() / 2) / travel());
        set.accept(Math.clamp(Math.round(min + fraction * (max - min)), min, max));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
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
        if (active && isFocused() && (event.isLeft() || event.isRight())) {
            int step = (event.isRight() ? 1 : -1) * (event.hasShiftDown() ? 10 : 1);
            set.accept(Math.clamp(get.getAsInt() + step, min, max));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + format.apply(get.getAsInt())));
        output.add(NarratedElementType.USAGE, Component.literal("Arrow keys adjust the value."));
    }
}
