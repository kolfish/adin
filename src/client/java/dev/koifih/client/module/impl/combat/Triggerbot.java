package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.RangeSetting;
import dev.koifih.client.setting.TargetSettings;
import dev.koifih.client.util.Entities;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Maths;
import dev.koifih.client.util.Players;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class Triggerbot extends Module {
    private static final float NEXT_TICK = 1.5f;
    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;

    private final RangeSetting cooldown = add(new RangeSetting("cooldown", 60, 100, 0, 100, Measure.FRACTION));
    private final BoolSetting weaponsOnly = add(new BoolSetting("weaponsOnly", true));
    private final TargetSettings targets = add(new TargetSettings());
    private final BoolSetting crits = add(new BoolSetting("crits", true));

    private float threshold;
    private boolean holdingSprint;

    public Triggerbot() {
        super("triggerbot", Category.COMBAT);
    }

    @Override
    public String info() {
        return cooldown.format(cooldown.low()) + " - " + cooldown.format(cooldown.high());
    }

    public boolean holdsSprint() {
        return isEnabled() && holdingSprint;
    }

    @Override
    protected void onEnable() {
        threshold = roll();
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        holdingSprint = false;
    }

    private void onTick(PreTickEvent event) {
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        LivingEntity target = armed(client, player) ? crosshairTarget(client, player) : null;
        if (target == null) {
            holdingSprint = false;
            return;
        }
        if (crits.get() && !critReady(client, player)) return;
        if (!charged(player, 0.5f)) return;
        AdinClient.MODULES.get(ShieldBreaker.class).prepare(player, target);
        client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
        holdingSprint = false;
        threshold = roll();
    }

    private boolean critReady(Minecraft client, LocalPlayer player) {
        if (player.onGround()) {
            holdingSprint = false;
            return !client.options.keyJump.isDown() && player.getDeltaMovement().y <= 0.0 && !resumingSprint(player);
        }
        if (!Players.canCrit(player)) {
            if (player.fallDistance <= 0.0 && peaking(player) && charged(player, NEXT_TICK)) dropSprint(player);
            return false;
        }
        if (player.isSprinting()) {
            dropSprint(player);
            return false;
        }
        return true;
    }

    private void dropSprint(LocalPlayer player) {
        holdingSprint = true;
        player.setSprinting(false);
    }

    private boolean charged(LocalPlayer player, float ticksAhead) {
        return player.getAttackStrengthScale(ticksAhead) >= threshold;
    }

    private float roll() {
        return Maths.random(cooldown.low() / 100f, cooldown.high() / 100f);
    }

    private boolean armed(Minecraft client, LocalPlayer player) {
        return Game.playing(client) && !player.isUsingItem() && (!weaponsOnly.get() || Entities.holdsWeapon(player));
    }

    private static boolean peaking(LocalPlayer player) {
        double rise = player.getDeltaMovement().y;
        return rise > 0.0 && (rise - GRAVITY) * DRAG <= 0.0;
    }

    private static boolean resumingSprint(LocalPlayer player) {
        return !player.isSprinting() && Players.isMoving(player);
    }

    private LivingEntity crosshairTarget(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity entity)) return null;
        return targets.accepts(player, entity) ? entity : null;
    }
}
