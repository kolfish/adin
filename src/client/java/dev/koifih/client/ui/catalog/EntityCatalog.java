package dev.koifih.client.gui.catalog;

import dev.koifih.client.render.preview.EntityPreview;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EntityCatalog implements Catalog {
    public enum Group implements Catalog.Group {
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

    public static final EntityCatalog INSTANCE = new EntityCatalog();
    private static final Set<EntityType<?>> BOSS_TYPES = Set.of(EntityTypes.ENDER_DRAGON, EntityTypes.WITHER, EntityTypes.WARDEN);

    private Map<Group, List<Entry>> groups;
    private List<Entry> all;

    private EntityCatalog() {}

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
        EntityPreview.drawEntity(graphics, (EntityType<?>) entry.subject(), x0, y0, x1, y1, yawDegrees);
    }

    @Override
    public boolean canPreview(Entry entry) {
        return EntityPreview.canPreviewEntity((EntityType<?>) entry.subject());
    }

    private void build() {
        if (groups != null) return;
        groups = new EnumMap<>(Group.class);
        for (Group group : Group.values()) groups.put(group, new ArrayList<>());
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            groups.get(groupOf(type)).add(new Entry(id.toString(), type.getDescription().getString(), type));
        }
        all = new ArrayList<>();
        for (List<Entry> entries : groups.values()) {
            entries.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
            all.addAll(entries);
        }
        all.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
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
