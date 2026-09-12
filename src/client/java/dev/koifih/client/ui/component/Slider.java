package dev.koifih.client.ui.component;

import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import lombok.Setter;
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
    private final IntSupplier[] gets;
    private final IntConsumer[] sets;
    private final Transition[] positions;
    @Setter
    private IntFunction<String> format = Integer::toString;
    private int knob;
    private boolean dragging;

    public Slider(int x, int y, int width, int height, float scale, Component label, int min, int max,
                  IntSupplier get, IntConsumer set) {
        this(x, y, width, height, scale, label, min, max, new IntSupplier[] {get}, new IntConsumer[] {set});
    }

    private Slider(int x, int y, int width, int height, float scale, Component label, int min, int max,
                   IntSupplier[] gets, IntConsumer[] sets) {
        super(x, y, width, height, scale, label);
        this.min = min;
        this.max = max;
        this.gets = gets;
        this.sets = sets;
        this.positions = new Transition[gets.length];
        for (int i = 0; i < gets.length; i++) positions[i] = new Transition(gets[i].getAsInt(), 120);
    }

    public static Slider range(int x, int y, int width, int height, float scale, Component label, int min, int max,
                               IntSupplier lowGet, IntConsumer lowSet, IntSupplier highGet, IntConsumer highSet) {
        return new Slider(x, y, width, height, scale, label, min, max,
                new IntSupplier[] {lowGet, highGet}, new IntConsumer[] {lowSet, highSet});
    }

    private boolean ranged() {
        return gets.length == 2;
    }

    private int value(int index) {
        return gets[index].getAsInt();
    }

    private String valueText(String separator) {
        return ranged() ? format.apply(value(0)) + separator + format.apply(value(1)) : format.apply(value(0));
    }

    private float valueWidth() {
        float size = px(7);
        float widest = ranged()
                ? Text.width(format.apply(max) + "-" + format.apply(max), size)
                : Math.max(Text.width(format.apply(min), size), Text.width(format.apply(max), size));
        return Math.max(px(18), widest + px(8));
    }

    private float knobWidth() {
        return px(10);
    }

    private float travel() {
        return Math.max(1f, getWidth() - valueWidth() - knobWidth());
    }

    private float knobX(float value) {
        return getX() + travel() * (value - min) / (max - min);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        for (int i = 0; i < positions.length; i++) {
            if (dragging && knob == i) positions[i].snap(value(i));
            else positions[i].set(value(i));
        }
        float trackHeight = px(3);
        float trackY = centerY() - trackHeight / 2;
        float knobHeight = px(6);
        float fillFrom = ranged() ? knobX(positions[0].value()) + knobWidth() / 2 : getX();
        float fillTo = knobX(positions[positions.length - 1].value()) + knobWidth() / 2;
        rect(graphics, getX(), trackY, getWidth() - valueWidth(), trackHeight, trackHeight / 2, Theme.CONTROL);
        rect(graphics, fillFrom, trackY, fillTo - fillFrom, trackHeight, trackHeight / 2, Theme.ACCENT);
        for (Transition position : positions) {
            rect(graphics, knobX(position.value()), centerY() - knobHeight / 2, knobWidth(), knobHeight, px(2), Theme.ACCENT);
        }
        text(graphics, valueText("-"), getRight() - valueWidth() + px(6), centerY(), px(7), Theme.TEXT);
    }

    private int valueAt(double mouseX) {
        float fraction = (float) ((mouseX - getX() - knobWidth() / 2) / travel());
        return Math.clamp(Math.round(min + fraction * (max - min)), min, max);
    }

    private void move(int value) {
        value = Math.clamp(value, min, max);
        if (!ranged()) {
            sets[0].accept(value);
            return;
        }
        int low = value(0);
        int high = value(1);
        if (knob == 1 && value < low) {
            sets[0].accept(value);
            sets[1].accept(low);
            knob = 0;
        } else if (knob == 0 && value > high) {
            sets[1].accept(value);
            sets[0].accept(high);
            knob = 1;
        } else {
            sets[knob].accept(value);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int value = valueAt(event.x());
        if (ranged()) knob = value > value(1) || Math.abs(value - value(1)) < Math.abs(value - value(0)) ? 1 : 0;
        dragging = true;
        move(value);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (dragging) move(valueAt(event.x()));
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        dragging = false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (ranged() && (event.isUp() || event.isDown())) {
            knob = 1 - knob;
            return true;
        }
        if (event.isLeft() || event.isRight()) {
            int step = (event.isRight() ? 1 : -1) * (event.hasShiftDown() ? 10 : 1);
            move(value(knob) + step);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + valueText(" to ")));
        output.add(NarratedElementType.USAGE, Component.literal(ranged()
                ? "Up and down pick a knob. Left and right move it." : "Arrow keys adjust the value."));
    }
}
