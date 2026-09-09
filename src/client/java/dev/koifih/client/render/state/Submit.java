package dev.koifih.client.render.state;

import dev.koifih.client.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public final class Submit {
    private Submit() {}

    public static void submit(GuiGraphicsExtractor graphics, GuiElementRenderState state) {
        ((GuiGraphicsExtractorAccessor) graphics).adin$getGuiRenderState().addGuiElement(state);
    }
}
