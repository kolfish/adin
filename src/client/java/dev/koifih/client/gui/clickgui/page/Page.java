package dev.koifih.client.gui.clickgui.page;

import dev.koifih.client.gui.clickgui.PanelLayout;
import dev.koifih.client.gui.component.Popup;
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

    default void reset() {}
}
