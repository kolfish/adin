package dev.koifih.client.module.impl.player;

import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Time;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public final class KeyPearl extends Module {
    private final SliderSetting delay = add(new SliderSetting("delay", 100, 1, 500));
    private final BoolSetting swapBack = add(new BoolSetting("swapBack", true));
    private final Time.Stopwatch sinceSwap = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;
    private boolean threw;

    public KeyPearl() {
        super("keyPearl", Category.PLAYER);
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
        if (!Game.playing(mc)) return;
        int selected = Hotbar.selected(player);
        int slot = player.getMainHandItem().is(Items.ENDER_PEARL) ? selected : Hotbar.find(player, stack -> stack.is(Items.ENDER_PEARL));
        if (slot == Hotbar.NONE || player.getCooldowns().isOnCooldown(player.getInventory().getItem(slot))) return;
        if (slot != selected) {
            if (originalSlot == Hotbar.NONE) originalSlot = selected;
            Hotbar.select(player, slot);
            sinceSwap.reset();
        }
        if (mc.gameMode.useItem(player, InteractionHand.MAIN_HAND).consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
            threw = true;
        }
    }

    private void onTick(PreTickEvent event) {
        boolean thrown = threw;
        threw = false;
        if (originalSlot == Hotbar.NONE || thrown) return;
        LocalPlayer player = event.client().player;
        if (player == null || !swapBack.get()) {
            originalSlot = Hotbar.NONE;
        } else if (sinceSwap.elapsed(delay.get())) {
            restore(player);
        }
    }

    private void restore(LocalPlayer player) {
        if (player != null && swapBack.get() && originalSlot != Hotbar.NONE) Hotbar.select(player, originalSlot);
        originalSlot = Hotbar.NONE;
    }
}
