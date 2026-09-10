package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.MultiPlayerGameModeAccessor;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.rotation.Rotation;
import dev.koifih.client.rotation.RotationConfig;
import dev.koifih.client.rotation.Smoothing;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Placement;
import dev.koifih.client.util.Players;
import dev.koifih.client.util.Time;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class AutoCrystal extends Module {
    private static final long SPAWN_WAIT = 250;
    private static final float TICK_MILLIS = 50f;
    private static final RotationConfig SNAP = RotationConfig.silent(0f, Smoothing.EASE_OUT_CUBIC);

    private final SliderSetting delay = add(new SliderSetting("delay", 50, 1, 500));
    private final BoolSetting headBob = add(new BoolSetting("headBob", false));
    private final BoolSetting silentSwap = add(new BoolSetting("silentSwap", false));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Stopwatch sinceAction = new Time.Stopwatch();
    private final Time.Stopwatch sincePlace = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;
    private boolean silent;
    private boolean releasing;
    private EndCrystal hit;
    private int ticks;
    private int lastActionTick;
    private boolean awaitingSpawn;

    public AutoCrystal() {
        super("autoCrystal", Category.COMBAT);
        swapBack.visibleWhen(() -> !silentSwap.get());
    }

    @Override
    public boolean activatable() {
        return true;
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, event -> {
            ticks++;
            if (releasing) restore(event.client().player);
        });
    }

    @Override
    protected void onDisable() {
        releasing = true;
        restore(Game.player());
    }

    @Override
    protected void onHold() {
        LocalPlayer player = mc.player;
        if (!Game.playing(mc) || Players.consuming(player)) return;
        releasing = false;
        if (headBob.get()) {
            bob(player);
            return;
        }
        if (!sinceAction.elapsed(delay.get())) return;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof EndCrystal crystal) {
            attack(player, crystal);
        } else if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK && placeable(hit.getBlockPos())) {
            place(player, hit);
        }
    }

    @Override
    protected void onRelease() {
        releasing = true;
        awaitingSpawn = false;
        hit = null;
    }

    private void bob(LocalPlayer player) {
        BlockPos base = crosshairBase(player);
        if (base == null) return;
        Vec3 eye = player.getEyePosition();
        Vec3 look = AdinClient.ROTATIONS.rotation(player).direction();
        BlockPos above = base.above();
        Vec3 top = Vec3.atBottomCenterOf(above);
        Vec3 center = top.add(0.0, 1.0, 0.0);
        EndCrystal crystal = crystalAbove(base);
        boolean ready = ticks - lastActionTick >= Math.max(1, Math.round(delay.get() / TICK_MILLIS));
        Vec3 next;
        if (crystal != null) {
            awaitingSpawn = false;
            next = crystal.getBoundingBox().getCenter();
            if (ready && hits(crystal.getBoundingBox(), eye, look, player.entityInteractionRange())) {
                attack(player, crystal);
                hit = crystal;
                lastActionTick = ticks;
                next = top;
            }
        } else if (mc.level.isEmptyBlock(above) && (!awaitingSpawn || sincePlace.elapsed(SPAWN_WAIT))) {
            next = top;
            if (ready && hits(new AABB(base), eye, look, player.blockInteractionRange()) && place(player, new BlockHitResult(top, Direction.UP, base, false))) {
                sincePlace.reset();
                lastActionTick = ticks;
                awaitingSpawn = true;
                next = center;
            }
        } else {
            next = center;
        }
        Vec3 target = next;
        AdinClient.ROTATIONS.aim(partialTick -> Rotation.toward(player.getEyePosition(partialTick), target), Priority.HIGH, SNAP);
    }

    private BlockPos crosshairBase(LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        double range = player.blockInteractionRange();
        Vec3 end = eye.add(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(range));
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        boolean blocked = hit.getType() == HitResult.Type.BLOCK;
        if (blocked && isBase(hit.getBlockPos())) return hit.getBlockPos();
        Vec3 reach = blocked ? hit.getLocation() : end;
        for (EndCrystal crystal : mc.level.getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(range))) {
            BlockPos base = crystal.blockPosition().below();
            if (isBase(base) && crystal.getBoundingBox().clip(eye, reach).isPresent()) return base;
        }
        return null;
    }

    private EndCrystal crystalAbove(BlockPos base) {
        for (EndCrystal crystal : mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(base.above()))) {
            if (crystal != hit) return crystal;
        }
        return null;
    }

    private static boolean hits(AABB box, Vec3 eye, Vec3 look, double range) {
        return box.clip(eye, eye.add(look.scale(range))).isPresent();
    }

    private void attack(LocalPlayer player, EndCrystal crystal) {
        mc.gameMode.attack(player, crystal);
        player.swing(InteractionHand.MAIN_HAND);
        sinceAction.reset();
    }

    public boolean holding() {
        return originalSlot != Hotbar.NONE;
    }

    private boolean isBase(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK);
    }

    private boolean placeable(BlockPos pos) {
        if (!isBase(pos)) return false;
        BlockPos above = pos.above();
        if (!mc.level.isEmptyBlock(above)) return false;
        return mc.level.getEntities(null, new AABB(above.getX(), above.getY(), above.getZ(), above.getX() + 1.0, above.getY() + 2.0, above.getZ() + 1.0)).isEmpty();
    }

    private boolean place(LocalPlayer player, BlockHitResult hit) {
        if (!Placement.ready()) return false;
        int selected = Hotbar.selected(player);
        int slot = player.getMainHandItem().is(Items.END_CRYSTAL) ? selected : Hotbar.find(player, stack -> stack.is(Items.END_CRYSTAL));
        if (slot == Hotbar.NONE) return false;
        if (originalSlot == Hotbar.NONE) {
            int handedOff = AdinClient.MODULES.get(AutoHitCrystal.class).holding();
            originalSlot = handedOff != Hotbar.NONE ? handedOff : selected;
            silent = silentSwap.get();
        }
        if (slot != (silent ? Hotbar.serverSlot() : selected) && !Hotbar.swap(player, slot, silent)) return false;
        if (silent && slot != selected) {
            ((MultiPlayerGameModeAccessor) mc.gameMode).adin$startPrediction(mc.level,
                    sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, sequence));
        } else {
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        }
        player.swing(InteractionHand.MAIN_HAND);
        sinceAction.reset();
        return true;
    }

    private void restore(LocalPlayer player) {
        if (player != null && originalSlot != Hotbar.NONE) {
            boolean done = silent ? Hotbar.resync(player) : !swapBack.get() || Hotbar.swap(player, originalSlot, false);
            if (!done) return;
        }
        originalSlot = Hotbar.NONE;
        silent = false;
        releasing = false;
    }
}
