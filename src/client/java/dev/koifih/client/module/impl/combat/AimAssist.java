package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.TickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.rotation.Bone;
import dev.koifih.client.rotation.Rotation;
import dev.koifih.client.rotation.RotationConfig;
import dev.koifih.client.rotation.RotationTarget;
import dev.koifih.client.rotation.Smoothing;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.setting.TargetSettings;
import dev.koifih.client.util.Entities;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class AimAssist extends Module {
    private static final String[] PRIORITIES = {"Closest to FOV", "Lowest health"};
    private static final int LOWEST_HEALTH = 1;
    private static final double RANGE = 6.0;

    private record Aim(LocalPlayer player, LivingEntity entity, Bone bone) implements RotationTarget {
        @Override
        public Rotation at(float partialTick) {
            return Rotation.toward(player.getEyePosition(partialTick), bone.point(player, entity, partialTick));
        }
    }

    private final SliderSetting fov = add(new SliderSetting("fov", 80, 10, 180, Measure.DEGREES));
    private final EnumSetting mode = add(new EnumSetting("mode", 0, Smoothing.NAMES));
    private final SliderSetting smoothness = add(new SliderSetting("smoothness", 50, 0, 100, Measure.PERCENT));
    private final BoolSetting onHold = add(new BoolSetting("onHold", false));
    private final BoolSetting weaponsOnly = add(new BoolSetting("weaponsOnly", false));
    private final EnumSetting target = add(new EnumSetting("target", 0, PRIORITIES));
    private final TargetSettings targets = add(new TargetSettings());
    private final MultiSetting bones = add(new MultiSetting("bones", Bone.NAMES, Bone.HEAD.ordinal()));

    public AimAssist() {
        super("aimAssist", Category.COMBAT);
        bones.optionVisibleWhen(option -> option == Bone.MULTIPOINT.ordinal() || !bones.get().contains(Bone.MULTIPOINT.ordinal()));
    }

    public boolean blocksBreaking() {
        return isEnabled() && onHold.get();
    }

    @Override
    protected void onEnable() {
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        if (onHold.get() && !event.client().options.keyAttack.isDown()) return;
        Aim aim = aim(event.client());
        if (aim == null) return;
        RotationConfig config = RotationConfig.visible(smoothness.get() / 100f, Smoothing.values()[mode.get()]);
        AdinClient.ROTATIONS.aim(aim, Priority.NORMAL, config);
    }

    private Aim aim(Minecraft client) {
        LocalPlayer player = client.player;
        if (!Game.playing(client)) return null;
        if (weaponsOnly.get() && !Entities.holdsWeapon(player)) return null;
        Aim best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE))) {
            if (!targeted(player, entity)) continue;
            Bone closest = null;
            double closestAngle = fov.get() * 0.5;
            for (Bone bone : Bone.ALL) {
                if (!bones.has(bone.ordinal())) continue;
                double angle = MathUtil.angle(player.getLookAngle(), bone.point(player, entity, 1f).subtract(player.getEyePosition()));
                if (angle >= closestAngle) continue;
                closest = bone;
                closestAngle = angle;
            }
            if (closest == null) continue;
            double score = target.get() == LOWEST_HEALTH ? entity.getHealth() : closestAngle;
            if (score >= bestScore) continue;
            best = new Aim(player, entity, closest);
            bestScore = score;
        }
        return best;
    }

    private boolean targeted(LocalPlayer player, LivingEntity entity) {
        return targets.accepts(player, entity) && player.distanceToSqr(entity) <= RANGE * RANGE && player.hasLineOfSight(entity);
    }
}
