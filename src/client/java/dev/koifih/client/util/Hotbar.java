package dev.koifih.client.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.function.Predicate;

public final class Hotbar {
    public static final int NONE = -1;

    private Hotbar() {}

    public static int selected(LocalPlayer player) {
        return player.getInventory().getSelectedSlot();
    }

    public static void select(LocalPlayer player, int slot) {
        player.getInventory().setSelectedSlot(slot);
    }

    public static int find(LocalPlayer player, Predicate<ItemStack> matcher) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
            if (matcher.test(inventory.getItem(slot))) return slot;
        }
        return NONE;
    }
}
