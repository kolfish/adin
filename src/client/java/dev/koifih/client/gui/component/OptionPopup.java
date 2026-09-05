package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.utils.Colors;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.rendering.Opacity;
import dev.koifih.client.rendering.IconRenderer;
import dev.koifih.client.rendering.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

abstract class OptionPopup extends RowPopup {
    private static final int CHECK_ICON = 0xe5ca;
    private static final float OPTION_HEIGHT = 15f;

    protected final String[] options;
    protected int highlighted;
    private final Transition[] checks;
    private boolean checksSynced;

    protected OptionPopup(int x, int y, int width, int height, float scale, Component label, String[] options) {
        super(x, y, width, height, scale, label);
        this.options = options;
        this.checks = new Transition[options.length];
        for (int i = 0; i < options.length; i++) checks[i] = new Transition(0f, 120, Easing.EASE_OUT_CUBIC);
    }

    protected abstract boolean isChosen(int option);

    protected abstract void choose(int option);

    protected abstract String value();

    @Override
    protected float popupHeight() {
        return px(4 + options.length * OPTION_HEIGHT);
    }

    private int optionAt(double y) {
        return (int) ((y - popupY() - px(2)) / px(OPTION_HEIGHT));
    }

    @Override
    protected void drawRowValue(GuiGraphicsExtractor graphics, float rightLimit, float available) {
        float size = px(7);
        String label = value();
        float labelWidth = TextRenderer.width(label, size);
        float labelX = rightLimit - labelWidth;
        if (labelWidth <= available) {
            text(graphics, label, labelX, centerY(), size, Theme.TEXT);
        } else {
            float fadeFrom = rightLimit - available;
            TextRenderer.drawFaded(graphics, label, labelX, TextRenderer.centeredBaseline(label, size, centerY()),
                    size, Theme.TEXT, fadeFrom, fadeFrom + px(18));
        }
    }

    @Override
    protected void drawContents(GuiGraphicsExtractor graphics, float x, float top, float width, float height) {
        if (!checksSynced) {
            for (int i = 0; i < options.length; i++) checks[i].snap(isChosen(i) ? 1f : 0f);
            checksSynced = true;
        } else if (isOpen()) {
            for (int i = 0; i < options.length; i++) checks[i].set(isChosen(i) ? 1f : 0f);
        }
        float iconSize = px(8);
        for (int i = 0; i < options.length; i++) {
            float center = top + px(2 + OPTION_HEIGHT * i + OPTION_HEIGHT / 2);
            float chosen = checks[i].value();
            if (chosen > 0f) {
                float size = iconSize * (0.6f + 0.4f * chosen);
                Opacity.with(chosen, () -> IconRenderer.draw(graphics, CHECK_ICON, x + px(9) + (iconSize - size) / 2,
                        center - size / 2, size, Theme.ACCENT));
            }
            text(graphics, fit(options[i], width - px(30), px(7)), x + px(22), center, px(7), Colors.lerp(Theme.DIM, Theme.TEXT, chosen));
        }
    }

    @Override
    protected void clickPopup(double x, double y) {
        int option = optionAt(y);
        if (option < 0 || option >= options.length) return;
        highlighted = option;
        choose(option);
    }

    @Override
    protected void popupKeyPressed(KeyEvent event) {
        if (event.isDown()) highlighted = (highlighted + 1) % options.length;
        else if (event.isUp()) highlighted = (highlighted + options.length - 1) % options.length;
        else if (event.isSelection()) choose(highlighted);
    }
}
