package dev.koifih.client.gui.component;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class Clickable extends Control {
    private final Runnable action;

    protected Clickable(int x, int y, int width, int height, float scale, Component label, Runnable action) {
        super(x, y, width, height, scale, label);
        this.action = action;
    }

    protected void activate() {
        action.run();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        activate();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (active && isFocused() && event.isSelection()) {
            activate();
            return true;
        }
        return super.keyPressed(event);
    }
}
