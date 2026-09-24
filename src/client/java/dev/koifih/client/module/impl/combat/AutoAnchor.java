package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.accessor.MultiPlayerGameModeAccessor;
import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import java.util.function.Predicate;

public final class AutoAnchor extends Module {
    private static final long PLACE_WAIT = 500;
    private static final double DIAGONAL = 0.4;
    private static final RotationConfig SNAP = RotationConfig.silent(0f, Smoothing.EASE_OUT_CUBIC);

    private final SliderSetting delay = add(new SliderSetting("delay", 100, 50, 500, Measure.MILLIS));
    private final BoolSetting safe = add(new BoolSetting("safe", false));
    private final BoolSetting silentSwap = add(new BoolSetting("silentSwap", false));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Ticker pacer = new Time.Ticker();
    private final Time.Stopwatch sincePlace = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;
    private boolean silent;
    private BlockPos anchor;
    private BlockPos charged;
    private BlockPos shield;
    private boolean spent;

    public AutoAnchor() {
        super("autoAnchor");
        swapBack.visibleWhen(() -> !silentSwap.get());
    }

    @Override
    public boolean activatable() {
        return true;
    }

    @Override
    protected void onEnable() {
        spent = false;
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        idle(Game.player());
    }

    @Override
    protected void onRelease() {
        idle(mc.player);
    }

    private void onTick(PreTickEvent event) {
        LocalPlayer player = event.client().player;
        if (player == null) return;
        if (bindKey() != InputConstants.UNKNOWN && !isBindDown()) {
            if (shield != null || anchor != null) idle(player);
            return;
        }
        if (!Game.playing(mc) || Players.consuming(player)) {
            if (shield == null && anchor == null) idle(player);
            return;
        }
        if (anchor == null) look(player);
        if (shield != null && shield(player)) return;
        if (anchor != null) work(player);
    }

    private void look(LocalPlayer player) {
        if (!pacer.ready()) return;
        BlockHitResult hit = mc.hitResult instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK ? block : null;
        if (hit == null) {
            idle(player);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.getBlock() instanceof RespawnAnchorBlock) {
            anchor = pos;
            return;
        }
        BlockPos target = state.canBeReplaced() ? pos : pos.relative(hit.getDirection());
        int slot = nearest(player, stack -> stack.is(Items.RESPAWN_ANCHOR));
        if (spent || slot == Hotbar.NONE || !placeable(player, player.getInventory().getItem(slot), hit, Blocks.RESPAWN_ANCHOR)) {
            idle(player);
        } else if (act(player, hit, stack -> stack.is(Items.RESPAWN_ANCHOR), true)) {
            anchor = target;
            spent = true;
            sincePlace.reset();
            if (safe.get()) shield = target.offset(towardPlayer(player, target));
        }
    }

    private static Vec3i towardPlayer(LocalPlayer player, BlockPos anchor) {
        Vec3 offset = player.getEyePosition().subtract(Vec3.atCenterOf(anchor));
        double x = Math.abs(offset.x);
        double z = Math.abs(offset.z);
        boolean diagonal = Math.min(x, z) > DIAGONAL * Math.max(x, z);
        int stepX = diagonal || x >= z ? (int) Math.signum(offset.x) : 0;
        int stepZ = diagonal || z > x ? (int) Math.signum(offset.z) : 0;
        return new Vec3i(stepX, 0, stepZ);
    }

    private boolean shield(LocalPlayer player) {
        BlockPos ground = shield.below();
        if (sincePlace.elapsed(PLACE_WAIT) || !mc.level.getBlockState(shield).canBeReplaced() || mc.level.getBlockState(ground).canBeReplaced()) {
            shield = null;
            return false;
        }
        BlockHitResult hit = Placement.placeInto(mc.level, player.getEyePosition(), shield, anchor);
        int slot = nearest(player, stack -> stack.is(Items.GLOWSTONE));
        if (hit == null || slot == Hotbar.NONE || !placeable(player, player.getInventory().getItem(slot), hit, Blocks.GLOWSTONE)
                || (facing(player, hit) && use(player, hit, stack -> stack.is(Items.GLOWSTONE), true))) {
            shield = null;
            return false;
        }
        aim(player, hit);
        return true;
    }

    private void work(LocalPlayer player) {
        BlockState state = mc.level.getBlockState(anchor);
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
            if (sincePlace.elapsed(PLACE_WAIT)) idle(player);
            return;
        }
        BlockHitResult hit = Placement.clickOn(mc.level, player.getEyePosition(), anchor);
        if (hit == null) return;
        aim(player, hit);
        if (!pacer.ready() || !facing(player, hit)) return;
        if (state.getValue(RespawnAnchorBlock.CHARGE) == 0 && !anchor.equals(charged)) {
            if (act(player, hit, stack -> stack.is(Items.GLOWSTONE), false)) charged = anchor;
        } else if (mc.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, anchor)) {
            idle(player);
        } else {
            act(player, hit, detonator(player), false);
        }
    }

    private void aim(LocalPlayer player, BlockHitResult hit) {
        Vec3 point = hit.getLocation();
        AdinClient.ROTATIONS.aim(partialTick -> Rotation.toward(player.getEyePosition(partialTick), point), Priority.HIGH, SNAP);
    }

    private boolean facing(LocalPlayer player, BlockHitResult hit) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = AdinClient.ROTATIONS.rotation(player).direction();
        return Placement.looksAt(mc.level, hit.getBlockPos(), eye, look, player.blockInteractionRange());
    }

    private void idle(LocalPlayer player) {
        anchor = null;
        charged = null;
        shield = null;
        restore(player);
    }

    private static boolean placeable(LocalPlayer player, ItemStack stack, BlockHitResult hit, Block block) {
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        if (!context.canPlace()) return false;
        BlockState state = block.getStateForPlacement(context);
        return state != null && state.canSurvive(mc.level, context.getClickedPos())
                && mc.level.isUnobstructed(state, context.getClickedPos(), CollisionContext.of(player));
    }

    private static Predicate<ItemStack> detonator(LocalPlayer player) {
        if (has(player, stack -> stack.is(Items.TOTEM_OF_UNDYING))) return stack -> stack.is(Items.TOTEM_OF_UNDYING);
        if (has(player, stack -> stack.has(DataComponents.WEAPON))) return stack -> stack.has(DataComponents.WEAPON);
        return stack -> !stack.isEmpty() && !stack.is(Items.GLOWSTONE);
    }

    private static boolean has(LocalPlayer player, Predicate<ItemStack> matcher) {
        return nearest(player, matcher) != Hotbar.NONE;
    }

    private static int nearest(LocalPlayer player, Predicate<ItemStack> matcher) {
        Inventory inventory = player.getInventory();
        int selected = Hotbar.selected(player);
        for (int distance = 0; distance < Inventory.SELECTION_SIZE; distance++) {
            for (int slot : new int[] {selected - distance, selected + distance}) {
                if (Inventory.isHotbarSlot(slot) && matcher.test(inventory.getItem(slot))) return slot;
            }
        }
        return Hotbar.NONE;
    }

    private boolean act(LocalPlayer player, BlockHitResult hit, Predicate<ItemStack> matcher, boolean placing) {
        if (!use(player, hit, matcher, placing)) return false;
        pacer.pace(delay.get());
        return true;
    }

    private boolean use(LocalPlayer player, BlockHitResult hit, Predicate<ItemStack> matcher, boolean placing) {
        int slot = nearest(player, matcher);
        if (slot == Hotbar.NONE || !Placement.ready()) return false;
        int selected = Hotbar.selected(player);
        boolean quiet = originalSlot != Hotbar.NONE ? silent : silentSwap.get();
        if (slot != (quiet ? Hotbar.serverSlot() : selected)) {
            if (originalSlot == Hotbar.NONE) {
                originalSlot = selected;
                silent = quiet;
            }
            if (!Hotbar.swap(player, slot, quiet)) return false;
        }
        if (quiet && slot != selected) {
            ItemStack stack = player.getInventory().getItem(slot);
            ((MultiPlayerGameModeAccessor) mc.gameMode).adin$startPrediction(mc.level, sequence -> {
                if (placing) Placement.predict(player, stack, hit);
                return new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, sequence);
            });
        } else if (!Clicks.right(mc, hit)) {
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        }
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    private void restore(LocalPlayer player) {
        if (player != null && originalSlot != Hotbar.NONE) {
            boolean done = silent ? Hotbar.resync(player) : !swapBack.get() || Hotbar.swap(player, originalSlot, false);
            if (!done) return;
        }
        originalSlot = Hotbar.NONE;
        silent = false;
    }
}
