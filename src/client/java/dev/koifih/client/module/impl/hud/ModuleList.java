package dev.koifih.client.module.impl.hud;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.module.HudModule;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.PositionSetting.Anchor;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.setting.TextColorSettings;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModuleList extends HudModule {
    private static final float HEIGHT = 12f;
    private static final float PADDING = 4f;
    private static final float EDGE_PADDING = 2f;
    private static final float GAP = 3f;
    private static final float RADIUS = 4f;
    private static final float TEXT_SIZE = 8f;
    private static final float ALPHA = 0.8f;

    private record Row(String name, String info, float width) {}

    private final BoolSetting prefix = add(new BoolSetting("prefix", true));
    private final SliderSetting size = add(new SliderSetting("scale", 100, 50, 200, Measure.PERCENT));
    private final TextColorSettings colors = add(new TextColorSettings());

    public ModuleList() {
        super("arraylist", Anchor.END, Anchor.START, 0f, 0f);
    }

    @Override
    protected void onEnable() {
        listen(HudRenderEvent.class, this::onHudRender);
    }

    private void onHudRender(HudRenderEvent event) {
        Theme.update();
        GuiGraphicsExtractor graphics = event.graphics();
        float scale = UiScale.current().factor() * size.get() / 100f;
        float textSize = TEXT_SIZE * scale;
        float padding = PADDING * scale;
        float edgePadding = EDGE_PADDING * scale;
        float gap = GAP * scale;
        List<Row> rows = new ArrayList<>();
        for (Module module : AdinClient.MODULES.all()) {
            if (!module.isEnabled()) continue;
            String info = prefix.get() ? module.info() : "";
            float width = Text.width(module.name(), textSize) + (info.isEmpty() ? 0f : gap + Text.width(info, textSize));
            rows.add(new Row(module.name(), info, width + padding + edgePadding));
        }
        if (rows.isEmpty()) {
            placed(0f, 0f, 0f, 0f);
            return;
        }
        rows.sort(Comparator.comparingDouble(Row::width).reversed().thenComparing(Row::name));
        var window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int height = Math.max(1, Math.round(HEIGHT * scale));
        int radius = Math.round(RADIUS * scale);
        int[] widths = new int[rows.size()];
        for (int i = 0; i < widths.length; i++) widths[i] = Math.round(rows.get(i).width());
        int listWidth = widths[0];
        int listHeight = height * widths.length;
        int x = Math.round(left(listWidth, screenWidth));
        int y = Math.round(top(listHeight, window.getGuiScaledHeight()));
        placed(x, y, listWidth, listHeight);
        boolean rightAligned = position().horizontal() != Anchor.START;
        int edge = rightAligned ? x + listWidth : x;
        boolean flushSide = rightAligned ? edge >= screenWidth : edge <= 0;
        boolean flushTop = y <= 0;
        int background = Colors.withAlpha(Theme.MAIN, ALPHA);
        for (int i = 0; i < widths.length; i++) {
            int rowWidth = widths[i];
            int rowX = rightAligned ? edge - rowWidth : edge;
            int rowY = y + i * height;
            int step = i + 1 < widths.length ? Math.min(radius, (rowWidth - widths[i + 1]) / 2) : radius;
            int corners = rightAligned ? Draw.BOTTOM_LEFT : Draw.BOTTOM_RIGHT;
            if (i == 0 && !flushTop) corners |= rightAligned ? Draw.TOP_LEFT : Draw.TOP_RIGHT;
            if (i == 0 && !flushTop && !flushSide) corners |= rightAligned ? Draw.TOP_RIGHT : Draw.TOP_LEFT;
            if (i == widths.length - 1 && !flushSide) corners |= rightAligned ? Draw.BOTTOM_RIGHT : Draw.BOTTOM_LEFT;
            Draw.rect(graphics, rowX, rowY, rowWidth, height, step, corners, background);
            if (i == 0) continue;
            int fillet = Math.min(radius, (widths[i - 1] - rowWidth) / 2);
            if (fillet <= 0) continue;
            if (rightAligned) Draw.fillet(graphics, rowX - fillet, rowY, fillet, fillet, fillet, Draw.BOTTOM_LEFT, background);
            else Draw.fillet(graphics, rowX + rowWidth, rowY, fillet, fillet, fillet, Draw.BOTTOM_RIGHT, background);
        }
        Text.ColorAt color = colors.colorAt(scale);
        for (int i = 0; i < widths.length; i++) {
            Row row = rows.get(i);
            int rowX = rightAligned ? edge - widths[i] : edge;
            float textX = rowX + (rightAligned ? padding : edgePadding);
            float centerY = y + i * height + height * 0.5f;
            Text.drawCentered(graphics, row.name(), textX, centerY, textSize, color);
            if (row.info().isEmpty()) continue;
            Text.drawCentered(graphics, row.info(), textX + Text.width(row.name(), textSize) + gap, centerY, textSize, Theme.MUTED);
        }
    }
}
