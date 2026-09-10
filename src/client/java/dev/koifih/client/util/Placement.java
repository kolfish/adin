package dev.koifih.client.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class Placement {
    private static final float DUPLICATE_ROTATION = 2f;
    private static final float DUPLICATE_TOLERANCE = 0.0001f;

    private static boolean placed;
    private static boolean rotated;
    private static float lastRotationDelta;
    private static float lastPlacedDelta = -1f;

    private Placement() {}

    public static boolean ready() {
        return !placed && !(rotated && duplicates(lastRotationDelta));
    }

    public static boolean duplicates(float yawDelta) {
        return yawDelta > DUPLICATE_ROTATION && Math.abs(yawDelta - lastPlacedDelta) < DUPLICATE_TOLERANCE;
    }

    public static void rotated(float yawDelta) {
        lastRotationDelta = yawDelta;
        rotated = true;
    }

    public static void predict(LocalPlayer player, ItemStack stack, BlockHitResult hit) {
        if (!(stack.getItem() instanceof BlockItem item)) return;
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        BlockState state = item.getBlock().getStateForPlacement(context);
        if (state != null) Game.level().setBlock(context.getClickedPos(), state, Block.UPDATE_ALL_IMMEDIATE);
    }

    public static void sent(Packet<?> packet) {
        if (packet instanceof ServerboundClientTickEndPacket) {
            placed = false;
        } else if (packet instanceof ServerboundUseItemOnPacket) {
            placed = true;
            if (rotated) lastPlacedDelta = lastRotationDelta;
            rotated = false;
        }
    }
}
