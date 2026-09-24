package dev.koifih.client.ui.clickgui;

import dev.koifih.client.ui.component.popup.Popup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.List;

public interface Page {
    void init(PanelLayout layout);

    void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta);

    void setState(boolean shown, boolean interactive);

    default void relayout(PanelLayout layout) {}

    default List<Popup> popups() {
        return List.of();
    }

    default boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    default boolean keyPressed(KeyEvent event) {
        return false;
    }

    default boolean mouseScrolled(double x, double y, double dx, double dy) {
        return false;
    }

    default void reset() {}
}
