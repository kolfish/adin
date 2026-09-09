package dev.koifih.client.module.impl.combat;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.MultiPlayerGameModeAccessor;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Time;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoCrystal extends Module {
    private static final float DUPLICATE_ROTATION = 2f;
    private static final float DUPLICATE_TOLERANCE = 0.0001f;

    private final SliderSetting delay = add(new SliderSetting("delay", 50, 1, 500));
    private final BoolSetting silentSwap = add(new BoolSetting("silentSwap", false));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Stopwatch sinceAction = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;
    private boolean silent;
    private boolean releasing;
    private float lastPlacedDelta = -1f;

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
        if (!Game.playing(mc) || !sinceAction.elapsed(delay.get())) return;
        releasing = false;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof EndCrystal crystal) {
            mc.gameMode.attack(player, crystal);
            player.swing(InteractionHand.MAIN_HAND);
            sinceAction.reset();
        } else if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK && placeable(hit.getBlockPos())) {
            place(player, hit);
        }
    }

    @Override
    protected void onRelease() {
        releasing = true;
    }

    public boolean holding() {
        return originalSlot != Hotbar.NONE;
    }

    private boolean placeable(BlockPos pos) {
        BlockState base = mc.level.getBlockState(pos);
        if (!base.is(Blocks.OBSIDIAN) && !base.is(Blocks.BEDROCK)) return false;
        BlockPos above = pos.above();
        if (!mc.level.isEmptyBlock(above)) return false;
        return mc.level.getEntities(null, new AABB(above.getX(), above.getY(), above.getZ(), above.getX() + 1.0, above.getY() + 2.0, above.getZ() + 1.0)).isEmpty();
    }

    private void place(LocalPlayer player, BlockHitResult hit) {
        float delta = AdinClient.ROTATIONS.sentYawDelta();
        if (delta > DUPLICATE_ROTATION && Math.abs(delta - lastPlacedDelta) < DUPLICATE_TOLERANCE) return;
        int selected = Hotbar.selected(player);
        int slot = player.getMainHandItem().is(Items.END_CRYSTAL) ? selected : Hotbar.find(player, stack -> stack.is(Items.END_CRYSTAL));
        if (slot == Hotbar.NONE) return;
        if (originalSlot == Hotbar.NONE) {
            int handedOff = AdinClient.MODULES.get(AutoHitCrystal.class).holding();
            originalSlot = handedOff != Hotbar.NONE ? handedOff : selected;
            silent = silentSwap.get();
        }
        if (slot != selected && !Hotbar.swap(player, slot, silent)) return;
        if (silent && slot != selected) {
            ((MultiPlayerGameModeAccessor) mc.gameMode).adin$startPrediction(mc.level,
                    sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, sequence));
        } else if (!mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            return;
        }
        player.swing(InteractionHand.MAIN_HAND);
        sinceAction.reset();
        lastPlacedDelta = delta;
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
