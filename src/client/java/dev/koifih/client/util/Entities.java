package dev.koifih.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Entities {
    private static final Map<EntityType<?>, String> IDS = new ConcurrentHashMap<>();

    public static String id(Entity entity) {
        return IDS.computeIfAbsent(entity.getType(), type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
    }

    public static boolean isLocal(Entity entity) {
        return entity == Game.player();
    }

    public static boolean holdsWeapon(LivingEntity entity) {
        return entity.getMainHandItem().has(DataComponents.WEAPON);
    }
}
