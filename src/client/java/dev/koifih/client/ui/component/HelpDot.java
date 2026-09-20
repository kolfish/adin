package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Supplier;

public final class HelpDot extends Control {
    private static final float TOOLTIP_PADDING = 5f;
    private static final float TOOLTIP_GAP = 4f;
    private static final float TOOLTIP_LINE = 9f;
    private static final int TOOLTIP_LINES = 3;

    private final Supplier<String> tooltip;
    private final Transition shown = new Transition(0f, 120);
    @Setter
    private float leftLimit;
    @Setter
    private float rightLimit = Float.MAX_VALUE;

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
    }

    public void renderTooltip(GuiGraphicsExtractor graphics) {
        float amount = shown.value();
        if (amount <= 0f) return;
        graphics.nextStratum();
        float textSize = px(6.5f);
        float padding = px(TOOLTIP_PADDING);
        float lineHeight = px(TOOLTIP_LINE);
        List<String> lines = Text.wrap(tooltip.get(), rightLimit - leftLimit - 2 * padding, textSize, TOOLTIP_LINES);
        float widest = 0f;
        for (String line : lines) widest = Math.max(widest, Text.width(line, textSize));
        float width = widest + 2 * padding;
        float height = lines.size() * lineHeight + px(5);
        float x = Math.max(Math.min(getX() + getWidth() / 2f - width / 2f, rightLimit - width), leftLimit);
        float y = getY() - px(TOOLTIP_GAP) - height;
        Transform.translated(graphics, 0f, px(3) * (1f - amount), () -> Opacity.with(amount, () -> {
            Draw.bordered(graphics, x, y, width, height, px(4), Theme.POPUP, Theme.POPUP_BORDER);
            for (int i = 0; i < lines.size(); i++) {
                text(graphics, lines.get(i), x + padding, y + px(2.5f) + lineHeight * (i + 0.5f), textSize, Theme.TEXT);
            }
        }));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(tooltip.get()));
    }
}
