package dev.koifih.client.ui.component;

import dev.koifih.client.render.Switch;
import dev.koifih.client.ui.Transition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class Bool extends Control {
    private final Component label;
    private final BooleanSupplier get;
    private final Transition thumb;

    public Bool(int x, int y, int width, int height, float scale, Component label,
                BooleanSupplier get, Consumer<Boolean> set) {
        super(x, y, width, height, scale, label, () -> set.accept(!get.getAsBoolean()));
        this.label = label;
        this.get = get;
        this.thumb = new Transition(get.getAsBoolean() ? 1f : 0f, 170, 170,
                Transition.Easing.EASE_OUT_SETTLE, Transition.Easing.EASE_OUT_CUBIC);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        thumb.set(get.getAsBoolean() ? 1f : 0f);
        Switch.draw(graphics, getX(), getY(), getWidth(), getHeight(), Math.clamp(thumb.value(), 0f, 1f), thumb.flight());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        setMessage(Component.literal(label.getString() + ": " + (get.getAsBoolean() ? "on" : "off")));
        super.updateWidgetNarration(output);
    }
}
