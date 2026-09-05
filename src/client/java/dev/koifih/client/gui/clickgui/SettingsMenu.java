package dev.koifih.client.gui.clickgui;

import dev.koifih.client.gui.Lang;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.gui.component.AccentPicker;
import dev.koifih.client.gui.component.Control;
import dev.koifih.client.gui.component.Dropdown;
import dev.koifih.client.gui.component.IconButton;
import dev.koifih.client.gui.component.Keybind;
import dev.koifih.client.gui.component.Popup;
import dev.koifih.client.gui.component.ThemeSwitch;
import dev.koifih.client.keybind.Keybinds;
import dev.koifih.client.rendering.TextRenderer;
import dev.koifih.client.rendering.Draw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class SettingsMenu {
    private static final int REVEAL_MILLIS = 150;
    private static final int WIDTH = 150;
    private static final int PADDING = 10;
    private static final int TITLE_HEIGHT = 18;
    private static final int ROW_STRIDE = 22;
    private static final int ROW_COUNT = 4;
    private static final int GEAR_GAP = 6;
    private static final int GEAR_SIZE = 16;
    private static final int RADIUS = 6;
    private static final int CLOSE_ICON = 0xe5cd;
    private static final int LANGUAGE_ICON = 0xe894;
    private static final int BIND_WIDTH = 42;
    private static final int BIND_HEIGHT = 16;

    private final WidgetHost host;
    private final Transition reveal = new Transition(0f, REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
    private final List<Control> controls = new ArrayList<>();
    private PanelLayout layout;
    private boolean open;
    private Dropdown language;
    private AccentPicker accent;
    private Keybind bind;

    public SettingsMenu(WidgetHost host) {
        this.host = host;
    }

    public void init(PanelLayout layout) {
        this.layout = layout;
        controls.clear();
        float scale = layout.scale();
        int rowHeight = layout.atLeastOne(ROW_STRIDE);
        int inner = layout.scaled(PADDING);

        int closeSize = layout.atLeastOne(12);
        IconButton close = host.add(new IconButton(right() - inner - closeSize,
                y() + inner + (layout.scaled(TITLE_HEIGHT) - closeSize) / 2, closeSize, scale, CLOSE_ICON,
                Component.literal("Close client settings"), this::close));

        int switchHeight = layout.atLeastOne(16);
        ThemeSwitch theme = host.add(new ThemeSwitch(x() + inner, rowY(0) + (rowHeight - switchHeight) / 2,
                width() - 2 * inner, switchHeight, scale));

        String[] languages = new String[Lang.Language.ALL.length];
        for (int i = 0; i < languages.length; i++) languages[i] = Lang.Language.ALL[i].displayName();
        int dropdownHeight = layout.atLeastOne(18);
        language = host.add(new Dropdown(x() + inner, rowY(1) + (rowHeight - dropdownHeight) / 2,
                width() - 2 * inner, dropdownHeight, scale, Component.literal(Lang.get("language")), languages,
                () -> Lang.current().ordinal(), index -> {
                    Lang.set(Lang.Language.ALL[index]);
                    host.requestRebuild();
                }));
        language.setIcon(LANGUAGE_ICON);
        language.setBottomLimit(layout.bottom());

        int accentHeight = layout.atLeastOne(14);
        AccentPicker picker = new AccentPicker(0, rowY(2) + (rowHeight - accentHeight) / 2, accentHeight, scale,
                Theme::accentRgb, Theme::setAccent);
        picker.setX(right() - inner - picker.getWidth());
        accent = host.add(picker);
        accent.setBottomLimit(layout.bottom());

        int bindWidth = layout.atLeastOne(BIND_WIDTH);
        int bindHeight = layout.atLeastOne(BIND_HEIGHT);
        bind = host.add(new Keybind(right() - inner - bindWidth, rowY(3) + (rowHeight - bindHeight) / 2,
                bindWidth, bindHeight, scale, Component.literal(Lang.get("clickgui_bind")),
                Keybinds::clickGuiKey, Keybinds::setClickGuiKey));
        bind.setClearable(false);

        controls.addAll(List.of(close, theme, language, accent, bind));
    }

    public int width() {
        return Math.min(layout.width() - 2 * layout.padding(), layout.scaled(WIDTH));
    }

    public int height() {
        return layout.scaled(2 * PADDING + TITLE_HEIGHT + ROW_COUNT * ROW_STRIDE);
    }

    public int x() {
        return layout.x() + layout.sidebarInset();
    }

    public int right() {
        return x() + width();
    }

    public int y() {
        return layout.bottom() - layout.sidebarInset() - layout.atLeastOne(GEAR_SIZE) - layout.scaled(GEAR_GAP) - height();
    }

    private int rowY(int row) {
        return y() + layout.scaled(PADDING + TITLE_HEIGHT) + layout.scaled(row * ROW_STRIDE);
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x() && pointX < right() && pointY >= y() && pointY < y() + height();
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isHidden() {
        return !open && reveal.value() == 0f;
    }

    public List<Popup> popups() {
        List<Popup> popups = new ArrayList<>();
        for (Control control : controls) if (control instanceof Popup popup) popups.add(popup);
        return popups;
    }

    public boolean captureMouse(MouseButtonEvent event) {
        return bind.captureMouse(event);
    }

    public void open() {
        open = true;
        reveal.set(1f);
        host.dropFocus();
    }

    public void close() {
        for (Control control : controls) control.dismiss();
        open = false;
        reveal.set(0f);
        host.dropFocus();
    }

    public void updateStates() {
        boolean ready = open && reveal.value() == 1f;
        for (Control control : controls) {
            control.active = ready;
            control.visible = reveal.value() > 0f;
        }
    }

    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float shown = reveal.value();
        if (shown <= 0f) return;
        graphics.nextStratum();
        float scale = layout.scale();
        float anchorX = x() + layout.scaled(8);
        float anchorY = y() + height();
        Draw.popIn(graphics, anchorX, anchorY, shown, 0.94f, () -> Draw.translated(graphics, 0f, 4 * scale * (1f - shown),
                () -> drawBody(graphics, mouseX, mouseY, delta)));
    }

    private void drawBody(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(RADIUS);
        Draw.borderedBox(graphics, x(), y(), width(), height(), radius, Theme.OVERLAY);
        float textX = x() + PADDING * scale;
        TextRenderer.drawCentered(graphics, Lang.get("settings"), textX,
                y() + (PADDING + TITLE_HEIGHT * 0.5f) * scale, 8 * scale, Theme.TEXT);
        String[] labels = {Lang.get("accent"), Lang.get("clickgui_bind")};
        for (int row = 0; row < labels.length; row++) {
            TextRenderer.drawCentered(graphics, labels[row], textX,
                    rowY(row + 2) + ROW_STRIDE * 0.5f * scale, 7 * scale, Theme.TEXT);
        }
        for (Control control : controls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        language.renderPopup(graphics, mouseX, mouseY);
        accent.renderPopup(graphics, mouseX, mouseY);
    }
}
