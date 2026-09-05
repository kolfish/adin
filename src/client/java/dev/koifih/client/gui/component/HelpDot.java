package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.rendering.Opacity;
import dev.koifih.client.rendering.TextRenderer;
import dev.koifih.client.rendering.Draw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

public final class HelpDot extends Control {
    private static final float TOOLTIP_PADDING = 5f;
    private static final float TOOLTIP_GAP = 4f;

    private final Supplier<String> tooltip;
    private final Transition shown = new Transition(0f, 120, Easing.EASE_OUT_CUBIC);

    public HelpDot(int x, int y, int size, float scale, Supplier<String> tooltip) {
        super(x, y, size, size, scale, Component.literal("Help"));
        this.tooltip = tooltip;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean hovered = active && mouseX >= getX() && mouseX < getRight() && mouseY >= getY() && mouseY < getBottom();
        shown.set(hovered || isFocused() ? 1f : 0f);
        float radius = getWidth() / 2f;
        rect(graphics, getX(), getY(), getWidth(), getHeight(), radius, Theme.CONTROL);
        float size = px(6.5f);
        TextRenderer.drawCenteredX(graphics, "?", getX() + getWidth() / 2f, centerY(), size, Theme.DIM);
        float amount = shown.value();
        if (amount <= 0f) return;
        graphics.nextStratum();
        String text = tooltip.get();
        float textSize = px(6.5f);
        float width = TextRenderer.width(text, textSize) + 2 * px(TOOLTIP_PADDING);
        float height = px(14);
        float x = Math.max(px(2), getX() + getWidth() / 2f - width / 2f);
        float y = getY() - px(TOOLTIP_GAP) - height;
        Draw.translated(graphics, 0f, px(3) * (1f - amount), () -> Opacity.with(amount, () -> {
            Draw.borderedBox(graphics, x, y, width, height, px(4), Theme.POPUP);
            text(graphics, text, x + px(TOOLTIP_PADDING), y + height / 2, textSize, Theme.TEXT);
        }));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(tooltip.get()));
    }
}
