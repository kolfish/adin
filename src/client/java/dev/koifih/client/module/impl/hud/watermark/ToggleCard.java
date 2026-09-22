package dev.koifih.client.module.impl.hud.watermark;

import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Switch;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.Transition.Easing;
import dev.koifih.client.util.Lang;
import dev.koifih.client.util.Time;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class ToggleCard {
    static final float WIDTH = 196f;
    static final float HEIGHT = 36f;

    private static final long SHOW_MILLIS = 2200L;
    private static final float CHIP = 24f;
    private static final float CHIP_START = 6f;
    private static final float CHIP_ICON = 13f;
    private static final float TEXT_GAP = 7f;
    private static final float TITLE = 7.5f;
    private static final float TITLE_RISE = 4.6f;
    private static final float STATE = 6f;
    private static final float STATE_DROP = 5.2f;
    private static final float SWITCH_WIDTH = 18f;
    private static final float SWITCH_HEIGHT = 10f;
    private static final float SWITCH_END = 10f;
    private static final int FLIP_MILLIS = 300;
    private static final int FLIP_DELAY = 400;

    private final Module module;
    private final Time.Stopwatch opened = new Time.Stopwatch();
    private final Time.Stopwatch touched = new Time.Stopwatch();
    private final Transition flip;
    private boolean enabled;

    ToggleCard(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
        flip = new Transition(enabled ? 0f : 1f, FLIP_MILLIS, Transition.Easing.SMOOTHSTEP);
        flip.set(enabled ? 1f : 0f, FLIP_DELAY);
    }

    boolean shows(Module other) {
        return module == other;
    }

    void update(boolean enabled) {
        this.enabled = enabled;
        flip.set(enabled ? 1f : 0f);
        touched.reset();
    }

    boolean expired() {
        return touched.elapsed(SHOW_MILLIS);
    }

    void draw(GuiGraphicsExtractor graphics, float x, float centerY, float width, float scale, Text.ColorAt hero) {
        float since = opened.seconds();
        float chip = CHIP * scale;
        float chipX = x + CHIP_START * scale;
        float chipCenterX = chipX + chip * 0.5f;
        Draw.rect(graphics, chipX, centerY - chip * 0.5f, chip, Math.round(chip), Math.round(chip * 0.5f), Theme.ROW);
        float icon = CHIP_ICON * scale;
        float pop = Math.max(0f, Easing.EASE_OUT_BACK.over(since, 0.2f, 0.45f));
        int color = hero.at(chipCenterX);
        Transform.scaledAbout(graphics, chipCenterX, centerY, pop, () -> Draw.icon(graphics, module.category().icon(),
                chipCenterX - icon * 0.5f, centerY - icon * 0.5f, icon, Theme.DIM, color, Theme.TEXT));
        float textX = chipX + chip + TEXT_GAP * scale;
        float switchX = x + width - (SWITCH_END + SWITCH_WIDTH) * scale;
        float room = switchX - textX - TEXT_GAP * scale;
        Text.drawCentered(graphics, Text.fit(module.name(), room, TITLE * scale), textX,
                centerY - TITLE_RISE * scale, TITLE * scale, Theme.TEXT);
        String state = Lang.get(enabled ? "notifications.enabled" : "notifications.disabled");
        Text.drawCentered(graphics, state, textX, centerY + STATE_DROP * scale, STATE * scale, enabled ? Theme.ACCENT : Theme.DIM);
        int switchHeight = Math.max(2, Math.round(SWITCH_HEIGHT * scale));
        Switch.draw(graphics, switchX, centerY - switchHeight * 0.5f, SWITCH_WIDTH * scale, switchHeight,
                flip.value(), (float) Math.sin(Math.PI * (1f - flip.flight())));
    }
}
