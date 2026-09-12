package dev.koifih.client.ui.clickgui;

import dev.koifih.client.input.Keybinds;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Tooltips;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.ui.Units;
import dev.koifih.client.ui.component.AccentPicker;
import dev.koifih.client.ui.component.Bool;
import dev.koifih.client.ui.component.Button;
import dev.koifih.client.ui.component.Control;
import dev.koifih.client.ui.component.Dropdown;
import dev.koifih.client.ui.component.Keybind;
import dev.koifih.client.ui.component.Popup;
import dev.koifih.client.ui.component.Segmented;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.util.Lang;
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
    private static final int ROW_COUNT = 8;
    private static final int TOGGLE_WIDTH = 24;
    private static final int TOGGLE_HEIGHT = 12;
    private static final int TOOLTIPS_ICON = 0xe88e;
    private static final int GEAR_GAP = 6;
    private static final int GEAR_SIZE = 16;
    private static final int RADIUS = 6;
    private static final int CLOSE_ICON = 0xe5cd;
    private static final int LIGHT_ICON = 0xe518;
    private static final int DARK_ICON = 0xe51c;
    private static final int LANGUAGE_ICON = 0xe894;
    private static final int SIZE_ICON = 0xe8ff;
    private static final int UNITS_ICON = 0xe41c;
    private static final int BIND_WIDTH = 42;
    private static final int BIND_HEIGHT = 16;
    private static final Theme.Mode[] MODES = {Theme.Mode.LIGHT, Theme.Mode.DARK};

    private final ClickGui gui;
    private final Transition reveal = new Transition(0f, REVEAL_MILLIS);
    private final List<Control> controls = new ArrayList<>();
    private PanelLayout layout;
    private boolean open;
    private Dropdown language;
    private Dropdown size;
    private Dropdown units;
    private Dropdown tooltips;
    private AccentPicker accent;
    private Keybind bind;

    public SettingsMenu(ClickGui gui) {
        this.gui = gui;
    }

    public void init(PanelLayout layout) {
        this.layout = layout;
        controls.clear();
        float scale = layout.scale();
        int rowHeight = layout.atLeastOne(ROW_STRIDE);
        int inner = layout.scaled(PADDING);

        int closeSize = layout.atLeastOne(12);
        Button close = gui.add(Button.icon(right() - inner - closeSize,
                y() + inner + (layout.scaled(TITLE_HEIGHT) - closeSize) / 2, closeSize, scale, CLOSE_ICON,
                Component.literal("Close client settings"), this::close));

        int switchHeight = layout.atLeastOne(16);
        Segmented theme = gui.add(new Segmented(x() + inner, rowY(0) + (rowHeight - switchHeight) / 2,
                width() - 2 * inner, switchHeight, scale, Component.literal("Theme"),
                new Segmented.Segment[] {
                        Segmented.Segment.of(LIGHT_ICON, Lang.get("theme.light")),
                        Segmented.Segment.of(DARK_ICON, Lang.get("theme.dark"))},
                () -> Theme.mode() == Theme.Mode.LIGHT ? 0 : 1, index -> Theme.setMode(MODES[index])));

        String[] languages = new String[Lang.Language.ALL.length];
        for (int i = 0; i < languages.length; i++) languages[i] = Lang.Language.ALL[i].displayName();
        int dropdownHeight = layout.atLeastOne(18);
        language = gui.add(Dropdown.single(x() + inner, rowY(1) + (rowHeight - dropdownHeight) / 2,
                width() - 2 * inner, dropdownHeight, scale, Component.literal(Lang.get("language")), languages,
                () -> Lang.current().ordinal(), index -> {
                    Lang.set(Lang.Language.ALL[index]);
                    gui.requestRebuild();
                }));
        language.setIcon(LANGUAGE_ICON);
        language.setBottomLimit(layout.bottom());

        String[] sizes = new String[UiScale.ALL.length];
        for (int i = 0; i < sizes.length; i++) sizes[i] = UiScale.ALL[i].displayName();
        size = gui.add(Dropdown.single(x() + inner, rowY(2) + (rowHeight - dropdownHeight) / 2,
                width() - 2 * inner, dropdownHeight, scale, Component.literal(Lang.get("size")), sizes,
                () -> UiScale.current().ordinal(), index -> UiScale.set(UiScale.ALL[index])));
        size.setIcon(SIZE_ICON);
        size.setBottomLimit(layout.bottom());

        String[] unitNames = new String[Units.ALL.length];
        for (int i = 0; i < unitNames.length; i++) unitNames[i] = Units.ALL[i].displayName();
        units = gui.add(Dropdown.single(x() + inner, rowY(3) + (rowHeight - dropdownHeight) / 2,
                width() - 2 * inner, dropdownHeight, scale, Component.literal(Lang.get("units")), unitNames,
                () -> Units.current().ordinal(), index -> Units.set(Units.ALL[index])));
        units.setIcon(UNITS_ICON);
        units.setBottomLimit(layout.bottom());

        int accentHeight = layout.atLeastOne(14);
        AccentPicker picker = new AccentPicker(0, rowY(5) + (rowHeight - accentHeight) / 2, accentHeight, scale,
                Theme::accentRgb, Theme::setAccent);
        picker.setX(right() - inner - picker.getWidth());
        accent = gui.add(picker);
        accent.setBottomLimit(layout.bottom());

        int bindWidth = layout.atLeastOne(BIND_WIDTH);
        int bindHeight = layout.atLeastOne(BIND_HEIGHT);
        String[] tooltipModes = new String[Tooltips.ALL.length];
        for (int i = 0; i < tooltipModes.length; i++) tooltipModes[i] = Tooltips.ALL[i].displayName();
        tooltips = gui.add(Dropdown.single(x() + inner, rowY(4) + (rowHeight - dropdownHeight) / 2,
                width() - 2 * inner, dropdownHeight, scale, Component.literal(Lang.get("tooltips")), tooltipModes,
                () -> Tooltips.current().ordinal(), index -> {
                    Tooltips.set(Tooltips.ALL[index]);
                    gui.requestRebuild();
                }));
        tooltips.setIcon(TOOLTIPS_ICON);
        tooltips.setBottomLimit(layout.bottom());

        bind = gui.add(new Keybind(right() - inner - bindWidth, rowY(6) + (rowHeight - bindHeight) / 2,
                bindWidth, bindHeight, scale, Component.literal(Lang.get("clickgui_bind")),
                Keybinds::clickGuiKey, Keybinds::setClickGuiKey));
        bind.setClearable(false);

        int toggleWidth = layout.atLeastOne(TOGGLE_WIDTH);
        int toggleHeight = layout.atLeastOne(TOGGLE_HEIGHT);
        Bool clicks = gui.add(new Bool(right() - inner - toggleWidth, rowY(7) + (rowHeight - toggleHeight) / 2,
                toggleWidth, toggleHeight, scale, Component.literal(Lang.get("click_simulation")),
                () -> Clicks.simulate, value -> Clicks.simulate = value));

        controls.addAll(List.of(close, theme, language, size, units, tooltips, accent, bind, clicks));
    }

    public void relayout(PanelLayout layout) {
        this.layout = layout;
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
        gui.dropFocus();
    }

    public void close() {
        for (Control control : controls) control.dismiss();
        open = false;
        reveal.set(0f);
        gui.dropFocus();
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
        Transform.popIn(graphics, anchorX, anchorY, shown, 0.94f, () -> Transform.translated(graphics, 0f, 4 * scale * (1f - shown),
                () -> drawBody(graphics, mouseX, mouseY, delta)));
    }

    private void drawBody(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(RADIUS);
        Draw.bordered(graphics, x(), y(), width(), height(), radius, Theme.OVERLAY, Theme.POPUP_BORDER);
        float textX = x() + PADDING * scale;
        Text.drawCentered(graphics, Lang.get("settings"), textX,
                y() + (PADDING + TITLE_HEIGHT * 0.5f) * scale, 8 * scale, Theme.TEXT);
        Text.drawCentered(graphics, Lang.get("accent"), textX, rowY(5) + ROW_STRIDE * 0.5f * scale, 7 * scale, Theme.TEXT);
        Text.drawCentered(graphics, Lang.get("clickgui_bind"), textX, rowY(6) + ROW_STRIDE * 0.5f * scale, 7 * scale, Theme.TEXT);
        Text.drawCentered(graphics, Lang.get("click_simulation"), textX, rowY(7) + ROW_STRIDE * 0.5f * scale, 7 * scale, Theme.TEXT);
        for (Control control : controls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        language.renderPopup(graphics, mouseX, mouseY);
        size.renderPopup(graphics, mouseX, mouseY);
        units.renderPopup(graphics, mouseX, mouseY);
        tooltips.renderPopup(graphics, mouseX, mouseY);
        accent.renderPopup(graphics, mouseX, mouseY);
    }
}
