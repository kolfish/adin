package dev.koifih.client.gui.clickgui;

import net.minecraft.client.gui.components.AbstractWidget;

public interface WidgetHost {
    <T extends AbstractWidget> T add(T widget);

    void remove(AbstractWidget widget);

    void requestRebuild();

    void dropFocus();
}
