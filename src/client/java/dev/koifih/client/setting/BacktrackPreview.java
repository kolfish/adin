package dev.koifih.client.setting;

import dev.koifih.client.render.Style;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class BacktrackPreview extends PreviewSetting {
    private final IntSupplier delayMillis;
    private final BooleanSupplier frozen;
    private final Supplier<Style> style;

    public BacktrackPreview(String id, IntSupplier delayMillis, BooleanSupplier frozen, Supplier<Style> style) {
        super(id);
        this.delayMillis = delayMillis;
        this.frozen = frozen;
        this.style = style;
    }

    public int delayMillis() {
        return delayMillis.getAsInt();
    }

    public boolean frozen() {
        return frozen.getAsBoolean();
    }

    public Style style() {
        return style.get();
    }
}
