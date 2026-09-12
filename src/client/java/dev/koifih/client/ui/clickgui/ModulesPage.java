package dev.koifih.client.ui.clickgui;

import dev.koifih.client.AdinClient;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Scissor;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.setting.BlockSetting;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EntitySetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.HotbarSetting;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.setting.RangeSetting;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.ui.Catalog;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Tooltips;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.component.Bool;
import dev.koifih.client.ui.component.Button;
import dev.koifih.client.ui.component.CatalogPicker;
import dev.koifih.client.ui.component.ColorPicker;
import dev.koifih.client.ui.component.Control;
import dev.koifih.client.ui.component.Dropdown;
import dev.koifih.client.ui.component.HelpDot;
import dev.koifih.client.ui.component.HotbarPicker;
import dev.koifih.client.ui.component.Keybind;
import dev.koifih.client.ui.component.Popup;
import dev.koifih.client.ui.component.Segmented;
import dev.koifih.client.ui.component.Slider;
import dev.koifih.client.ui.component.VideoPopup;
import dev.koifih.client.ui.video.Videos;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Lang;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ModulesPage implements Page {
    private static final int ROW_HEIGHT = 40;
    private static final int ROW_STRIDE = 46;
    private static final float DESCRIPTION_SIZE = 6.8f;
    private static final float DESCRIPTION_PITCH = 9f;
    private static final int DESCRIPTION_LINES = 2;
    private static final int ROW_RADIUS = 6;
    private static final int TOGGLE_WIDTH = 24;
    private static final int TOGGLE_HEIGHT = 12;
    private static final int GEAR_SIZE = 16;
    private static final int GEAR_ICON_SIZE = 11;
    private static final int SETTINGS_ICON = 0xe8b8;
    private static final int TOGGLE_ICON = 0xea18;
    private static final int HOLD_ICON = 0xe913;
    private static final int PREVIEW_ICON = 0xe89e;
    private static final int OVERLAY_INSET = 14;
    private static final int OVERLAY_PADDING = 6;
    private static final int OVERLAY_RADIUS = 6;
    private static final int OVERLAY_REVEAL_MILLIS = 150;
    private static final int SETTING_REVEAL_MILLIS = 180;
    private static final float SETTING_MIN_ZOOM = 0.9f;
    private static final int SETTING_ROW_HEIGHT = 22;
    private static final int SETTING_ROW_STRIDE = 24;
    private static final int BIND_WIDTH = 42;
    private static final int BIND_HEIGHT = 16;
    private static final int BIND_MODE_WIDTH = 36;
    private static final int FIELD_HEIGHT = 20;
    private static final int HELP_SIZE = 12;
    private static final int PICKER_INSET = 4;
    private static final int PREVIEW_BUTTON_SIZE = 16;
    private static final int SCROLL_MILLIS = 150;
    private static final int FADE_HEIGHT = 24;

    private final ClickGui gui;
    private final Supplier<Category> category;
    private final Transition overlayReveal = new Transition(0f, OVERLAY_REVEAL_MILLIS);
    private final Transition rowScrollShown = new Transition(0f, SCROLL_MILLIS);
    private final Transition settingScrollShown = new Transition(0f, SCROLL_MILLIS);
    private final List<Module> modules = new ArrayList<>();
    private final List<RowControl> rowControls = new ArrayList<>();
    private final List<Control> settingControls = new ArrayList<>();
    private final List<SettingRow> settingRows = new ArrayList<>();
    private final List<CatalogPicker> windows = new ArrayList<>();
    private PanelLayout layout;
    private Keybind bindControl;
    private Module openModule;
    private boolean shown;
    private boolean interactive;
    private boolean overlayOpen;
    private float rowScroll;
    private float settingScroll;

    private record RowControl(AbstractWidget widget, int row, int inset) {}

    private record SettingRow(Setting<?> setting, Control control, String label, Transition reveal) {
        float shown() {
            return reveal.value();
        }
    }

    @Override
    public void init(PanelLayout layout) {
        this.layout = layout;
        dismissRows();
        rowControls.clear();
        settingControls.clear();
        settingRows.clear();
        windows.clear();
        buildRows();
        if (openModule != null) buildSettings(openModule);
    }

    private void dismissRows() {
        for (RowControl control : rowControls) if (control.widget() instanceof Control widget) widget.dismiss();
    }

    public void reloadRows() {
        dismissRows();
        for (RowControl control : rowControls) gui.remove(control.widget());
        rowControls.clear();
        rowScroll = 0f;
        rowScrollShown.snap(0f);
        buildRows();
    }

    private void buildRows() {
        modules.clear();
        if (category.get() != null) modules.addAll(AdinClient.MODULES.in(category.get()));
        float scale = layout.scale();
        int rowHeight = rowHeight();
        int toggleWidth = layout.atLeastOne(TOGGLE_WIDTH);
        int toggleHeight = layout.atLeastOne(TOGGLE_HEIGHT);
        int toggleX = layout.right() - 2 * layout.padding() - toggleWidth;
        int gearSize = layout.atLeastOne(GEAR_SIZE);
        int iconSize = layout.scaled(GEAR_ICON_SIZE);
        int gearX = toggleX - layout.scaled(PanelLayout.GAP) - iconSize - (gearSize - iconSize) / 2;
        for (int row = 0; row < modules.size(); row++) {
            Module module = modules.get(row);
            int rowY = rowY(row);
            int toggleInset = (rowHeight - toggleHeight) / 2;
            rowControls.add(new RowControl(gui.add(new Bool(toggleX, rowY + toggleInset, toggleWidth, toggleHeight, scale,
                    Component.literal(module.name()), module::isEnabled, module::setEnabled)), row, toggleInset));
            int gearInset = (rowHeight - gearSize) / 2;
            boolean gear = !module.settings().isEmpty();
            if (gear) {
                rowControls.add(new RowControl(gui.add(Button.icon(gearX, rowY + gearInset, gearSize, scale, SETTINGS_ICON,
                        Component.literal(module.name() + " settings"), () -> openOverlay(module))), row, gearInset));
            }
            List<Videos.Clip> clips = Tooltips.current() == Tooltips.VIDEO ? Videos.of(module.id()) : List.<Videos.Clip>of();
            if (clips.isEmpty()) continue;
            int videoX = (gear ? gearX : toggleX) - layout.scaled(PanelLayout.GAP) - gearSize;
            VideoPopup video = new VideoPopup(videoX, rowY + gearInset, gearSize, scale, clips);
            video.setArea(layout.rowX(), layout.rowWidth());
            rowControls.add(new RowControl(gui.add(bounded(video)), row, gearInset));
        }
    }

    private float controlsLeft(int row) {
        float left = layout.rowX() + layout.rowWidth();
        for (RowControl control : rowControls) {
            if (control.row() == row) left = Math.min(left, control.widget().getX());
        }
        return left;
    }

    private void layoutRowControls() {
        for (RowControl control : rowControls) control.widget().setY(rowY(control.row()) + control.inset());
    }

    private int rowsSpan() {
        if (modules.isEmpty()) return 0;
        return 2 * layout.padding() + layout.scaled((modules.size() - 1) * ROW_STRIDE) + rowHeight();
    }

    private float maxRowScroll() {
        return Math.max(0f, rowsSpan() - layout.contentHeight());
    }

    private float maxSettingScroll() {
        return Math.max(0f, naturalOverlayHeight() - overlayHeight());
    }

    private ScreenRectangle contentArea() {
        return new ScreenRectangle(layout.contentX(), layout.contentY(), layout.contentWidth(), layout.contentHeight());
    }

    private ScreenRectangle overlayArea() {
        return new ScreenRectangle(overlayX(), overlayY(), overlayWidth(), overlayHeight());
    }

    private boolean within(AbstractWidget widget, ScreenRectangle area) {
        return widget.getY() >= area.top() && widget.getY() + widget.getHeight() <= area.bottom();
    }

    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (!shown || !interactive) return false;
        if (overlayOpen) {
            if (!inOverlay(x, y)) return false;
            settingScroll = Math.clamp((float) (settingScroll - dy * SETTING_ROW_STRIDE * layout.scale()), 0f, maxSettingScroll());
            settingScrollShown.set(settingScroll);
            return true;
        }
        if (!layout.inContent(x, y) || maxRowScroll() <= 0f) return false;
        rowScroll = Math.clamp((float) (rowScroll - dy * ROW_STRIDE * layout.scale()), 0f, maxRowScroll());
        rowScrollShown.set(rowScroll);
        return true;
    }

    private void buildSettings(Module module) {
        for (Control control : settingControls) gui.remove(control);
        for (SettingRow row : settingRows) gui.remove(row.control());
        settingControls.clear();
        settingRows.clear();
        windows.clear();
        float scale = layout.scale();
        int x = settingX();
        int width = settingWidth();
        int right = x + width;
        int halfX = x + width / 2;
        int halfWidth = right - halfX;
        int bindWidth = layout.atLeastOne(BIND_WIDTH);
        int bindHeight = layout.atLeastOne(BIND_HEIGHT);
        int modeWidth = layout.atLeastOne(BIND_MODE_WIDTH);
        int fieldHeight = layout.atLeastOne(FIELD_HEIGHT);
        int toggleWidth = layout.atLeastOne(TOGGLE_WIDTH);
        int toggleHeight = layout.atLeastOne(TOGGLE_HEIGHT);
        int previewSize = layout.atLeastOne(PREVIEW_BUTTON_SIZE);

        int modeX = right - modeWidth;
        int bindX = module.activatable() ? right - bindWidth : modeX - layout.scaled(PanelLayout.GAP) - bindWidth;
        bindControl = gui.add(new Keybind(bindX, settingY(0, bindHeight),
                bindWidth, bindHeight, scale, Component.literal(Lang.get("keybind")), module::key, module::setKey));
        settingControls.add(bindControl);
        if (!module.activatable()) {
            settingControls.add(gui.add(new Segmented(modeX, settingY(0, bindHeight), modeWidth, bindHeight, scale,
                    Component.literal("Bind mode"),
                    new Segmented.Segment[] {Segmented.Segment.of(TOGGLE_ICON), Segmented.Segment.of(HOLD_ICON)},
                    () -> module.hold() ? 1 : 0, index -> module.setHold(index == 1))));
            int helpSize = layout.atLeastOne(HELP_SIZE);
            settingControls.add(gui.add(new HelpDot(bindControl.getX() - layout.scaled(PanelLayout.GAP) - helpSize,
                    settingY(0, helpSize), helpSize, scale,
                    () -> Lang.get(module.hold() ? "bind.help.hold" : "bind.help.toggle"))));
        }

        for (Setting<?> setting : module.settings()) {
            Component label = Component.literal(setting.name());
            Control control = switch (setting) {
                case BoolSetting bool ->
                        new Bool(right - toggleWidth, 0, toggleWidth, toggleHeight, scale, label, bool::get, bool::set);
                case SliderSetting value -> {
                    Slider slider = new Slider(halfX, 0, halfWidth, bindHeight, scale, label,
                            value.min(), value.max(), value::get, value::set);
                    slider.setFormat(value::format);
                    yield slider;
                }
                case RangeSetting range -> {
                    Slider slider = Slider.range(halfX, 0, halfWidth, bindHeight, scale, label,
                            range.min(), range.max(), range::low, range::setLow, range::high, range::setHigh);
                    slider.setFormat(range::format);
                    yield slider;
                }
                case EnumSetting choice ->
                        bounded(Dropdown.single(x, 0, width, fieldHeight, scale, label, choice.options(), choice::get, choice::set));
                case MultiSetting multi -> {
                    Dropdown select = Dropdown.multi(x, 0, width, fieldHeight, scale, label, multi.options(), multi::get);
                    select.setOptionVisible(multi::isOptionVisible);
                    yield bounded(select);
                }
                case ColorSetting color -> {
                    ColorPicker picker = new ColorPicker(x, 0, width, fieldHeight, scale, label, color::get, color::set);
                    picker.gradient(color::secondary, color::setSecondary, color::isGradient);
                    yield bounded(picker);
                }
                case EntitySetting entities ->
                        windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, Catalog.ENTITIES, entities::get));
                case BlockSetting blocks ->
                        windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, Catalog.BLOCKS, blocks::get));
                case PreviewSetting preview ->
                        Button.icon(right - previewSize, 0, previewSize, scale, PREVIEW_ICON, label, () -> gui.openPreview(preview));
                case HotbarSetting hotbar -> {
                    int pickerWidth = HotbarPicker.preferredWidth(bindHeight, scale);
                    yield new HotbarPicker(right - pickerWidth, 0, pickerWidth, bindHeight, scale, label, hotbar::has, hotbar::toggle);
                }
                default -> null;
            };
            if (control == null) continue;
            String rowLabel = control instanceof Popup ? null : setting.name();
            Transition reveal = new Transition(setting.isVisible() ? 1f : 0f, SETTING_REVEAL_MILLIS);
            settingRows.add(new SettingRow(setting, gui.add(control), rowLabel, reveal));
        }
        layoutRows();
        relayout(layout);
    }

    private void animateRows() {
        if (openModule == null) return;
        for (SettingRow row : settingRows) row.reveal().set(row.setting().isVisible() ? 1f : 0f);
        layoutRows();
        setState(shown, interactive);
    }

    private void layoutRows() {
        settingScroll = Math.clamp(settingScroll, 0f, maxSettingScroll());
        settingScrollShown.set(settingScroll);
        for (Control control : settingControls) control.setY(settingY(0f, control.getHeight()));
        float offset = 1f;
        for (SettingRow row : settingRows) {
            Control control = row.control();
            control.setY(settingY(offset, control.getHeight()));
            offset += row.shown();
        }
    }

    private float rowSpan() {
        float span = 1f;
        for (SettingRow row : settingRows) span += row.shown();
        return span;
    }

    private <T extends Popup> T bounded(T popup) {
        popup.setBottomLimit(layout.bottom());
        return popup;
    }

    private CatalogPicker windowed(CatalogPicker picker) {
        windows.add(picker);
        return bounded(picker);
    }

    @Override
    public void relayout(PanelLayout layout) {
        this.layout = layout;
        for (RowControl control : rowControls) {
            if (control.widget() instanceof VideoPopup video) video.setArea(layout.rowX(), layout.rowWidth());
        }
        int inset = layout.scaled(PICKER_INSET);
        for (CatalogPicker window : windows) {
            window.setWindow(layout.contentX() + inset, layout.contentY() + inset,
                    layout.contentWidth() - 2 * inset, layout.contentHeight() - 2 * inset);
        }
    }

    private int rowHeight() {
        return layout.atLeastOne(ROW_HEIGHT);
    }

    private int rowY(int row) {
        return layout.contentY() + layout.padding() + layout.scaled(row * ROW_STRIDE) - Math.round(rowScrollShown.value());
    }

    private int overlayX() {
        return layout.contentX() + layout.scaled(OVERLAY_INSET);
    }

    private int overlayWidth() {
        return layout.contentWidth() - 2 * layout.scaled(OVERLAY_INSET);
    }

    private int naturalOverlayHeight() {
        return Math.round((2 * OVERLAY_PADDING + rowSpan() * SETTING_ROW_STRIDE) * layout.scale());
    }

    private int overlayHeight() {
        return Math.min(naturalOverlayHeight(), layout.contentHeight() - 2 * layout.scaled(OVERLAY_INSET));
    }

    private int overlayY() {
        return layout.contentY() + (layout.contentHeight() - overlayHeight()) / 2;
    }

    private int settingX() {
        return overlayX() + layout.padding();
    }

    private int settingWidth() {
        return overlayWidth() - 2 * layout.padding();
    }

    private int settingRowHeight() {
        return layout.atLeastOne(SETTING_ROW_HEIGHT);
    }

    private int settingRowY(float row) {
        return overlayY() + layout.scaled(OVERLAY_PADDING) + Math.round(row * SETTING_ROW_STRIDE * layout.scale()) - Math.round(settingScrollShown.value());
    }

    private int settingY(float row, int controlHeight) {
        return settingRowY(row) + (settingRowHeight() - controlHeight) / 2;
    }

    private boolean inOverlay(double pointX, double pointY) {
        return pointX >= overlayX() && pointX < overlayX() + overlayWidth()
                && pointY >= overlayY() && pointY < overlayY() + overlayHeight();
    }

    private void openOverlay(Module module) {
        openModule = module;
        settingScroll = 0f;
        settingScrollShown.snap(0f);
        buildSettings(module);
        setOverlayOpen(true);
    }

    public void closeOverlay() {
        setOverlayOpen(false);
    }

    private void setOverlayOpen(boolean open) {
        dismissRows();
        for (Control control : settingControls) control.dismiss();
        for (SettingRow row : settingRows) row.control().dismiss();
        overlayOpen = open;
        overlayReveal.set(open ? 1f : 0f);
        gui.dropFocus();
    }

    @Override
    public void reset() {
        closeOverlay();
    }

    @Override
    public void setState(boolean shown, boolean interactive) {
        this.shown = shown;
        this.interactive = interactive;
        float reveal = overlayReveal.value();
        boolean rows = shown && interactive && !overlayOpen && reveal == 0f;
        ScreenRectangle content = contentArea();
        for (RowControl control : rowControls) {
            control.widget().active = rows && within(control.widget(), content);
            control.widget().visible = shown;
        }
        boolean overlayReady = shown && interactive && overlayOpen && reveal == 1f;
        ScreenRectangle overlay = overlayArea();
        for (Control control : settingControls) {
            control.active = overlayReady && within(control, overlay);
            control.visible = shown && reveal > 0f;
        }
        for (SettingRow row : settingRows) {
            float rowShown = row.shown();
            row.control().active = overlayReady && rowShown >= 1f && within(row.control(), overlay);
            row.control().visible = shown && reveal > 0f && rowShown > 0f;
        }
    }

    @Override
    public List<Popup> popups() {
        List<Popup> popups = new ArrayList<>();
        for (RowControl control : rowControls) if (control.widget() instanceof Popup popup) popups.add(popup);
        for (Control control : settingControls) if (control instanceof Popup popup) popups.add(popup);
        for (SettingRow row : settingRows) if (row.control() instanceof Popup popup) popups.add(popup);
        return popups;
    }

    public boolean captureMouse(MouseButtonEvent event) {
        return bindControl != null && bindControl.captureMouse(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!shown || !interactive || !overlayOpen) return false;
        if (!layout.inContent(event.x(), event.y()) || inOverlay(event.x(), event.y())) return false;
        closeOverlay();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!shown || event.key() != GLFW.GLFW_KEY_ESCAPE) return false;
        if (!overlayOpen && overlayReveal.value() <= 0f) return false;
        closeOverlay();
        return true;
    }

    public float dimAmount() {
        return 1f - 0.6f * overlayReveal.value();
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        rowScrollShown.set(rowScroll);
        layoutRowControls();
        Scissor.clip(contentArea(), () -> {
            drawRows(graphics);
            for (RowControl control : rowControls) control.widget().extractRenderState(graphics, mouseX, mouseY, delta);
            drawFade(graphics);
        });
        for (RowControl control : rowControls) {
            if (control.widget() instanceof Popup popup) popup.renderPopup(graphics, mouseX, mouseY);
        }
    }

    private void drawFade(GuiGraphicsExtractor graphics) {
        int height = Math.min(layout.scaled(FADE_HEIGHT), layout.contentHeight());
        float remaining = maxRowScroll() - rowScrollShown.value();
        if (remaining <= 0f || height <= 0) return;
        int bottom = Colors.withAlpha(Theme.MAIN, Math.clamp(remaining / height, 0f, 1f));
        Draw.gradient(graphics, layout.contentX(), layout.bottom() - height, layout.contentWidth(), height,
                layout.atLeastOne(PanelLayout.RADIUS), Draw.BOTTOM_RIGHT, Colors.withAlpha(Theme.MAIN, 0f), bottom);
    }

    public void drawOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        animateRows();
        float reveal = overlayReveal.value();
        if (reveal <= 0f || openModule == null) return;
        graphics.nextStratum();
        Transform.popIn(graphics, overlayX() + overlayWidth() * 0.5f, overlayY() + overlayHeight() * 0.5f, reveal, 0.96f,
                () -> drawSettings(graphics, mouseX, mouseY, delta));
    }

    private void drawRows(GuiGraphicsExtractor graphics) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(ROW_RADIUS);
        for (int row = 0; row < modules.size(); row++) {
            Module module = modules.get(row);
            int y = rowY(row);
            Draw.rect(graphics, layout.rowX(), y, layout.rowWidth(), rowHeight(), radius, Theme.ROW);
            float textX = layout.rowX() + PanelLayout.PADDING * scale;
            String description = Tooltips.current() == Tooltips.VIDEO && !Videos.of(module.id()).isEmpty() ? "" : module.description();
            if (description.isEmpty()) {
                Text.drawCentered(graphics, module.name(), textX, y + rowHeight() * 0.5f, 8.5f * scale, Theme.TEXT);
                continue;
            }
            float textWidth = controlsLeft(row) - layout.scaled(PanelLayout.GAP) - textX;
            List<String> lines = Text.wrap(description, textWidth, DESCRIPTION_SIZE * scale, DESCRIPTION_LINES);
            if (lines.size() == 1) {
                Text.drawCentered(graphics, module.name(), textX, y + 13 * scale, 8.5f * scale, Theme.TEXT);
                Text.drawCentered(graphics, lines.getFirst(), textX, y + 27 * scale, DESCRIPTION_SIZE * scale, Theme.MUTED);
                continue;
            }
            Text.drawCentered(graphics, module.name(), textX, y + 10 * scale, 8.5f * scale, Theme.TEXT);
            for (int i = 0; i < lines.size(); i++) {
                Text.drawCentered(graphics, lines.get(i), textX, y + (21.5f + i * DESCRIPTION_PITCH) * scale,
                        DESCRIPTION_SIZE * scale, Theme.MUTED);
            }
        }
    }

    private void drawSettings(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(OVERLAY_RADIUS);
        Draw.bordered(graphics, overlayX(), overlayY(), overlayWidth(), overlayHeight(), radius, Theme.OVERLAY, Theme.POPUP_BORDER);
        Scissor.clip(overlayArea(), () -> {
            Text.drawCentered(graphics, Lang.get("keybind"), settingX(), settingRowY(0f) + settingRowHeight() * 0.5f, 8 * scale, Theme.TEXT);
            for (Control control : settingControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
            float offset = 1f;
            for (SettingRow row : settingRows) {
                float rowShown = row.shown();
                if (rowShown > 0f) drawRow(graphics, row, offset, rowShown, mouseX, mouseY, delta);
                offset += rowShown;
            }
        });
        for (Control control : settingControls) {
            if (control instanceof Popup popup) popup.renderPopup(graphics, mouseX, mouseY);
            if (control instanceof HelpDot help) help.renderTooltip(graphics);
        }
        for (SettingRow row : settingRows) {
            if (row.control() instanceof Popup popup && row.shown() >= 1f) popup.renderPopup(graphics, mouseX, mouseY);
        }
    }

    private void drawRow(GuiGraphicsExtractor graphics, SettingRow row, float offset, float rowShown,
                         int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        float centerY = settingRowY(offset) + settingRowHeight() * 0.5f;
        float centerX = settingX() + settingWidth() * 0.5f;
        Transform.popIn(graphics, centerX, centerY, rowShown, SETTING_MIN_ZOOM, () -> {
            if (row.label() != null) Text.drawCentered(graphics, row.label(), settingX(), centerY, 8 * scale, Theme.TEXT);
            row.control().extractRenderState(graphics, mouseX, mouseY, delta);
        });
    }
}
