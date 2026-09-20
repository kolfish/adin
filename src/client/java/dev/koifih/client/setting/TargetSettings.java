package dev.koifih.client.setting;

import dev.koifih.client.friends.FriendList;
import dev.koifih.client.module.impl.misc.AntiBot;
import dev.koifih.client.util.Entities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.List;

public final class TargetSettings implements SettingGroup {
    private static final String[] OPTIONS = {"Players", "Invisible", "Entities"};
    private static final int PLAYERS = 0;
    private static final int INVISIBLE = 1;
    private static final int ENTITIES = 2;

    private final MultiSetting targets = new MultiSetting("targets", OPTIONS, PLAYERS);
    private final EntitySetting entities = new EntitySetting("entities");

    public TargetSettings() {
        entities.visibleWhen(() -> targets.has(ENTITIES));
    }

    @Override
    public List<Setting<?>> settings() {
        return List.of(targets, entities);
    }

    public boolean accepts(Player self, LivingEntity entity) {
        if (entity == self || !entity.isAlive() || entity.isSpectator() || FriendList.protects(entity)) return false;
        if (AntiBot.flags(entity)) return false;
        if (entity.isInvisible() && !targets.has(INVISIBLE)) return false;
        if (entity instanceof Player) return targets.has(PLAYERS);
        return targets.has(ENTITIES) && entities.get().contains(Entities.id(entity));
    }
}
