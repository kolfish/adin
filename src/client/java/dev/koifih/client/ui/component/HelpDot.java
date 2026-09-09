package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
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
        Text.drawCenteredX(graphics, "?", getX() + getWidth() / 2f, centerY(), size, Theme.DIM);
        float amount = shown.value();
        if (amount <= 0f) return;
        graphics.nextStratum();
        String text = tooltip.get();
        float textSize = px(6.5f);
        float width = Text.width(text, textSize) + 2 * px(TOOLTIP_PADDING);
        float height = px(14);
        float x = Math.max(px(2), getX() + getWidth() / 2f - width / 2f);
        float y = getY() - px(TOOLTIP_GAP) - height;
        Transform.translated(graphics, 0f, px(3) * (1f - amount), () -> Opacity.with(amount, () -> {
            Draw.bordered(graphics, x, y, width, height, px(4), Theme.POPUP, Theme.POPUP_BORDER);
            text(graphics, text, x + px(TOOLTIP_PADDING), y + height / 2, textSize, Theme.TEXT);
        }));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(tooltip.get()));
    }
}
