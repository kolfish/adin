package dev.koifih.client.module.impl.combat;

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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoHitCrystal extends Module {
    private final SliderSetting delay = add(new SliderSetting("delay", 100, 1, 500));
    private final BoolSetting silentSwap = add(new BoolSetting("silentSwap", false));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Stopwatch sinceSwap = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;
    private boolean silent;
    private boolean placed;

    public AutoHitCrystal() {
        super("autoHitCrystal", Category.COMBAT);
        swapBack.visibleWhen(() -> !silentSwap.get());
    }

    @Override
    public boolean activatable() {
        return true;
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        restore(Game.player());
    }

    @Override
    protected void onActivate() {
        LocalPlayer player = mc.player;
        if (!Game.playing(mc) || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        int selected = Hotbar.selected(player);
        int slot = player.getMainHandItem().is(Items.OBSIDIAN) ? selected : Hotbar.find(player, stack -> stack.is(Items.OBSIDIAN));
        if (slot == Hotbar.NONE) return;
        if (slot != selected) {
            if (originalSlot == Hotbar.NONE) originalSlot = selected;
            silent = silentSwap.get();
            if (!Hotbar.swap(player, slot, silent)) {
                originalSlot = Hotbar.NONE;
                return;
            }
            sinceSwap.reset();
        }
        if (silent && slot != selected) {
            ((MultiPlayerGameModeAccessor) mc.gameMode).adin$startPrediction(mc.level,
                    sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, sequence));
            BlockPos pos = mc.level.getBlockState(hit.getBlockPos()).canBeReplaced() ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
            SoundType sound = Blocks.OBSIDIAN.defaultBlockState().getSoundType();
            mc.level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1f) / 2f, sound.getPitch() * 0.8f);
        } else if (!mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            return;
        }
        player.swing(InteractionHand.MAIN_HAND);
        placed = true;
    }

    private void onTick(PreTickEvent event) {
        boolean justPlaced = placed;
        placed = false;
        if (originalSlot == Hotbar.NONE || justPlaced) return;
        LocalPlayer player = event.client().player;
        if (player == null || (!silent && !swapBack.get())) {
            originalSlot = Hotbar.NONE;
        } else if (sinceSwap.elapsed(delay.get())) {
            restore(player);
        }
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
