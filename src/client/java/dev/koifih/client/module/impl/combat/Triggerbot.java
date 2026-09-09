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
    private final RangeSetting cooldown = add(new RangeSetting("cooldown", 60, 100, 0, 100, Measure.FRACTION));
    private final BoolSetting weaponsOnly = add(new BoolSetting("weaponsOnly", true));
    private final TargetSettings targets = add(new TargetSettings());
    private final BoolSetting crits = add(new BoolSetting("crits", true));
    private static final float NEXT_TICK = 1.5f;

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
        boolean held = holdingSprint;
        holdingSprint = false;
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        if (!Game.playing(client) || player.isUsingItem()) return;
        if (weaponsOnly.get() && !Entities.holdsWeapon(player)) return;
        LivingEntity target = crosshairTarget(client, player);
        if (target == null) return;
        float charge = player.getAttackStrengthScale(0.5f);
        if (crits.get() && !player.onGround()) {
            if (!Players.canCrit(player)) return;
            holdingSprint = held;
            if (player.isSprinting()) {
                if (player.getAttackStrengthScale(NEXT_TICK) < threshold) return;
                holdingSprint = true;
                player.setSprinting(false);
                return;
            }
        } else if (held || (crits.get() && (client.options.keyJump.isDown() || launched(player) || waitingForSprint(player)))) {
            return;
        }
        if (charge < threshold) return;
        AdinClient.MODULES.get(ShieldBreaker.class).prepare(player, target);
        client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
        threshold = roll();
        holdingSprint = false;
    }

    private float roll() {
        return Maths.random(cooldown.low() / 100f, cooldown.high() / 100f);
    }

    private static boolean launched(LocalPlayer player) {
        return player.getDeltaMovement().y > 0.0;
    }

    private static boolean waitingForSprint(LocalPlayer player) {
        return !player.isSprinting() && Players.isMoving(player);
    }

    private LivingEntity crosshairTarget(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity entity)) return null;
        return targets.accepts(player, entity) ? entity : null;
    }
}
