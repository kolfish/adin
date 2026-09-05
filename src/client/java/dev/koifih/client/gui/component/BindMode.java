package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.rendering.IconRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BindMode extends Control {
    private static final int TOGGLE_ICON = 0xea18;
    private static final int HOLD_ICON = 0xe913;

    private final BooleanSupplier hold;
    private final Consumer<Boolean> set;
    private final Transition highlight;

    public BindMode(int x, int y, int width, int height, float scale, BooleanSupplier hold, Consumer<Boolean> set) {
        super(x, y, width, height, scale, Component.literal("Bind mode"));
        this.hold = hold;
        this.set = set;
        this.highlight = new Transition(hold.getAsBoolean() ? 1f : 0f, 150, Easing.EASE_OUT_CUBIC);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean holding = hold.getAsBoolean();
        highlight.set(holding ? 1f : 0f);
        int half = getWidth() / 2;
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(3), Theme.CONTROL);
        rect(graphics, getX() + half * highlight.value(), getY(), half, getHeight(), px(3), Theme.CONTROL_ACTIVE);
        float iconSize = getHeight() * 0.7f;
        float iconY = getY() + (getHeight() - iconSize) / 2;
        IconRenderer.draw(graphics, TOGGLE_ICON, getX() + (half - iconSize) / 2, iconY, iconSize,
                holding ? Theme.DIM : Theme.TEXT);
        IconRenderer.draw(graphics, HOLD_ICON, getX() + half + (half - iconSize) / 2, iconY, iconSize,
                holding ? Theme.TEXT : Theme.DIM);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        set.accept(event.x() >= getX() + getWidth() / 2.0);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (event.isLeft()) set.accept(false);
        else if (event.isRight()) set.accept(true);
        else if (event.isSelection()) set.accept(!hold.getAsBoolean());
        else return super.keyPressed(event);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Bind mode: " + (hold.getAsBoolean() ? "hold" : "toggle")));
        output.add(NarratedElementType.USAGE, Component.literal("Left arrow selects toggle, right arrow selects hold."));
    }
}
