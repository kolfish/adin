package dev.koifih.client.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot adin$getHoveredSlot();

    @Accessor("leftPos")
    int adin$getLeftPos();

    @Accessor("topPos")
    int adin$getTopPos();

    @Invoker("slotClicked")
    void adin$slotClicked(Slot slot, int index, int button, ContainerInput input);
}
