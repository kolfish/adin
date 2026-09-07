package dev.koifih.client.module.impl.hud;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.UiScale;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.gui.Rects;
import dev.koifih.client.render.gui.Text;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModuleList extends Module {
    private static final float HEIGHT = 12f;
    private static final float PADDING = 4f;
    private static final float RIGHT_PADDING = 2f;
    private static final float GAP = 3f;
    private static final float RADIUS = 4f;
    private static final float TEXT_SIZE = 8f;
    private static final float ALPHA = 0.8f;
    private static final String[] COLORS = {"Accent", "Solid", "Gradient"};
    private static final int ACCENT = 0;
    private static final int GRADIENT = 2;
    private static final float WAVE_LENGTH = 120f;
    private static final float WAVE_SECONDS = 2f;

    private record Row(String name, String info, float width) {}

    private final BoolSetting prefix = add(new BoolSetting("prefix", true));
    private final SliderSetting size = add(new SliderSetting("scale", 100, 50, 200, Measure.PERCENT));
    private final EnumSetting color = add(new EnumSetting("color", ACCENT, COLORS));
    private final ColorSetting textColor = add(new ColorSetting("textColor", Theme.DEFAULT_ACCENT, 0xB7CCE8));

    public ModuleList() {
        super("arraylist", Category.HUD);
        textColor.visibleWhen(() -> color.get() != ACCENT);
        textColor.gradientWhen(() -> color.get() == GRADIENT);
    }

    @Override
    protected void onEnable() {
        listen(HudRenderEvent.class, this::onHudRender);
    }

    private void onHudRender(HudRenderEvent event) {
        Theme.update();
        float scale = UiScale.current().factor() * size.get() / 100f;
        float textSize = TEXT_SIZE * scale;
        float padding = PADDING * scale;
        float gap = GAP * scale;
        List<Row> rows = new ArrayList<>();
        for (Module module : AdinClient.MODULES.all()) {
            if (!module.isEnabled()) continue;
            String info = prefix.get() ? module.info() : "";
            float width = Text.width(module.name(), textSize) + (info.isEmpty() ? 0f : gap + Text.width(info, textSize));
            rows.add(new Row(module.name(), info, width + padding + RIGHT_PADDING * scale));
        }
        if (rows.isEmpty()) return;
        rows.sort(Comparator.comparingDouble(Row::width).reversed().thenComparing(Row::name));
        int right = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Math.max(1, Math.round(HEIGHT * scale));
        int radius = Math.round(RADIUS * scale);
        int[] lefts = new int[rows.size()];
        for (int i = 0; i < lefts.length; i++) lefts[i] = right - Math.round(rows.get(i).width());
        int background = Colors.withAlpha(Theme.MAIN, ALPHA);
        for (int i = 0; i < lefts.length; i++) {
            int x = lefts[i];
            int y = i * height;
            int below = i + 1 < lefts.length ? Math.min(radius, (lefts[i + 1] - x) / 2) : radius;
            Rects.draw(event.graphics(), x, y, right - x, height, below, Rects.BOTTOM_LEFT, background);
            if (i == 0) continue;
            int fillet = Math.min(radius, (x - lefts[i - 1]) / 2);
            if (fillet > 0) Rects.fillet(event.graphics(), x - fillet, y, fillet, fillet, fillet, Rects.BOTTOM_LEFT, background);
        }
        for (int i = 0; i < lefts.length; i++) {
            Row row = rows.get(i);
            float textX = lefts[i] + padding;
            float centerY = i * height + height * 0.5f;
            Text.drawCentered(event.graphics(), row.name(), textX, centerY, textSize, nameColor(scale));
            if (row.info().isEmpty()) continue;
            Text.drawCentered(event.graphics(), row.info(), textX + Text.width(row.name(), textSize) + gap, centerY, textSize, Theme.MUTED);
        }
    }

    private Text.ColorAt nameColor(float scale) {
        if (color.get() == ACCENT) return x -> Theme.ACCENT;
        int primary = Colors.opaque(textColor.get());
        if (color.get() != GRADIENT) return x -> primary;
        int secondary = Colors.opaque(textColor.secondary());
        float phase = (System.nanoTime() / 1_000_000_000f) / WAVE_SECONDS;
        float length = WAVE_LENGTH * scale;
        return x -> {
            float wave = (x / length - phase) % 1f;
            if (wave < 0f) wave += 1f;
            return Colors.lerp(primary, secondary, wave < 0.5f ? wave * 2f : 2f - wave * 2f);
        };
    }
}
