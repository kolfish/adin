package dev.koifih.client.module.impl.movement;

import dev.koifih.client.module.Module;
import dev.koifih.client.setting.EnumSetting;

public final class MoveFix extends Module {
    private static final String[] MODES = {"Silent", "Strict"};
    private static final int SILENT = 0;

    private final EnumSetting mode = add(new EnumSetting("mode", SILENT, MODES));

    public MoveFix() {
        super("moveFix");
    }

    @Override
    public String info() {
        return mode.selected();
    }

    public boolean corrects() {
        return isEnabled();
    }

    public boolean correctsInput() {
        return isEnabled() && mode.get() == SILENT;
    }
}
