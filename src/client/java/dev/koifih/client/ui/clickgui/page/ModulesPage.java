package dev.koifih.client.ui.clickgui.page;

import dev.koifih.client.AdinClient;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.setting.BlockSetting;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EntitySetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.MultiSetting;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.setting.RangeSetting;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
import dev.koifih.client.ui.catalog.BlockCatalog;
import dev.koifih.client.ui.catalog.EntityCatalog;
import dev.koifih.client.ui.clickgui.PanelLayout;
import dev.koifih.client.ui.clickgui.WidgetHost;
import dev.koifih.client.ui.component.BindMode;
import dev.koifih.client.ui.component.Bool;
import dev.koifih.client.ui.component.CatalogPicker;
import dev.koifih.client.ui.component.ColorPicker;
import dev.koifih.client.ui.component.Control;
import dev.koifih.client.ui.component.Dropdown;
import dev.koifih.client.ui.component.HelpDot;
import dev.koifih.client.ui.component.IconButton;
import dev.koifih.client.ui.component.Keybind;
import dev.koifih.client.ui.component.MultiSelect;
import dev.koifih.client.ui.component.Popup;
import dev.koifih.client.ui.component.RangeSlider;
import dev.koifih.client.ui.component.Slider;
import dev.koifih.client.ui.component.Windowed;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModulesPage implements Page {
    private static final int ROW_HEIGHT = 40;
    private static final int ROW_STRIDE = 46;
    private static final int ROW_RADIUS = 6;
    private static final int TOGGLE_WIDTH = 24;
    private static final int TOGGLE_HEIGHT = 12;
    private static final int GEAR_SIZE = 16;
    private static final int GEAR_ICON_SIZE = 11;
    private static final int SETTINGS_ICON = 0xe8b8;
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
    private static final int PREVIEW_ICON = 0xe89e;

    private final WidgetHost host;
    private final Supplier<Category> category;
    private final Transition overlayReveal = new Transition(0f, OVERLAY_REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
    private final List<Module> modules = new ArrayList<>();
    private final List<AbstractWidget> rowControls = new ArrayList<>();
    private final List<Control> settingControls = new ArrayList<>();
    private final List<SettingRow> settingRows = new ArrayList<>();
    private final List<Windowed> windows = new ArrayList<>();
    private PanelLayout layout;
    private Keybind bindControl;
    private Module openModule;
    private boolean shown;
    private boolean interactive;
    private boolean overlayOpen;

    private record SettingRow(Setting<?> setting, Control control, String label, Transition reveal) {
        float shown() {
            return reveal.value();
        }
    }

    public ModulesPage(WidgetHost host, Supplier<Category> category) {
        this.host = host;
        this.category = category;
    }

    @Override
    public void init(PanelLayout layout) {
        this.layout = layout;
        rowControls.clear();
        settingControls.clear();
        settingRows.clear();
        windows.clear();
        buildRows();
        if (openModule != null) buildSettings(openModule);
    }

    public void reloadRows() {
        for (AbstractWidget control : rowControls) host.remove(control);
        rowControls.clear();
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
            rowControls.add(host.add(new Bool(toggleX, rowY + (rowHeight - toggleHeight) / 2, toggleWidth, toggleHeight, scale,
                    Component.literal(module.name()), module::isEnabled, module::setEnabled)));
            if (module.settings().isEmpty()) continue;
            rowControls.add(host.add(new IconButton(gearX, rowY + (rowHeight - gearSize) / 2, gearSize, scale, SETTINGS_ICON,
                    Component.literal(module.name() + " settings"), () -> openOverlay(module))));
        }
    }

    private void buildSettings(Module module) {
        for (Control control : settingControls) host.remove(control);
        for (SettingRow row : settingRows) host.remove(row.control());
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
        bindControl = host.add(new Keybind(bindX, settingY(0, bindHeight),
                bindWidth, bindHeight, scale, Component.literal(Lang.get("keybind")), module::key, module::setKey));
        settingControls.add(bindControl);
        if (!module.activatable()) {
            settingControls.add(host.add(new BindMode(modeX, settingY(0, bindHeight), modeWidth, bindHeight, scale,
                    module::hold, module::setHold)));
            int helpSize = layout.atLeastOne(HELP_SIZE);
            settingControls.add(host.add(new HelpDot(bindControl.getX() - layout.scaled(PanelLayout.GAP) - helpSize,
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
                    RangeSlider slider = new RangeSlider(halfX, 0, halfWidth, bindHeight, scale, label,
                            range.min(), range.max(), range::low, range::setLow, range::high, range::setHigh);
                    slider.setFormat(range::format);
                    yield slider;
                }
                case EnumSetting choice ->
                        bounded(new Dropdown(x, 0, width, fieldHeight, scale, label, choice.options(), choice::get, choice::set));
                case MultiSetting multi -> {
                    MultiSelect select = new MultiSelect(x, 0, width, fieldHeight, scale, label, multi.options(), multi::get);
                    select.setOptionVisible(multi::isOptionVisible);
                    yield bounded(select);
                }
                case ColorSetting color -> {
                    ColorPicker picker = new ColorPicker(x, 0, width, fieldHeight, scale, label, color::get, color::set);
                    picker.gradient(color::secondary, color::setSecondary, color::isGradient);
                    yield bounded(picker);
                }
                case EntitySetting entities ->
                        windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, EntityCatalog.INSTANCE, entities::get));
                case BlockSetting blocks ->
                        windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, BlockCatalog.INSTANCE, blocks::get));
                case PreviewSetting preview ->
                        new IconButton(right - previewSize, 0, previewSize, scale, PREVIEW_ICON, label, () -> host.openPreview(preview));
                default -> null;
            };
            if (control == null) continue;
            String rowLabel = control instanceof Popup ? null : setting.name();
            Transition reveal = new Transition(setting.isVisible() ? 1f : 0f, SETTING_REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
            settingRows.add(new SettingRow(setting, host.add(control), rowLabel, reveal));
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

    private <T extends Popup & Windowed> T windowed(T popup) {
        windows.add(popup);
        return bounded(popup);
    }

    @Override
    public void relayout(PanelLayout layout) {
        this.layout = layout;
        int inset = layout.scaled(PICKER_INSET);
        for (Windowed window : windows) {
            window.setWindow(layout.contentX() + inset, layout.contentY() + inset,
                    layout.contentWidth() - 2 * inset, layout.contentHeight() - 2 * inset);
        }
    }

    private int rowHeight() {
        return layout.atLeastOne(ROW_HEIGHT);
    }

    private int rowY(int row) {
        return layout.contentY() + layout.padding() + layout.scaled(row * ROW_STRIDE);
    }

    private int overlayX() {
        return layout.contentX() + layout.scaled(OVERLAY_INSET);
    }

    private int overlayWidth() {
        return layout.contentWidth() - 2 * layout.scaled(OVERLAY_INSET);
    }

    private int overlayHeight() {
        return Math.round((2 * OVERLAY_PADDING + rowSpan() * SETTING_ROW_STRIDE) * layout.scale());
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
        return overlayY() + layout.scaled(OVERLAY_PADDING) + Math.round(row * SETTING_ROW_STRIDE * layout.scale());
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
        buildSettings(module);
        setOverlayOpen(true);
    }

    public void closeOverlay() {
        setOverlayOpen(false);
    }

    private void setOverlayOpen(boolean open) {
        for (Control control : settingControls) control.dismiss();
        for (SettingRow row : settingRows) row.control().dismiss();
        overlayOpen = open;
        overlayReveal.set(open ? 1f : 0f);
        host.dropFocus();
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
        for (AbstractWidget control : rowControls) {
            control.active = rows;
            control.visible = shown;
        }
        boolean overlayReady = shown && interactive && overlayOpen && reveal == 1f;
        for (Control control : settingControls) {
            control.active = overlayReady;
            control.visible = shown && reveal > 0f;
        }
        for (SettingRow row : settingRows) {
            float rowShown = row.shown();
            row.control().active = overlayReady && rowShown >= 1f;
            row.control().visible = shown && reveal > 0f && rowShown > 0f;
        }
    }

    @Override
    public List<Popup> popups() {
        List<Popup> popups = new ArrayList<>();
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

    public Setting<?> highlightedSetting(double mouseX, double mouseY) {
        for (SettingRow row : settingRows) {
            if (row.control() instanceof Popup popup && popup.isOpen()) return row.setting();
        }
        for (SettingRow row : settingRows) {
            Control control = row.control();
            if (control.visible && control.active && control.isMouseOver(mouseX, mouseY)) return row.setting();
        }
        return null;
    }

    public float dimAmount() {
        return 1f - 0.6f * overlayReveal.value();
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawRows(graphics);
        for (AbstractWidget control : rowControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
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
            String description = module.description();
            if (description.isEmpty()) {
                Text.drawCentered(graphics, module.name(), textX, y + rowHeight() * 0.5f, 8.5f * scale, Theme.TEXT);
            } else {
                Text.drawCentered(graphics, module.name(), textX, y + 13 * scale, 8.5f * scale, Theme.TEXT);
                Text.drawCentered(graphics, description, textX, y + 27 * scale, 6.8f * scale, Theme.MUTED);
            }
        }
    }

    private void drawSettings(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(OVERLAY_RADIUS);
        Draw.bordered(graphics, overlayX(), overlayY(), overlayWidth(), overlayHeight(), radius, Theme.OVERLAY, Theme.POPUP_BORDER);
        Text.drawCentered(graphics, Lang.get("keybind"), settingX(), settingRowY(0f) + settingRowHeight() * 0.5f, 8 * scale, Theme.TEXT);
        for (Control control : settingControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        float offset = 1f;
        for (SettingRow row : settingRows) {
            float rowShown = row.shown();
            if (rowShown > 0f) drawRow(graphics, row, offset, rowShown, mouseX, mouseY, delta);
            offset += rowShown;
        }
        for (Control control : settingControls) {
            if (control instanceof Popup popup) popup.renderPopup(graphics, mouseX, mouseY);
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
