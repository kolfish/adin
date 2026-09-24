package dev.koifih.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Players {
    private static final float MOVE_EPSILON = 1.0E-10f;

    public static boolean isMoving(LocalPlayer player) {
        return player.input.getMoveVector().lengthSquared() > MOVE_EPSILON;
    }

    public static boolean canCrit(LocalPlayer player) {
        return player.fallDistance > 0.0 && !player.onClimbable() && !player.isInWater()
                && !player.isMobilityRestricted() && !player.isPassenger();
    }

    public static boolean consuming(LocalPlayer player) {
        if (player.isUsingItem()) return true;
        ItemStack main = player.getMainHandItem();
        if (main.has(DataComponents.CONSUMABLE)) return true;
        return player.getOffhandItem().has(DataComponents.CONSUMABLE) && !(main.getItem() instanceof BlockItem);
    }
}
