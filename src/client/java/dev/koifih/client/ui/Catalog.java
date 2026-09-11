package dev.koifih.client.ui;

import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class Catalog {
    public interface Group {
        String label();
    }

    public record Entry(String id, String name, Object subject) {}

    public static final Catalog ENTITIES = new Entities();
    public static final Catalog BLOCKS = new Blocks();

    private Map<Group, List<Entry>> entries;

    public abstract Group[] groups();

    public abstract boolean canPreview(Entry entry);

    public abstract void drawPreview(GuiGraphicsExtractor graphics, Entry entry, int x0, int y0, int x1, int y1, float yawDegrees);

    protected abstract void collect(BiConsumer<Group, Entry> out);

    public List<Entry> entries(Group group) {
        if (entries == null) {
            Map<Group, List<Entry>> built = new HashMap<>();
            for (Group each : groups()) built.put(each, new ArrayList<>());
            collect((each, entry) -> built.get(each).add(entry));
            for (List<Entry> list : built.values()) list.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
            entries = built;
        }
        return entries.get(group);
    }

    private static final class Entities extends Catalog {
        private enum Group implements Catalog.Group {
            BOSSES("entities.bosses"),
            UNDEAD("entities.undead"),
            RAIDERS("entities.raiders"),
            ARTHROPODS("entities.arthropods"),
            AQUATIC("entities.aquatic"),
            HOSTILE("entities.hostile"),
            PASSIVE("entities.passive"),
            OTHER("entities.other");

            private final String key;

            Group(String key) {
                this.key = key;
            }

            @Override
            public String label() {
                return Lang.get(key);
            }
        }

        private static final Set<EntityType<?>> BOSS_TYPES = Set.of(EntityTypes.ENDER_DRAGON, EntityTypes.WITHER, EntityTypes.WARDEN);

        @Override
        public Catalog.Group[] groups() {
            return Group.values();
        }

        @Override
        public boolean canPreview(Entry entry) {
            return EntityPreview.canPreviewEntity((EntityType<?>) entry.subject());
        }

        @Override
        public void drawPreview(GuiGraphicsExtractor graphics, Entry entry, int x0, int y0, int x1, int y1, float yawDegrees) {
            EntityPreview.drawEntity(graphics, (EntityType<?>) entry.subject(), x0, y0, x1, y1, yawDegrees);
        }

        @Override
        protected void collect(BiConsumer<Catalog.Group, Entry> out) {
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                out.accept(groupOf(type), new Entry(id, type.getDescription().getString(), type));
            }
        }

        private static Group groupOf(EntityType<?> type) {
            if (BOSS_TYPES.contains(type)) return Group.BOSSES;
            var holder = type.builtInRegistryHolder();
            if (holder.is(EntityTypeTags.UNDEAD)) return Group.UNDEAD;
            if (holder.is(EntityTypeTags.RAIDERS)) return Group.RAIDERS;
            if (holder.is(EntityTypeTags.ARTHROPOD)) return Group.ARTHROPODS;
            if (holder.is(EntityTypeTags.AQUATIC)) return Group.AQUATIC;
            MobCategory category = type.getCategory();
            if (category == MobCategory.MONSTER) return Group.HOSTILE;
            if (category == MobCategory.MISC) return Group.OTHER;
            return Group.PASSIVE;
        }
    }

    private static final class Blocks extends Catalog {
        private enum Group implements Catalog.Group {
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

        @Override
        public Catalog.Group[] groups() {
            return Group.values();
        }

        @Override
        public boolean canPreview(Entry entry) {
            return ((Block) entry.subject()).defaultBlockState().getRenderShape() == RenderShape.MODEL;
        }

        @Override
        public void drawPreview(GuiGraphicsExtractor graphics, Entry entry, int x0, int y0, int x1, int y1, float yawDegrees) {
            EntityPreview.drawBlock(graphics, (Block) entry.subject(), x0, y0, x1, y1, yawDegrees);
        }

        @Override
        protected void collect(BiConsumer<Catalog.Group, Entry> out) {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block.defaultBlockState().isAir()) continue;
                String id = BuiltInRegistries.BLOCK.getKey(block).toString();
                out.accept(groupOf(block, id.substring(id.indexOf(':') + 1)), new Entry(id, block.getName().getString(), block));
            }
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
}
