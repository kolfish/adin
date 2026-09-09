package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
import dev.koifih.client.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

abstract class OptionPopup extends RowPopup {
    private static final int CHECK_ICON = 0xe5ca;
    private static final float OPTION_HEIGHT = 15f;
    private static final int OPTION_REVEAL_MILLIS = 150;
    private static final float OPTION_MIN_ZOOM = 0.9f;

    protected final String[] options;
    protected int highlighted;
    private final Transition[] checks;
    private final Transition[] reveals;
    private boolean checksSynced;
    private IntPredicate optionVisible = option -> true;

    protected OptionPopup(int x, int y, int width, int height, float scale, Component label, String[] options) {
        super(x, y, width, height, scale, label);
        this.options = options;
        this.checks = new Transition[options.length];
        this.reveals = new Transition[options.length];
        for (int i = 0; i < options.length; i++) {
            checks[i] = new Transition(0f, 120, Easing.EASE_OUT_CUBIC);
            reveals[i] = new Transition(1f, OPTION_REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
        }
    }

    protected abstract boolean isChosen(int option);

    protected abstract void choose(int option);

    protected abstract String value();

    public void setOptionVisible(IntPredicate visible) {
        optionVisible = visible;
    }

    protected boolean isOptionVisible(int option) {
        return optionVisible.test(option);
    }

    protected List<Integer> visibleOptions() {
        List<Integer> visible = new ArrayList<>();
        for (int i = 0; i < options.length; i++) if (optionVisible.test(i)) visible.add(i);
        return visible;
    }

    private void syncReveals() {
        for (int i = 0; i < options.length; i++) {
            float target = optionVisible.test(i) ? 1f : 0f;
            if (checksSynced) reveals[i].set(target);
            else reveals[i].snap(target);
        }
    }

    private float rowSpan() {
        float span = 0f;
        for (Transition reveal : reveals) span += reveal.value();
        return span;
    }

    @Override
    protected float popupHeight() {
        return px(4 + rowSpan() * OPTION_HEIGHT);
    }

    private int optionAt(double y) {
        float offset = (float) ((y - popupY() - px(2)) / px(OPTION_HEIGHT));
        if (offset < 0f) return -1;
        for (int i = 0; i < options.length; i++) {
            float span = reveals[i].value();
            if (offset < span) return span >= 1f && optionVisible.test(i) ? i : -1;
            offset -= span;
        }
        return -1;
    }

    @Override
    protected void drawRowValue(GuiGraphicsExtractor graphics, float rightLimit, float available) {
        float size = px(7);
        String label = value();
        float labelWidth = Text.width(label, size);
        float labelX = rightLimit - labelWidth;
        if (labelWidth <= available) {
            text(graphics, label, labelX, centerY(), size, Theme.TEXT);
        } else {
            float fadeFrom = rightLimit - available;
            Text.drawFaded(graphics, label, labelX, Text.centeredBaseline(label, size, centerY()),
                    size, Theme.TEXT, fadeFrom, fadeFrom + px(18));
        }
    }

    @Override
    protected void drawContents(GuiGraphicsExtractor graphics, float x, float top, float width, float height) {
        syncReveals();
        if (!checksSynced) {
            for (int i = 0; i < options.length; i++) checks[i].snap(isChosen(i) ? 1f : 0f);
            checksSynced = true;
        } else if (isOpen()) {
            for (int i = 0; i < options.length; i++) checks[i].set(isChosen(i) ? 1f : 0f);
        }
        float offset = 0f;
        for (int i = 0; i < options.length; i++) {
            float shown = reveals[i].value();
            if (shown > 0f) {
                float center = top + px(2) + px(OPTION_HEIGHT) * (offset + shown * 0.5f);
                drawOption(graphics, i, x, center, width, shown);
            }
            offset += shown;
        }
    }

    private void drawOption(GuiGraphicsExtractor graphics, int option, float x, float center, float width, float shown) {
        float iconSize = px(8);
        float chosen = checks[option].value();
        Transform.popIn(graphics, x + width * 0.5f, center, shown, OPTION_MIN_ZOOM, () -> {
            if (chosen > 0f) {
                float size = iconSize * (0.6f + 0.4f * chosen);
                Opacity.with(chosen, () -> Draw.icon(graphics, CHECK_ICON, x + px(9) + (iconSize - size) / 2,
                        center - size / 2, size, Theme.ACCENT));
            }
            text(graphics, fit(options[option], width - px(30), px(7)), x + px(22), center, px(7), Colors.lerp(Theme.DIM, Theme.TEXT, chosen));
        });
    }

    @Override
    protected void clickPopup(double x, double y) {
        int option = optionAt(y);
        if (option < 0) return;
        highlighted = option;
        choose(option);
    }

    @Override
    protected void popupKeyPressed(KeyEvent event) {
        List<Integer> visible = visibleOptions();
        if (visible.isEmpty()) return;
        int row = Math.max(0, visible.indexOf(highlighted));
        if (event.isDown()) highlighted = visible.get((row + 1) % visible.size());
        else if (event.isUp()) highlighted = visible.get((row + visible.size() - 1) % visible.size());
        else if (event.isSelection() && isOptionVisible(highlighted)) choose(highlighted);
    }
}
