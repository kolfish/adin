package dev.koifih.client.ui.component;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ThemeSwitch extends Control {
    private static final int LIGHT_ICON = 0xe518;
    private static final int DARK_ICON = 0xe51c;
    private static final Theme.Mode[] MODES = {Theme.Mode.LIGHT, Theme.Mode.DARK};
    private static final String[] LABEL_KEYS = {"theme.light", "theme.dark"};
    private static final int[] ICONS = {LIGHT_ICON, DARK_ICON};

    private final Transition highlight;

    public ThemeSwitch(int x, int y, int width, int height, float scale) {
        super(x, y, width, height, scale, Component.literal("Theme"));
        this.highlight = new Transition(index(Theme.mode()), 150, Easing.EASE_OUT_CUBIC);
    }

    private static int index(Theme.Mode mode) {
        return mode == Theme.Mode.LIGHT ? 0 : 1;
    }

    private float pillWidth() {
        return getWidth() / (float) MODES.length;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        highlight.set(index(Theme.mode()));
        float pill = pillWidth();
        float radius = px(4);
        rect(graphics, getX(), getY(), getWidth(), getHeight(), radius, Theme.CONTROL);
        rect(graphics, getX() + pill * highlight.value(), getY(), pill, getHeight(), radius, Theme.CONTROL_ACTIVE);
        float iconSize = px(8);
        float size = px(7);
        for (int i = 0; i < MODES.length; i++) {
            boolean selected = Theme.mode() == MODES[i];
            int color = selected ? Theme.TEXT : Theme.DIM;
            String label = Lang.get(LABEL_KEYS[i]);
            float contentWidth = iconSize + px(3) + Text.width(label, size);
            float startX = getX() + pill * i + (pill - contentWidth) / 2;
            Draw.icon(graphics, ICONS[i], startX, centerY() - iconSize / 2, iconSize, color);
            text(graphics, label, startX + iconSize + px(3), centerY(), size, color);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int index = Math.clamp((int) ((event.x() - getX()) / pillWidth()), 0, MODES.length - 1);
        Theme.setMode(MODES[index]);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (event.isLeft()) Theme.setMode(Theme.Mode.LIGHT);
        else if (event.isRight()) Theme.setMode(Theme.Mode.DARK);
        else if (event.isSelection()) Theme.setMode(Theme.mode() == Theme.Mode.DARK ? Theme.Mode.LIGHT : Theme.Mode.DARK);
        else return super.keyPressed(event);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Theme: " + Lang.get(LABEL_KEYS[index(Theme.mode())])));
        output.add(NarratedElementType.USAGE, Component.literal("Left selects light, right selects dark."));
    }
}
