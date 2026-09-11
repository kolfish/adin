package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.util.Colors;
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
        this.thumb = new Transition(get.getAsBoolean() ? 1f : 0f, 150, Transition.Easing.SMOOTHSTEP);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        thumb.set(get.getAsBoolean() ? 1f : 0f);
        float on = thumb.value();
        int radius = getHeight() / 2;
        Draw.rect(graphics, getX(), getY(), getWidth(), getHeight(), radius,
                Colors.lerp(Theme.TOGGLE_OFF_BORDER, Theme.ACCENT, on));
        Draw.rect(graphics, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2,
                Math.max(0, radius - 1), Colors.lerp(Theme.TOGGLE_OFF, Theme.ACCENT, on));
        int inset = Math.max(1, Math.round(getHeight() / 6f));
        int size = Math.max(1, getHeight() - 2 * inset);
        float thumbX = getX() + inset + (getWidth() - 2 * inset - size) * on;
        Draw.rect(graphics, thumbX, getY() + inset, size, size, size / 2,
                Colors.lerp(Theme.THUMB_OFF, Theme.ON_ACCENT, on));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        setMessage(Component.literal(label.getString() + ": " + (get.getAsBoolean() ? "on" : "off")));
        super.updateWidgetNarration(output);
    }
}
