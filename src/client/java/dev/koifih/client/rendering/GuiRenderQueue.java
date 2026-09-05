package dev.koifih.client.rendering;

import dev.koifih.client.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public final class GuiRenderQueue {
    private GuiRenderQueue() {}

    public static void submit(GuiGraphicsExtractor graphics, GuiElementRenderState state) {
        ((GuiGraphicsExtractorAccessor) graphics).adin$getGuiRenderState().addGuiElement(state);
    }
}
