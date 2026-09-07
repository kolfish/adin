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
import dev.koifih.client.setting.EntitySetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class AimAssist extends Module {
    private static final String[] TARGETS = {"Players", "Invisible", "Entities"};
    private static final int PLAYERS = 0;
    private static final int INVISIBLE = 1;
    private static final int ENTITIES = 2;
    private static final double RANGE = 6.0;

    private record Aim(LocalPlayer player, LivingEntity entity, Bone bone) implements RotationTarget {
        @Override
        public Rotation at(float partialTick) {
            return Rotation.toward(player.getEyePosition(partialTick), bone.center(entity, partialTick));
        }
    }

    private final SliderSetting fov = add(new SliderSetting("fov", 80, 10, 180, Measure.DEGREES));
    private final EnumSetting mode = add(new EnumSetting("mode", 0, Smoothing.NAMES));
    private final SliderSetting smoothness = add(new SliderSetting("smoothness", 50, 0, 100, Measure.PERCENT));
    private final BoolSetting onHold = add(new BoolSetting("onHold", false));
    private final BoolSetting weaponsOnly = add(new BoolSetting("weaponsOnly", false));
    private final MultiSetting targets = add(new MultiSetting("targets", TARGETS, PLAYERS));
    private final EntitySetting entities = add(new EntitySetting("entities"));
    private final MultiSetting bones = add(new MultiSetting("bones", Bone.NAMES, Bone.HEAD.ordinal()));

    public AimAssist() {
        super("aimAssist", Category.COMBAT);
        entities.visibleWhen(() -> targets.has(ENTITIES));
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
        if (player == null || client.gui.screen() != null || !client.mouseHandler.isMouseGrabbed()) return null;
        if (weaponsOnly.get() && !player.getMainHandItem().has(DataComponents.WEAPON)) return null;
        return aim(player);
    }

    private Aim aim(LocalPlayer player) {
        Aim closest = null;
        double closestAngle = fov.get() * 0.5;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE))) {
            if (!targeted(player, entity)) continue;
            for (Bone bone : Bone.ALL) {
                if (!bones.has(bone.ordinal())) continue;
                double angle = angle(player, bone.center(entity, 1f));
                if (angle >= closestAngle) continue;
                closest = new Aim(player, entity, bone);
                closestAngle = angle;
            }
        }
        return closest;
    }

    private boolean targeted(LocalPlayer player, LivingEntity entity) {
        if (entity == player || !entity.isAlive() || entity.isSpectator()) return false;
        if (entity.isInvisible() && !targets.has(INVISIBLE)) return false;
        if (player.distanceToSqr(entity) > RANGE * RANGE || !player.hasLineOfSight(entity)) return false;
        if (entity instanceof Player) return targets.has(PLAYERS);
        return targets.has(ENTITIES) && entities.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    private static double angle(LocalPlayer player, Vec3 point) {
        Vec3 direction = point.subtract(player.getEyePosition()).normalize();
        return Math.toDegrees(Math.acos(Mth.clamp(player.getLookAngle().dot(direction), -1.0, 1.0)));
    }
}
