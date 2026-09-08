package dev.koifih.client.module.impl.combat;

import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ShieldBreaker extends Module {
    private enum Stage {
        IDLE,
        SWITCHED,
        STRUCK,
        STUNNED
    }

    private final SliderSetting delay = add(new SliderSetting("delay", 100, 0, 500));
    private final BoolSetting facing = add(new BoolSetting("facing", true));
    private final BoolSetting stun = add(new BoolSetting("stun", true));
    private final BoolSetting switchBack = add(new BoolSetting("switchBack", true));
    private Stage stage = Stage.IDLE;
    private long stageAt;
    private int originalSlot = Hotbar.NONE;

    public ShieldBreaker() {
        super("shieldBreaker", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        stage = Stage.IDLE;
        originalSlot = Hotbar.NONE;
    }

    private void onTick(PreTickEvent event) {
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        if (!Game.playing(client) || player.isBlocking()) {
            abort(player);
            return;
        }
        Player target = crosshairPlayer(client, player);
        Player blocking = target != null && target.isBlocking() && (!facing.get() || facingUs(player, target)) ? target : null;
        switch (stage) {
            case IDLE -> begin(player, blocking);
            case SWITCHED -> strike(client, player, blocking);
            case STRUCK -> stun(client, player, target);
            case STUNNED -> finish(player);
        }
    }

    private void begin(LocalPlayer player, Player target) {
        if (target == null) return;
        if (player.getMainHandItem().is(ItemTags.AXES)) {
            originalSlot = Hotbar.NONE;
            stage = Stage.SWITCHED;
            stageAt = 0L;
            return;
        }
        int axe = Hotbar.find(player, stack -> stack.is(ItemTags.AXES));
        if (axe == Hotbar.NONE) return;
        originalSlot = Hotbar.selected(player);
        Hotbar.select(player, axe);
        advance(Stage.SWITCHED);
    }

    private void strike(Minecraft client, LocalPlayer player, Player target) {
        if (!elapsed()) return;
        if (target == null) {
            finish(player);
            return;
        }
        client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
        advance(Stage.STRUCK);
    }

    private void stun(Minecraft client, LocalPlayer player, Player target) {
        if (!stun.get()) {
            finish(player);
            return;
        }
        if (target != null) {
            client.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
        advance(Stage.STUNNED);
    }

    private void finish(LocalPlayer player) {
        if (elapsed()) abort(player);
    }

    private void abort(LocalPlayer player) {
        if (switchBack.get() && originalSlot != Hotbar.NONE) Hotbar.select(player, originalSlot);
        originalSlot = Hotbar.NONE;
        stage = Stage.IDLE;
    }

    private void advance(Stage next) {
        stage = next;
        stageAt = System.currentTimeMillis();
    }

    private boolean elapsed() {
        return System.currentTimeMillis() - stageAt >= delay.get();
    }

    private static Player crosshairPlayer(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player target)) return null;
        return target != player && target.isAlive() && !target.isSpectator() ? target : null;
    }

    private static boolean facingUs(LocalPlayer player, Player target) {
        Vec3 toPlayer = player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
        return target.getViewVector(1f).multiply(1.0, 0.0, 1.0).dot(toPlayer) > 0.0;
    }
}
