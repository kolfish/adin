package dev.koifih.client.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import dev.koifih.client.rendering.screen.HealthBar;
import dev.koifih.client.rendering.screen.RectStyle;
import dev.koifih.client.rendering.world.BoxStyle;
import net.minecraft.world.phys.AABB;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PreviewSetting extends Setting<Void> {
    private final BooleanSupplier flat;
    private final BooleanSupplier boxShown;
    private final Supplier<RectStyle> rectStyle;
    private final Supplier<BoxStyle> boxStyle;
    private final UnaryOperator<AABB> flatFit;
    private final Supplier<HealthBar.Side> healthSide;

    public PreviewSetting(String id, BooleanSupplier flat, BooleanSupplier boxShown, Supplier<RectStyle> rectStyle,
                          Supplier<BoxStyle> boxStyle, UnaryOperator<AABB> flatFit, Supplier<HealthBar.Side> healthSide) {
        super(id, null);
        this.flat = flat;
        this.boxShown = boxShown;
        this.rectStyle = rectStyle;
        this.boxStyle = boxStyle;
        this.flatFit = flatFit;
        this.healthSide = healthSide;
    }

    public boolean isFlat() {
        return flat.getAsBoolean();
    }

    public boolean isBoxShown() {
        return boxShown.getAsBoolean();
    }

    public RectStyle rectStyle() {
        return rectStyle.get();
    }

    public BoxStyle boxStyle() {
        return boxStyle.get();
    }

    public AABB fit(AABB bounds) {
        return isFlat() ? flatFit.apply(bounds) : bounds;
    }

    public HealthBar.Side healthSide() {
        return healthSide.get();
    }

    @Override
    public JsonElement save() {
        return JsonNull.INSTANCE;
    }

    @Override
    public void load(JsonElement json) {
    }
}
