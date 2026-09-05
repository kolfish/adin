package dev.koifih.client.gui.clickgui;

import dev.koifih.client.feature.Category;
import dev.koifih.client.gui.Lang;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.component.IconButton;
import dev.koifih.client.gui.component.TabButton;
import dev.koifih.client.rendering.RectRenderer;
import dev.koifih.client.rendering.TextRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Sidebar {
    public record Tab(String id, int icon, Supplier<String> label, Category category) {}

    public static final List<Tab> TABS = tabs();
    private static final int BUTTON_HEIGHT = 22;
    private static final int BUTTON_STRIDE = 26;
    private static final int GEAR_SIZE = 16;
    private static final int SETTINGS_ICON = 0xe8b8;
    private static final int CONFIGS_ICON = 0xe2c7;
    private static final String BRAND_TEXT = "adin.lol";

    private final WidgetHost host;
    private final Consumer<Tab> onSelect;
    private final Runnable onSettings;
    private final List<TabButton> buttons = new ArrayList<>();
    private final SelectionPill pill = new SelectionPill();
    private IconButton settingsButton;

    public Sidebar(WidgetHost host, Consumer<Tab> onSelect, Runnable onSettings) {
        this.host = host;
        this.onSelect = onSelect;
        this.onSettings = onSettings;
    }

    private static List<Tab> tabs() {
        List<Tab> tabs = new ArrayList<>();
        for (Category category : Category.values()) tabs.add(new Tab(category.id(), category.icon(), category::label, category));
        tabs.add(new Tab("configs", CONFIGS_ICON, () -> Lang.get("category.configs"), null));
        return List.copyOf(tabs);
    }

    public void init(PanelLayout layout, Supplier<Tab> selected) {
        buttons.clear();
        pill.reset();
        float scale = layout.scale();
        int x = layout.x() + layout.sidebarInset() - TabButton.iconInset(scale);
        int maxWidth = Math.max(1, layout.sidebarWidth() - layout.sidebarInset() - (x - layout.x()));
        int height = layout.atLeastOne(BUTTON_HEIGHT);
        int index = 0;
        for (Tab tab : TABS) {
            int y = layout.contentY() + layout.scaled(PanelLayout.PADDING + index++ * BUTTON_STRIDE);
            int width = Math.min(maxWidth, TabButton.preferredWidth(tab, scale));
            buttons.add(host.add(new TabButton(x, y, width, height, scale, tab,
                    () -> selected.get() == tab, () -> onSelect.accept(tab))));
        }
        int gearSize = layout.atLeastOne(GEAR_SIZE);
        settingsButton = host.add(new IconButton(layout.x() + layout.sidebarInset(),
                layout.bottom() - layout.sidebarInset() - gearSize, gearSize, scale, SETTINGS_ICON,
                Component.literal("Client settings"), onSettings));
    }

    public void setActive(boolean active) {
        for (TabButton button : buttons) button.active = active;
        settingsButton.active = active;
    }

    public void resetPill() {
        pill.reset();
    }

    public void draw(GuiGraphicsExtractor graphics, PanelLayout layout, Tab selected, int mouseX, int mouseY, float delta) {
        drawBrand(graphics, layout);
        drawPill(graphics, selected);
        for (TabButton button : buttons) button.extractRenderState(graphics, mouseX, mouseY, delta);
        settingsButton.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawBrand(GuiGraphicsExtractor graphics, PanelLayout layout) {
        float size = 10 * layout.scale();
        float x = layout.x() + PanelLayout.SIDEBAR_INSET * layout.scale();
        float baseline = TextRenderer.centeredBaseline(BRAND_TEXT, size, layout.y() + layout.topBarHeight() * 0.5f);
        TextRenderer.draw(graphics, List.of(new TextRenderer.Span("adin", Theme.TEXT),
                new TextRenderer.Span(".lol", Theme.ACCENT)), x, baseline, size);
    }

    private void drawPill(GuiGraphicsExtractor graphics, Tab selected) {
        TabButton target = null;
        for (TabButton button : buttons) {
            if (button.tab() == selected) target = button;
        }
        if (target == null) return;
        SelectionPill.Position position = pill.update(target.getY(), target.getWidth());
        RectRenderer.draw(graphics, target.getX(), position.y(), position.width(),
                target.getHeight(), target.getHeight() / 2, Theme.ACCENT);
        for (TabButton button : buttons) {
            float center = button.getY() + button.getHeight() * 0.5f;
            button.setOnPill(center >= position.y() && center < position.y() + target.getHeight());
        }
    }
}
