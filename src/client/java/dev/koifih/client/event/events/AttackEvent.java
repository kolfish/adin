package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record AttackEvent(Player player, Entity target) implements Event {
}
