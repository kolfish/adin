package dev.koifih.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class Entities {
    private Entities() {}

    public static String id(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static boolean isLocal(Entity entity) {
        return entity == Minecraft.getInstance().player;
    }

    public static boolean holdsWeapon(LivingEntity entity) {
        return entity.getMainHandItem().has(DataComponents.WEAPON);
    }
}
