package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import dev.koifih.client.render.Style;
import dev.koifih.client.render.screen.HealthBar;
import net.minecraft.world.phys.AABB;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PreviewSetting extends Setting<Void> {
    public record Shade(int rgb, int secondaryRgb, boolean gradient, int effect) {}

    private final BooleanSupplier flat;
    private final BooleanSupplier boxShown;
    private final Supplier<Style> screenStyle;
    private final Supplier<Style> worldStyle;
    private final UnaryOperator<AABB> flatFit;
    private final Supplier<HealthBar.Side> healthSide;
    private final Function<Setting<?>, Shade> shade;

    public PreviewSetting(String id, BooleanSupplier flat, BooleanSupplier boxShown, Supplier<Style> screenStyle,
                          Supplier<Style> worldStyle, UnaryOperator<AABB> flatFit, Supplier<HealthBar.Side> healthSide,
                          Function<Setting<?>, Shade> shade) {
        super(id, null);
        this.flat = flat;
        this.boxShown = boxShown;
        this.screenStyle = screenStyle;
        this.worldStyle = worldStyle;
        this.flatFit = flatFit;
        this.healthSide = healthSide;
        this.shade = shade;
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

    public Shade shade(Setting<?> highlighted) {
        return highlighted == null ? null : shade.apply(highlighted);
    }

    @Override
    public JsonElement save() {
        return JsonNull.INSTANCE;
    }

    @Override
    public void load(JsonElement json) {
    }
}
