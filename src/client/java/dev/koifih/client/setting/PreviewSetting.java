package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import dev.koifih.client.render.Style;
import dev.koifih.client.render.screen.HealthBar;
import net.minecraft.world.phys.AABB;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PreviewSetting extends Setting<Void> {
    private final BooleanSupplier flat;
    private final BooleanSupplier boxShown;
    private final Supplier<Style> screenStyle;
    private final Supplier<Style> worldStyle;
    private final UnaryOperator<AABB> flatFit;
    private final Supplier<HealthBar.Side> healthSide;

    public PreviewSetting(String id, BooleanSupplier flat, BooleanSupplier boxShown, Supplier<Style> screenStyle,
                          Supplier<Style> worldStyle, UnaryOperator<AABB> flatFit, Supplier<HealthBar.Side> healthSide) {
        super(id, null);
        this.flat = flat;
        this.boxShown = boxShown;
        this.screenStyle = screenStyle;
        this.worldStyle = worldStyle;
        this.flatFit = flatFit;
        this.healthSide = healthSide;
    }

    public boolean isFlat() {
        return flat.getAsBoolean();
    }

    public boolean isBoxShown() {
        return boxShown.getAsBoolean();
    }

    public Style screenStyle() {
        return screenStyle.get();
    }

    public Style worldStyle() {
        return worldStyle.get();
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
