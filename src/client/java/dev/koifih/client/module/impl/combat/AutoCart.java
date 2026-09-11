package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.accessor.MultiPlayerGameModeAccessor;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.rotation.Rotation;
import dev.koifih.client.rotation.RotationConfig;
import dev.koifih.client.rotation.Smoothing;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Placement;
import dev.koifih.client.util.ProjectilePrediction;
import dev.koifih.client.util.Time;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import java.util.HashSet;
import java.util.Set;

public final class AutoCart extends Module {
    private static final String[] MODES = {"Normal", "Silent"};
    private static final int SILENT = 1;
    private static final int DROP_SEARCH = 8;
    private static final double RAIL_HEIGHT = 0.0625;
    private static final double CART_CLEARANCE = 1.0;
    private static final double ARROW_SEARCH = 8.0;
    private static final double MOVING = 0.01;

    private enum Stage { IDLE, TRACK, RAIL, CART }

    private final EnumSetting mode = add(new EnumSetting("mode", 0, MODES));
    private final SliderSetting smoothness = add(new SliderSetting("smoothness", 50, 0, 100, Measure.PERCENT));
    private final SliderSetting delay = add(new SliderSetting("delay", 100, 1, 500));
    private final BoolSetting silentSwap = add(new BoolSetting("silentSwap", false));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Stopwatch sinceAction = new Time.Stopwatch();
    private Stage stage = Stage.IDLE;
    private BlockPos ground;
    private AbstractArrow tracked;
    private final Set<Integer> seenArrows = new HashSet<>();
    private int originalSlot = Hotbar.NONE;
    private boolean silent;

    public AutoCart() {
        super("autoCart", Category.COMBAT);
        swapBack.visibleWhen(() -> !silentSwap.get());
    }

    @Override
    public String info() {
        return mode.selected();
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        stop(Game.player());
    }

    private AbstractArrow firedArrow(LocalPlayer player) {
        AbstractArrow fired = null;
        for (AbstractArrow arrow : mc.level.getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(ARROW_SEARCH))) {
            if (arrow.getOwner() != player || arrow.getDeltaMovement().lengthSqr() < MOVING || !seenArrows.add(arrow.getId())) continue;
            fired = arrow;
        }
        return fired;
    }

    private boolean flying(AbstractArrow arrow) {
        return arrow != null && !arrow.isRemoved() && arrow.getDeltaMovement().lengthSqr() >= MOVING;
    }

    private void track(LocalPlayer player) {
        if (!flying(tracked)) {
            stop(player);
            return;
        }
        BlockHitResult landing = ProjectilePrediction.arrow(player, tracked);
        if (landing == null) return;
        BlockPos found = ground(landing);
        if (found == null || !Vec3.atCenterOf(found.above()).closerThan(player.getEyePosition(), player.blockInteractionRange())) return;
        if (!mc.level.getEntitiesOfClass(MinecartTNT.class, new AABB(found.above()).inflate(CART_CLEARANCE)).isEmpty()) {
            stop(player);
            return;
        }
        ground = found;
        stage = Stage.RAIL;
    }

    private void onTick(PreTickEvent event) {
        LocalPlayer player = event.client().player;
        if (player == null) {
            stage = Stage.IDLE;
            return;
        }
        if (stage == Stage.IDLE) {
            AbstractArrow arrow = Game.playing(mc) && holdsFlameBow(player) ? firedArrow(player) : null;
            if (!sinceAction.elapsed(delay.get())) return;
            if (originalSlot != Hotbar.NONE) restore(player);
            if (arrow != null) {
                tracked = arrow;
                stage = Stage.TRACK;
            }
            return;
        }
        if (!Game.playing(mc) || !flying(tracked)) {
            stop(player);
            return;
        }
        if (stage == Stage.TRACK) {
            track(player);
            return;
        }
        BlockPos rail = ground.above();
        BlockState state = mc.level.getBlockState(rail);
        if (stage == Stage.RAIL) {
            if (state.is(BlockTags.RAILS)) {
                stage = Stage.CART;
            } else {
                Vec3 point = Vec3.atBottomCenterOf(rail);
                if (!aimed(player, point, ground)) return;
                BlockHitResult hit = new BlockHitResult(point, Direction.UP, ground, false);
                int slot = Hotbar.find(player, stack -> stack.is(Items.RAIL));
                if (slot == Hotbar.NONE || !placeable(player, player.getInventory().getItem(slot), hit)) {
                    stop(player);
                    return;
                }
                if (use(player, hit, Items.RAIL, true)) stage = Stage.CART;
                return;
            }
        }
        if (!state.is(BlockTags.RAILS)) {
            if (sinceAction.elapsed(delay.get() * 4L)) stop(player);
            return;
        }
        Vec3 point = Vec3.atBottomCenterOf(rail).add(0.0, RAIL_HEIGHT, 0.0);
        if (!aimed(player, point, rail)) return;
        if (use(player, new BlockHitResult(point, Direction.UP, rail, false), Items.TNT_MINECART, false)) stage = Stage.IDLE;
    }

    private boolean aimed(LocalPlayer player, Vec3 point, BlockPos block) {
        Smoothing smoothing = Smoothing.EASE_OUT_CUBIC;
        RotationConfig config = mode.get() == SILENT
                ? RotationConfig.silent(smoothness.get() / 100f, smoothing)
                : RotationConfig.visible(smoothness.get() / 100f, smoothing);
        AdinClient.ROTATIONS.aim(partialTick -> Rotation.toward(player.getEyePosition(partialTick), point), Priority.HIGH, config);
        if (!sinceAction.elapsed(delay.get())) return false;
        Vec3 eye = player.getEyePosition();
        Vec3 look = AdinClient.ROTATIONS.rotation(player).direction();
        return new AABB(block).clip(eye, eye.add(look.scale(player.blockInteractionRange()))).isPresent();
    }

    private BlockPos ground(BlockHitResult landing) {
        BlockPos start = landing.getDirection() == Direction.UP
                ? landing.getBlockPos()
                : landing.getBlockPos().relative(landing.getDirection()).below();
        for (int drop = 0; drop < DROP_SEARCH; drop++) {
            BlockPos candidate = start.below(drop);
            BlockState above = mc.level.getBlockState(candidate.above());
            if (!above.canBeReplaced() && !above.is(BlockTags.RAILS)) continue;
            if (mc.level.getBlockState(candidate).isFaceSturdy(mc.level, candidate, Direction.UP)) return candidate;
        }
        return null;
    }

    private static boolean placeable(LocalPlayer player, ItemStack stack, BlockHitResult hit) {
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        if (!context.canPlace()) return false;
        BlockState state = Blocks.RAIL.getStateForPlacement(context);
        return state != null && state.canSurvive(mc.level, context.getClickedPos())
                && mc.level.isUnobstructed(state, context.getClickedPos(), CollisionContext.of(player));
    }

    private static boolean holdsFlameBow(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(Items.BOW)) return false;
        for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
            if (enchantment.is(Enchantments.FLAME)) return true;
        }
        return false;
    }

    private boolean use(LocalPlayer player, BlockHitResult hit, Item item, boolean placing) {
        int slot = Hotbar.find(player, stack -> stack.is(item));
        if (slot == Hotbar.NONE) {
            stop(player);
            return false;
        }
        if (!Placement.ready()) return false;
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
        } else {
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        }
        player.swing(InteractionHand.MAIN_HAND);
        sinceAction.reset();
        return true;
    }

    private void stop(LocalPlayer player) {
        stage = Stage.IDLE;
        tracked = null;
        seenArrows.clear();
        restore(player);
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
