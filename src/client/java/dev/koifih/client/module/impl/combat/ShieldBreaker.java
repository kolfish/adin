package dev.koifih.client.module.impl.combat;

import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.accessor.MinecraftAccessor;
import dev.koifih.client.friends.FriendList;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Time;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ShieldBreaker extends Module {
    private final SliderSetting delay = add(new SliderSetting("delay", 100, 1, 500));
    private final BoolSetting facing = add(new BoolSetting("facing", true));
    private final BoolSetting switchBack = add(new BoolSetting("switchBack", true));
    private final Time.Stopwatch sinceSwitch = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;

    public ShieldBreaker() {
        super("shieldBreaker");
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        restore(Game.player());
    }

    private void onTick(PreTickEvent event) {
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        if (originalSlot != Hotbar.NONE) {
            if (player == null || !switchBack.get() || sinceSwitch.elapsed(delay.get())) restore(player);
            return;
        }
        if (!Game.playing(client) || player.isBlocking() || player.getAttackStrengthScale(0.5f) < 1f) return;
        Player target = crosshairPlayer(client, player);
        if (target == null) return;
        prepare(player, target);
        if (player.getMainHandItem().is(ItemTags.AXES)) ((MinecraftAccessor) client).adin$startAttack();
    }

    public void prepare(LocalPlayer player, Entity target) {
        if (!isEnabled() || player.getMainHandItem().is(ItemTags.AXES)) return;
        if (!(target instanceof Player other) || !shielded(player, other)) return;
        int axe = Hotbar.find(player, stack -> stack.is(ItemTags.AXES));
        if (axe == Hotbar.NONE) return;
        int selected = Hotbar.selected(player);
        if (!Hotbar.swap(player, axe, false)) return;
        originalSlot = selected;
        sinceSwitch.reset();
    }

    private void restore(LocalPlayer player) {
        if (player != null && switchBack.get() && originalSlot != Hotbar.NONE && !Hotbar.swap(player, originalSlot, false)) return;
        originalSlot = Hotbar.NONE;
    }

    private boolean shielded(LocalPlayer player, Player target) {
        return target.isBlocking() && (!facing.get() || facingUs(player, target));
    }

    private static boolean facingUs(LocalPlayer player, Player target) {
        Vec3 toPlayer = player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
        return target.getViewVector(1f).multiply(1.0, 0.0, 1.0).dot(toPlayer) > 0.0;
    }

    private static Player crosshairPlayer(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player target)) return null;
        return target != player && target.isAlive() && !target.isSpectator() && !FriendList.protects(target) ? target : null;
    }
}
