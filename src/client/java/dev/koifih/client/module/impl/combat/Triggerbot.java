package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.event.events.PreTickEvent;
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
<<<<<<< HEAD
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
=======
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class Triggerbot extends Module {
    private static final float NEXT_TICK = 1.5f;
    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;
    private static final int FULL_CHARGE = 100;
    private static final int EXTRA_CHARGE = 125;
    private static final float HUNDREDTHS = 100f;

<<<<<<< HEAD
    private final RangeSetting cooldown = add(new RangeSetting("cooldown", 100, 100, 0, EXTRA_CHARGE, Measure.FRACTION));
=======
    private final RangeSetting cooldown = add(new RangeSetting("cooldown", 60, 100, 0, EXTRA_CHARGE, Measure.FRACTION));
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    private final BoolSetting extraDelay = add(new BoolSetting("extraDelay", false));
    private final BoolSetting range = add(new BoolSetting("range", false));
    private final RangeSetting blocks = add(new RangeSetting("blocks", 300, 300, 100, 300, Measure.FRACTION));
    private final BoolSetting weaponsOnly = add(new BoolSetting("weaponsOnly", true));
    private final TargetSettings targets = add(new TargetSettings());
    private final BoolSetting crits = add(new BoolSetting("crits", true));

    private float threshold;
    private float reach;
    private int overcharge;
    private boolean holdingSprint;
    private LivingEntity current;

    public Triggerbot() {
        super("triggerbot");
        cooldown.maxWhen(() -> extraDelay.get() ? EXTRA_CHARGE : FULL_CHARGE);
        extraDelay.describe();
        blocks.visibleWhen(range::get);
    }

    @Override
    public String info() {
        return cooldown.format(cooldown.low()) + " - " + cooldown.format(cooldown.high());
    }

    public boolean holdsSprint() {
        return isEnabled() && holdingSprint;
    }

    public LivingEntity target() {
        return isEnabled() ? current : null;
    }

    @Override
    protected void onEnable() {
        rearm();
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        holdingSprint = false;
        current = null;
    }

    private void onTick(PreTickEvent event) {
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        if (player == null) {
            current = null;
            overcharge = 0;
            holdingSprint = false;
            return;
        }
        LivingEntity target = armed(client, player) ? crosshairTarget(client, player) : null;
        current = target;
        overcharge = player.getAttackStrengthScale(0f) >= 1f ? overcharge + 1 : 0;
        if (target == null) {
            holdingSprint = false;
            return;
        }
<<<<<<< HEAD
        if (target.hurtTime > 0) return;
        AdinClient.MODULES.get(ShieldBreaker.class).prepare(player, target);
        if (crits.get() && !critReady(client, player)) return;
        if (!charged(player, 0f)) return;
        if (Clicks.simulate) {
            while (client.options.keyAttack.consumeClick()) {}
            if (!Clicks.left(client, target)) {
                client.gameMode.attack(player, target);
                player.resetAttackStrengthTicker();
                player.swing(InteractionHand.MAIN_HAND);
            }
        } else {
            client.gameMode.attack(player, target);
            player.resetAttackStrengthTicker();
            player.swing(InteractionHand.MAIN_HAND);
        }
=======
        if (crits.get() && !critReady(client, player)) return;
        if (!charged(player, 0.5f)) return;
        AdinClient.MODULES.get(ShieldBreaker.class).prepare(player, target);
        if (!Clicks.left(client, target)) client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
        holdingSprint = false;
        rearm();
    }

    private boolean critReady(Minecraft client, LocalPlayer player) {
        if (player.onGround()) {
            holdingSprint = false;
<<<<<<< HEAD
            if (client.options.keyJump.isDown()) return false;
            if (player.isSprinting()) {
                dropSprint(player);
                return false;
            }
            return player.getDeltaMovement().y <= 0.0;
=======
            return !client.options.keyJump.isDown() && player.getDeltaMovement().y <= 0.0;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
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
<<<<<<< HEAD
        if (player.isSprinting()) {
            player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
=======
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
        player.setSprinting(false);
    }

    private boolean charged(LocalPlayer player, float ticksAhead) {
<<<<<<< HEAD
        float scale = player.getAttackStrengthScale(ticksAhead);
        if (scale < Math.min(threshold, 1f)) return false;
        if (scale < 0.95f) return false;
=======
        if (player.getAttackStrengthScale(ticksAhead) < Math.min(threshold, 1f)) return false;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
        return overcharge >= Math.max(0f, threshold - 1f) * player.getCurrentItemAttackStrengthDelay();
    }

    private void rearm() {
<<<<<<< HEAD
        threshold = Math.max(0.95f, Maths.random(cooldown.low() / HUNDREDTHS, cooldown.high() / HUNDREDTHS));
        if (crits.get() && threshold < 1f) threshold = 1f;
=======
        threshold = Maths.random(cooldown.low() / HUNDREDTHS, cooldown.high() / HUNDREDTHS);
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
        reach = Maths.random(blocks.low() / HUNDREDTHS, blocks.high() / HUNDREDTHS);
        overcharge = 0;
    }

    private boolean armed(Minecraft client, LocalPlayer player) {
        return Game.playing(client) && !player.isUsingItem() && (!weaponsOnly.get() || Entities.holdsWeapon(player));
    }

    private static boolean peaking(LocalPlayer player) {
        double rise = player.getDeltaMovement().y;
        return rise > 0.0 && (rise - GRAVITY) * DRAG <= 0.0;
    }


    private LivingEntity crosshairTarget(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity entity)) return null;
        return targets.accepts(player, entity) && withinReach(player, hit.getLocation()) ? entity : null;
    }

    private boolean withinReach(LocalPlayer player, Vec3 hit) {
        return !range.get() || hit.distanceToSqr(player.getEyePosition()) <= reach * reach;
    }
}
