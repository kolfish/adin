package dev.koifih.client.render.blocks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Util;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockIndex {
    public record Matches(long[] bits, AABB[] shapes) {
        public boolean has(int index) {
            return (bits[index >>> 6] & (1L << index)) != 0;
        }

        public AABB shape(int index) {
            return shapes == null ? null : shapes[index];
        }

        public int count() {
            int total = 0;
            for (long word : bits) total += Long.bitCount(word);
            return total;
        }
    }

    public static final int SIZE = 16;
    public static final int VOLUME = SIZE * SIZE * SIZE;
    private static final int WORDS = VOLUME / Long.SIZE;
    private static final int RESCAN_BUDGET = 64;

    private static final Map<Long, Matches> sections = new ConcurrentHashMap<>();
    private static final Map<BlockState, AABB> shapes = new ConcurrentHashMap<>();
    private static final AABB FULL = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final Set<Long> dirty = ConcurrentHashMap.newKeySet();
    private static volatile Set<Block> blocks = Set.of();
    private static LongConsumer listener = key -> {};

    public static void init(LongConsumer onChanged) {
        listener = onChanged;
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> scanChunk(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> forgetChunk(chunk));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    public static void configure(Set<Block> selected) {
        blocks = Set.copyOf(selected);
        clear();
        rescanAll();
    }

    public static boolean active() {
        return !blocks.isEmpty();
    }

    public static Matches matches(long key) {
        return sections.get(key);
    }

    public static int index(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    private static AABB shape(BlockState state) {
        return shapes.computeIfAbsent(state, key -> {
            VoxelShape shape = key.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (shape.isEmpty() || Block.isShapeFullBlock(shape)) return FULL;
            return shape.bounds();
        });
    }

    public static void onBlockChanged(LevelChunk chunk, BlockPos pos, BlockState from, BlockState to) {
        if (!active() || !chunk.getLevel().isClientSide()) return;
        if (blocks.contains(from.getBlock()) || blocks.contains(to.getBlock())) {
            dirty.add(SectionPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4));
        }
    }

    public static void drain() {
        if (dirty.isEmpty()) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            dirty.clear();
            return;
        }
        int budget = RESCAN_BUDGET;
        for (Long key : dirty) {
            if (budget-- == 0) break;
            dirty.remove(key);
            LevelChunk chunk = level.getChunkSource().getChunk(SectionPos.x(key), SectionPos.z(key), ChunkStatus.FULL, false);
            if (chunk == null) forget(key);
            else scan(chunk, chunk.getSectionIndexFromSectionY(SectionPos.y(key)));
        }
    }

    private static void rescanAll() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null || client.player == null || !active()) return;
        int radius = client.options.renderDistance().get() + 1;
        int centerX = client.player.chunkPosition().x();
        int centerZ = client.player.chunkPosition().z();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                LevelChunk chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null) scanChunk(chunk);
            }
        }
    }

    private static void scanChunk(LevelChunk chunk) {
        if (!active()) return;
        for (int index = 0; index < chunk.getSectionsCount(); index++) scan(chunk, index);
    }

    private static void scan(LevelChunk chunk, int index) {
        if (index < 0 || index >= chunk.getSectionsCount()) return;
        long key = key(chunk, index);
        LevelChunkSection section = chunk.getSection(index);
        Predicate<BlockState> matcher = matcher();
        if (section.hasOnlyAir() || !section.maybeHas(matcher)) {
            forget(key);
            return;
        }
        PalettedContainer<BlockState> states = section.getStates().copy();
        Util.backgroundExecutor().execute(() -> {
            long[] bits = new long[WORDS];
            AABB[] bounds = null;
            boolean any = false;
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    for (int x = 0; x < SIZE; x++) {
                        BlockState state = states.get(x, y, z);
                        if (!matcher.test(state)) continue;
                        int bit = index(x, y, z);
                        bits[bit >>> 6] |= 1L << bit;
                        any = true;
                        AABB shape = shape(state);
                        if (shape == FULL) continue;
                        if (bounds == null) bounds = new AABB[VOLUME];
                        bounds[bit] = shape;
                    }
                }
            }
            if (any) sections.put(key, new Matches(bits, bounds));
            else sections.remove(key);
            listener.accept(key);
        });
    }

    private static Predicate<BlockState> matcher() {
        Set<Block> selected = blocks;
        return state -> selected.contains(state.getBlock());
    }

    private static void forgetChunk(LevelChunk chunk) {
        for (int index = 0; index < chunk.getSectionsCount(); index++) forget(key(chunk, index));
    }

    private static void forget(long key) {
        if (sections.remove(key) != null) listener.accept(key);
    }

    private static long key(LevelChunk chunk, int index) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(chunk.getPos().getMinBlockX()),
                chunk.getSectionYFromSectionIndex(index), SectionPos.blockToSectionCoord(chunk.getPos().getMinBlockZ()));
    }

    private static void clear() {
        dirty.clear();
        for (Long key : sections.keySet()) forget(key);
        sections.clear();
    }
}
