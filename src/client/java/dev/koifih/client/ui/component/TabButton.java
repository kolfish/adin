package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.gui.clickgui.Sidebar;
import dev.koifih.client.render.gui.Icons;
import dev.koifih.client.render.gui.Text;
import dev.koifih.client.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;

public final class TabButton extends Clickable {
    private static final float ICON_INSET = 5f;
    private static final float ICON_SIZE = 11f;
    private static final float TEXT_INSET = 22f;
    private static final float TEXT_SIZE = 8.5f;
    private static final float TEXT_RIGHT_PADDING = 5f;

    private final Sidebar.Tab tab;
    private final BooleanSupplier selected;
    private final Transition onPill = new Transition(0f, 150, Easing.EASE_OUT_CUBIC);

    public TabButton(int x, int y, int width, int height, float scale, Sidebar.Tab tab,
                     BooleanSupplier selected, Runnable onSelect) {
        super(x, y, width, height, scale, Component.literal(tab.label().get()), onSelect);
        this.tab = tab;
        this.selected = selected;
    }

    public static int iconInset(float scale) {
        return Math.round(ICON_INSET * scale);
    }

    public Sidebar.Tab tab() {
        return tab;
    }

    @Override
    public Component getMessage() {
        return Component.literal(tab.label().get());
    }

    public void setOnPill(boolean covered) {
        onPill.set(covered ? 1f : 0f);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float covered = onPill.value();
        float iconSize = ICON_SIZE * scale;
        Icons.draw(graphics, tab.icon(), getX() + ICON_INSET * scale,
                getY() + (getHeight() - iconSize) * 0.5f, iconSize, Colors.lerp(Theme.DIM, 0xFF000000, covered));
        String label = getMessage().getString();
        float textX = getX() + TEXT_INSET * scale;
        float textSize = TEXT_SIZE * scale;
        float available = Math.max(1, getRight() - TEXT_RIGHT_PADDING * scale - textX);
        textSize *= Math.min(1f, available / Math.max(1f, Text.width(label, textSize)));
        Text.drawCentered(graphics, label, textX, getY() + getHeight() * 0.5f, textSize,
                Colors.lerp(Theme.DIM, Theme.ON_ACCENT, covered));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        super.updateWidgetNarration(output);
        if (selected.getAsBoolean()) {
            output.add(NarratedElementType.HINT, Component.translatable("category.adin.selected"));
        }
    }
}
