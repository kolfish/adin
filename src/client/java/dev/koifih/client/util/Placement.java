package dev.koifih.client.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

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

    public static BlockHitResult placeInto(Level level, Vec3 eye, BlockPos target) {
        BlockHitResult best = null;
        for (Direction face : Direction.values()) {
            BlockPos against = target.relative(face.getOpposite());
            if (!level.getBlockState(against).isFaceSturdy(level, against, face)) continue;
            best = nearer(eye, best, candidate(level, eye, against, face));
        }
        return best;
    }

    public static BlockHitResult clickOn(Level level, Vec3 eye, BlockPos block) {
        BlockHitResult best = null;
        for (Direction face : Direction.values()) {
            BlockPos neighbor = block.relative(face);
            if (!level.getBlockState(neighbor).getCollisionShape(level, neighbor).isEmpty()) continue;
            best = nearer(eye, best, candidate(level, eye, block, face));
        }
        return best;
    }

    public static boolean looksAt(Level level, BlockPos pos, Vec3 eye, Vec3 look, double range) {
        return shape(level, pos).clip(eye, eye.add(look.scale(range))).isPresent();
    }

    public static AABB shape(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
    }

    private static BlockHitResult candidate(Level level, Vec3 eye, BlockPos block, Direction face) {
        if (!canClick(eye, new AABB(block), face)) return null;
        return new BlockHitResult(faceCenter(shape(level, block), face), face, block, false);
    }

    private static BlockHitResult nearer(Vec3 eye, BlockHitResult current, BlockHitResult candidate) {
        if (candidate == null) return current;
        if (current == null || candidate.getLocation().distanceToSqr(eye) < current.getLocation().distanceToSqr(eye)) return candidate;
        return current;
    }

    private static boolean canClick(Vec3 eye, AABB block, Direction face) {
        if (block.contains(eye)) return true;
        return switch (face) {
            case UP -> eye.y >= block.maxY;
            case DOWN -> eye.y <= block.minY;
            case NORTH -> eye.z <= block.minZ;
            case SOUTH -> eye.z >= block.maxZ;
            case WEST -> eye.x <= block.minX;
            case EAST -> eye.x >= block.maxX;
        };
    }

    private static Vec3 faceCenter(AABB box, Direction face) {
        Vec3 center = box.getCenter();
        return switch (face) {
            case UP -> new Vec3(center.x, box.maxY, center.z);
            case DOWN -> new Vec3(center.x, box.minY, center.z);
            case NORTH -> new Vec3(center.x, center.y, box.minZ);
            case SOUTH -> new Vec3(center.x, center.y, box.maxZ);
            case WEST -> new Vec3(box.minX, center.y, center.z);
            case EAST -> new Vec3(box.maxX, center.y, center.z);
        };
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
