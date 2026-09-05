package dev.koifih.client.feature.movement;

import dev.koifih.client.event.TickEvent;
import dev.koifih.client.feature.Category;
import dev.koifih.client.feature.Feature;
import dev.koifih.client.feature.setting.BoolSetting;
import net.minecraft.client.player.LocalPlayer;

public final class Sprint extends Feature {
    private final BoolSetting omni = add(new BoolSetting("omni", false));

    public Sprint() {
        super("sprint", Category.MOVEMENT);
    }

    public boolean omniActive() {
        return isEnabled() && omni.get();
    }

    @Override
    protected void onEnable() {
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        LocalPlayer player = event.client().player;
        if (player == null || player.isSprinting() || !canSprint(player)) return;
        boolean moving = omni.get()
                ? player.input.getMoveVector().lengthSquared() > 1.0E-10f
                : player.input.hasForwardImpulse();
        if (moving) player.setSprinting(true);
    }

    private static boolean canSprint(LocalPlayer player) {
        boolean fed = player.getFoodData().getFoodLevel() > 6 || player.getAbilities().mayfly;
        return fed && !player.isShiftKeyDown() && !player.isUsingItem() && !player.isFallFlying() && !player.isPassenger();
    }
}
