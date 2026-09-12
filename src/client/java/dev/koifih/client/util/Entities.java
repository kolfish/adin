package dev.koifih.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Entities {
    public static String id(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static boolean isLocal(Entity entity) {
        return entity == Game.player();
    }

    public static boolean holdsWeapon(LivingEntity entity) {
        return entity.getMainHandItem().has(DataComponents.WEAPON);
    }
}
