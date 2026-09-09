package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.render.gui.Text;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class Segmented extends Control {
    private final String[] labels;
    private final IntSupplier get;
    private final IntConsumer set;
    private final Transition highlight;

    public Segmented(int x, int y, int width, int height, float scale, Component label,
                     String[] labels, IntSupplier get, IntConsumer set) {
        super(x, y, width, height, scale, label);
        this.labels = labels;
        this.get = get;
        this.set = set;
        this.highlight = new Transition(get.getAsInt(), 150, Easing.EASE_OUT_CUBIC);
    }

    public static int preferredWidth(String[] labels, float scale) {
        float widest = 0;
        for (String label : labels) widest = Math.max(widest, Text.width(label, 6.5f * scale));
        return (int) Math.ceil(labels.length * (widest + 12 * scale));
    }

    private float segmentWidth() {
        return getWidth() / (float) labels.length;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        highlight.set(get.getAsInt());
        float segment = segmentWidth();
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(4), Theme.CONTROL);
        rect(graphics, getX() + segment * highlight.value(), getY(), segment, getHeight(), px(4), Theme.CONTROL_ACTIVE);
        float size = px(6.5f);
        for (int i = 0; i < labels.length; i++) {
            String label = fit(labels[i], segment - px(6), size);
            float x = getX() + segment * i + (segment - Text.width(label, size)) / 2;
            text(graphics, label, x, centerY(), size, get.getAsInt() == i ? Theme.TEXT : Theme.DIM);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        set.accept(Math.clamp((int) ((event.x() - getX()) / segmentWidth()), 0, labels.length - 1));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (event.isLeft()) set.accept(Math.max(0, get.getAsInt() - 1));
        else if (event.isRight()) set.accept(Math.min(labels.length - 1, get.getAsInt() + 1));
        else return super.keyPressed(event);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + labels[get.getAsInt()]));
        output.add(NarratedElementType.USAGE, Component.literal("Left and right arrows change the selection."));
    }
}
