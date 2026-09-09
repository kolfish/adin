package dev.koifih.client.module.impl.movement;

import dev.koifih.client.event.events.TickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
public final class Sprint extends Module {

    boolean wasSprinting;

    public Sprint() {
        super("sprint", Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        if (mc.options.toggleSprint().get()) {
            wasSprinting = true;
        }
        listen(TickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        if (wasSprinting) {
            mc.options.toggleSprint().set(true);
        }
    }

    private void onTick(TickEvent event) {
        if (mc.options.toggleSprint().get().booleanValue()) {
            mc.options.toggleSprint().set(false);
        }
        mc.options.keySprint.setDown(true);
    }
}
