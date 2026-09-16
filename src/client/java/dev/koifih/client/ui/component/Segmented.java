package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class Segmented extends Control {
    public record Segment(int icon, String label) {
        public static Segment of(String label) {
            return new Segment(-1, label);
        }

        public static Segment of(int icon) {
            return new Segment(icon, null);
        }

        public static Segment of(int icon, String label) {
            return new Segment(icon, label);
        }
    }

    private static final float TEXT_SIZE = 7f;
    private static final float ICON_SIZE = 8f;
    private static final float ICON_GAP = 3f;
    private static final float SEGMENT_PADDING = 12f;

    private static final float STRETCH = 0.18f;

    private final Segment[] segments;
    private final IntSupplier get;
    private final IntConsumer set;
    private final Transition highlight;

    public Segmented(int x, int y, int width, int height, float scale, Component label,
                     Segment[] segments, IntSupplier get, IntConsumer set) {
        super(x, y, width, height, scale, label);
        this.segments = segments;
        this.get = get;
        this.set = set;
        this.highlight = new Transition(get.getAsInt(), 190, Transition.Easing.EASE_OUT_EXPO);
    }

    public static int preferredWidth(Segment[] segments, float scale) {
        float widest = 0;
        for (Segment segment : segments) widest = Math.max(widest, contentWidth(segment, segment.label(), scale));
        return (int) Math.ceil(segments.length * (widest + SEGMENT_PADDING * scale));
    }

    private static float contentWidth(Segment segment, String label, float scale) {
        float width = 0;
        if (segment.icon() >= 0) width += ICON_SIZE * scale;
        if (segment.icon() >= 0 && label != null) width += ICON_GAP * scale;
        if (label != null) width += Text.width(label, TEXT_SIZE * scale);
        return width;
    }

    private float segmentWidth() {
        return getWidth() / (float) segments.length;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int selected = get.getAsInt();
        highlight.set(selected);
        float segment = segmentWidth();
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(4), Theme.CONTROL);
        float index = Math.clamp(highlight.value(), 0f, segments.length - 1f);
        float wide = segment * (1f + STRETCH * highlight.flight());
        float slot = Math.clamp(getX() + segment * index - (wide - segment) * 0.5f, getX(), getX() + getWidth() - wide);
        rect(graphics, slot, getY(), wide, getHeight(), px(4), Theme.CONTROL_ACTIVE);
        for (int i = 0; i < segments.length; i++) {
            Segment item = segments[i];
            int color = selected == i ? Theme.TEXT : Theme.DIM;
            float iconSpan = item.icon() >= 0 ? px(ICON_SIZE + ICON_GAP) : 0f;
            String label = item.label() == null ? null : fit(item.label(), segment - px(6) - iconSpan, px(TEXT_SIZE));
            float x = getX() + segment * i + (segment - contentWidth(item, label, scale)) / 2;
            if (item.icon() >= 0) {
                Draw.icon(graphics, item.icon(), x, centerY() - px(ICON_SIZE) / 2, px(ICON_SIZE), color);
                x += iconSpan;
            }
            if (label != null) text(graphics, label, x, centerY(), px(TEXT_SIZE), color);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        set.accept(Math.clamp((int) ((event.x() - getX()) / segmentWidth()), 0, segments.length - 1));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        int selected = get.getAsInt();
        if (event.isLeft()) set.accept(Math.max(0, selected - 1));
        else if (event.isRight()) set.accept(Math.min(segments.length - 1, selected + 1));
        else if (event.isSelection()) set.accept((selected + 1) % segments.length);
        else return super.keyPressed(event);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        int selected = get.getAsInt();
        String name = segments[selected].label() != null ? segments[selected].label() : String.valueOf(selected + 1);
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + name));
        output.add(NarratedElementType.USAGE, Component.literal("Left and right arrows change the selection."));
    }
}
