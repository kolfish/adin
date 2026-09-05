package dev.koifih.client.gui.catalog;

import dev.koifih.client.gui.Lang;

import dev.koifih.client.rendering.Previews;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class BlockCatalog implements Catalog {
    public enum Group implements Catalog.Group {
        ORES("blocks.ores"),
        WOOD("blocks.wood"),
        STONE("blocks.stone"),
        PLANTS("blocks.plants"),
        LIQUIDS("blocks.liquids"),
        REDSTONE("blocks.redstone"),
        UTILITY("blocks.utility"),
        OTHER("blocks.other");

        private final String key;

        Group(String key) {
            this.key = key;
        }

        @Override
        public String label() {
            return Lang.get(key);
        }
    }

    public static final BlockCatalog INSTANCE = new BlockCatalog();
    private static final String[] STONE_WORDS = {"stone", "brick", "deepslate", "cobble", "andesite", "diorite", "granite",
            "tuff", "basalt", "calcite", "dripstone", "obsidian", "netherrack", "end_stone", "purpur", "prismarine", "quartz"};
    private static final String[] PLANT_WORDS = {"grass", "fern", "vine", "moss", "mushroom", "fungus", "roots", "lily",
            "kelp", "seagrass", "coral", "bamboo", "cactus", "sugar_cane", "azalea", "flower", "sprout", "bush", "petal",
            "pumpkin", "melon", "wart", "nylium", "hanging", "leaf", "pickle", "chorus", "dead_", "spore", "pitcher"};
    private static final String[] REDSTONE_WORDS = {"redstone", "piston", "observer", "comparator", "repeater", "lever",
            "hopper", "dropper", "dispenser", "tripwire", "daylight", "note_block", "tnt", "target", "sculk_sensor",
            "lightning_rod", "bell", "detector", "copper_bulb", "crafter"};
    private static final String[] UTILITY_WORDS = {"chest", "barrel", "furnace", "smoker", "brewing", "crafting", "anvil",
            "enchanting", "grindstone", "loom", "stonecutter", "cartography", "smithing", "beacon", "jukebox", "campfire",
            "lectern", "composter", "cauldron", "bed", "respawn_anchor", "ender_chest", "bookshelf", "fletching",
            "conduit", "lodestone", "scaffolding", "ladder", "torch", "lantern", "vault", "trial_spawner"};

    private Map<Group, List<Entry>> groups;
    private List<Entry> all;

    private BlockCatalog() {}

    @Override
    public Catalog.Group[] groups() {
        return Group.values();
    }

    @Override
    public List<Entry> entries(Catalog.Group group) {
        build();
        return groups.get((Group) group);
    }

    @Override
    public List<Entry> all() {
        build();
        return all;
    }

    @Override
    public void drawPreview(GuiGraphicsExtractor graphics, Entry entry, int x0, int y0, int x1, int y1, float yawDegrees) {
        Previews.drawBlock(graphics, (Block) entry.subject(), x0, y0, x1, y1, yawDegrees);
    }

    @Override
    public boolean canPreview(Entry entry) {
        return ((Block) entry.subject()).defaultBlockState().getRenderShape() == RenderShape.MODEL;
    }

    private void build() {
        if (groups != null) return;
        groups = new EnumMap<>(Group.class);
        for (Group group : Group.values()) groups.put(group, new ArrayList<>());
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block.defaultBlockState().isAir()) continue;
            String id = BuiltInRegistries.BLOCK.getKey(block).toString();
            groups.get(groupOf(block, id.substring(id.indexOf(':') + 1))).add(new Entry(id, block.getName().getString(), block));
        }
        all = new ArrayList<>();
        for (List<Entry> entries : groups.values()) {
            entries.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
            all.addAll(entries);
        }
        all.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
    }

    private static boolean containsAny(String path, String[] words) {
        for (String word : words) if (path.contains(word)) return true;
        return false;
    }

    private static Group groupOf(Block block, String path) {
        var holder = block.builtInRegistryHolder();
        if (block instanceof LiquidBlock) return Group.LIQUIDS;
        if (path.contains("_ore") || path.equals("ancient_debris") || path.startsWith("raw_")) return Group.ORES;
        if (holder.is(BlockTags.BUTTONS) || holder.is(BlockTags.PRESSURE_PLATES) || holder.is(BlockTags.RAILS)
                || containsAny(path, REDSTONE_WORDS)) return Group.REDSTONE;
        if (holder.is(BlockTags.LOGS) || holder.is(BlockTags.PLANKS) || holder.is(BlockTags.WOODEN_DOORS)
                || holder.is(BlockTags.WOODEN_STAIRS) || holder.is(BlockTags.WOODEN_SLABS) || holder.is(BlockTags.WOODEN_FENCES)
                || holder.is(BlockTags.WOODEN_TRAPDOORS) || holder.is(BlockTags.FENCE_GATES) || holder.is(BlockTags.SIGNS)
                || path.contains("wood") || path.contains("stripped") || path.contains("hyphae") || path.contains("stem")) {
            return Group.WOOD;
        }
        if (holder.is(BlockTags.LEAVES) || holder.is(BlockTags.FLOWERS) || path.contains("sapling")
                || holder.is(BlockTags.CROPS) || containsAny(path, PLANT_WORDS)) return Group.PLANTS;
        if (holder.is(BlockTags.BASE_STONE_OVERWORLD) || holder.is(BlockTags.BASE_STONE_NETHER)
                || holder.is(BlockTags.STONE_BRICKS) || containsAny(path, STONE_WORDS)) return Group.STONE;
        if (containsAny(path, UTILITY_WORDS) || holder.is(BlockTags.SHULKER_BOXES) || holder.is(BlockTags.BEDS)) return Group.UTILITY;
        return Group.OTHER;
    }
}
