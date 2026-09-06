package dev.koifih.client.gui.clickgui.page;

import dev.koifih.client.feature.Category;
import dev.koifih.client.feature.Feature;
import dev.koifih.client.feature.FeatureManager;
import dev.koifih.client.feature.setting.BlockSetting;
import dev.koifih.client.feature.setting.BoolSetting;
import dev.koifih.client.feature.setting.ColorSetting;
import dev.koifih.client.feature.setting.EntitySetting;
import dev.koifih.client.feature.setting.EnumSetting;
import dev.koifih.client.feature.setting.MultiSetting;
import dev.koifih.client.feature.setting.PreviewSetting;
import dev.koifih.client.feature.setting.RangeSetting;
import dev.koifih.client.feature.setting.Setting;
import dev.koifih.client.feature.setting.SliderSetting;
import dev.koifih.client.gui.Lang;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.gui.catalog.BlockCatalog;
import dev.koifih.client.gui.catalog.EntityCatalog;
import dev.koifih.client.gui.clickgui.PanelLayout;
import dev.koifih.client.gui.clickgui.WidgetHost;
import dev.koifih.client.gui.component.BindMode;
import dev.koifih.client.gui.component.Bool;
import dev.koifih.client.gui.component.CatalogPicker;
import dev.koifih.client.gui.component.ColorPicker;
import dev.koifih.client.gui.component.Control;
import dev.koifih.client.gui.component.Dropdown;
import dev.koifih.client.gui.component.HelpDot;
import dev.koifih.client.gui.component.IconButton;
import dev.koifih.client.gui.component.Keybind;
import dev.koifih.client.gui.component.MultiSelect;
import dev.koifih.client.gui.component.Popup;
import dev.koifih.client.gui.component.RangeSlider;
import dev.koifih.client.gui.component.Slider;
import dev.koifih.client.gui.component.Windowed;
import dev.koifih.client.rendering.Draw;
import dev.koifih.client.rendering.RectRenderer;
import dev.koifih.client.rendering.TextRenderer;
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
    private final List<Feature> features = new ArrayList<>();
    private final List<AbstractWidget> rowControls = new ArrayList<>();
    private final List<Control> settingControls = new ArrayList<>();
    private final List<SettingRow> settingRows = new ArrayList<>();
    private final List<Windowed> windows = new ArrayList<>();
    private PanelLayout layout;
    private Keybind bindControl;
    private Feature openFeature;
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
        if (openFeature != null) buildSettings(openFeature);
    }

    public void setCategory(Category selected) {
        for (AbstractWidget control : rowControls) host.remove(control);
        rowControls.clear();
        buildRows();
    }

    private void buildRows() {
        features.clear();
        if (category.get() != null) features.addAll(FeatureManager.in(category.get()));
        float scale = layout.scale();
        int rowHeight = rowHeight();
        int toggleWidth = layout.atLeastOne(TOGGLE_WIDTH);
        int toggleHeight = layout.atLeastOne(TOGGLE_HEIGHT);
        int toggleX = layout.right() - 2 * layout.padding() - toggleWidth;
        int gearSize = layout.atLeastOne(GEAR_SIZE);
        int iconSize = layout.scaled(GEAR_ICON_SIZE);
        int gearX = toggleX - layout.scaled(PanelLayout.GAP) - iconSize - (gearSize - iconSize) / 2;
        for (int row = 0; row < features.size(); row++) {
            Feature feature = features.get(row);
            int rowY = rowY(row);
            rowControls.add(host.add(new Bool(toggleX, rowY + (rowHeight - toggleHeight) / 2, toggleWidth, toggleHeight, scale,
                    Component.literal(feature.name()), feature::isEnabled, feature::setEnabled)));
            rowControls.add(host.add(new IconButton(gearX, rowY + (rowHeight - gearSize) / 2, gearSize, scale, SETTINGS_ICON,
                    Component.literal(feature.name() + " settings"), () -> openOverlay(feature))));
        }
    }

    private void buildSettings(Feature feature) {
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
        bindControl = host.add(new Keybind(modeX - layout.scaled(PanelLayout.GAP) - bindWidth, settingY(0, bindHeight),
                bindWidth, bindHeight, scale, Component.literal(Lang.get("keybind")), feature::key, feature::setKey));
        settingControls.add(bindControl);
        settingControls.add(host.add(new BindMode(modeX, settingY(0, bindHeight), modeWidth, bindHeight, scale,
                feature::hold, feature::setHold)));
        int helpSize = layout.atLeastOne(HELP_SIZE);
        settingControls.add(host.add(new HelpDot(bindControl.getX() - layout.scaled(PanelLayout.GAP) - helpSize,
                settingY(0, helpSize), helpSize, scale,
                () -> Lang.get(feature.hold() ? "bind.help.hold" : "bind.help.toggle"))));

        for (Setting<?> setting : feature.settings()) {
            Component label = Component.literal(setting.name());
            Control control;
            String rowLabel = setting.name();
            if (setting instanceof BoolSetting bool) {
                control = new Bool(right - toggleWidth, 0, toggleWidth, toggleHeight, scale, label, bool::get, bool::set);
            } else if (setting instanceof SliderSetting slider) {
                Slider sliderControl = new Slider(halfX, 0, halfWidth, bindHeight, scale, label,
                        slider.min(), slider.max(), slider::get, slider::set);
                sliderControl.setFormat(slider::format);
                control = sliderControl;
            } else if (setting instanceof RangeSetting range) {
                control = new RangeSlider(halfX, 0, halfWidth, bindHeight, scale, label,
                        range.min(), range.max(), range::low, range::setLow, range::high, range::setHigh);
            } else if (setting instanceof EnumSetting choice) {
                control = bounded(new Dropdown(x, 0, width, fieldHeight, scale, label, choice.options(), choice::get, choice::set));
                rowLabel = null;
            } else if (setting instanceof MultiSetting multi) {
                MultiSelect select = new MultiSelect(x, 0, width, fieldHeight, scale, label, multi.options(), multi::get);
                select.setOptionVisible(multi::isOptionVisible);
                control = bounded(select);
                rowLabel = null;
            } else if (setting instanceof ColorSetting color) {
                control = bounded(new ColorPicker(x, 0, width, fieldHeight, scale, label, color::get, color::set));
                rowLabel = null;
            } else if (setting instanceof EntitySetting entities) {
                control = windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, EntityCatalog.INSTANCE, entities::get));
                rowLabel = null;
            } else if (setting instanceof BlockSetting blocks) {
                control = windowed(new CatalogPicker(x, 0, width, fieldHeight, scale, label, BlockCatalog.INSTANCE, blocks::get));
                rowLabel = null;
            } else if (setting instanceof PreviewSetting preview) {
                control = new IconButton(right - previewSize, 0, previewSize, scale, PREVIEW_ICON, label, () -> host.openPreview(preview));
            } else {
                continue;
            }
            Transition reveal = new Transition(setting.isVisible() ? 1f : 0f, SETTING_REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
            settingRows.add(new SettingRow(setting, host.add(control), rowLabel, reveal));
        }
        layoutRows();
        relayout(layout);
    }

    private void animateRows() {
        if (openFeature == null) return;
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

    private void openOverlay(Feature feature) {
        openFeature = feature;
        buildSettings(feature);
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
        if (reveal <= 0f || openFeature == null) return;
        graphics.nextStratum();
        Draw.popIn(graphics, overlayX() + overlayWidth() * 0.5f, overlayY() + overlayHeight() * 0.5f, reveal, 0.96f,
                () -> drawSettings(graphics, mouseX, mouseY, delta));
    }

    private void drawRows(GuiGraphicsExtractor graphics) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(ROW_RADIUS);
        for (int row = 0; row < features.size(); row++) {
            Feature feature = features.get(row);
            int y = rowY(row);
            RectRenderer.draw(graphics, layout.rowX(), y, layout.rowWidth(), rowHeight(), radius, Theme.ROW);
            float textX = layout.rowX() + PanelLayout.PADDING * scale;
            String description = feature.description();
            if (description.isEmpty()) {
                TextRenderer.drawCentered(graphics, feature.name(), textX, y + rowHeight() * 0.5f, 8.5f * scale, Theme.TEXT);
            } else {
                TextRenderer.drawCentered(graphics, feature.name(), textX, y + 13 * scale, 8.5f * scale, Theme.TEXT);
                TextRenderer.drawCentered(graphics, description, textX, y + 27 * scale, 6.8f * scale, Theme.MUTED);
            }
        }
    }

    private void drawSettings(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = layout.scale();
        int radius = layout.atLeastOne(OVERLAY_RADIUS);
        Draw.borderedBox(graphics, overlayX(), overlayY(), overlayWidth(), overlayHeight(), radius, Theme.OVERLAY);
        TextRenderer.drawCentered(graphics, Lang.get("keybind"), settingX(), settingRowY(0f) + settingRowHeight() * 0.5f, 8 * scale, Theme.TEXT);
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
        Draw.popIn(graphics, centerX, centerY, rowShown, SETTING_MIN_ZOOM, () -> {
            if (row.label() != null) TextRenderer.drawCentered(graphics, row.label(), settingX(), centerY, 8 * scale, Theme.TEXT);
            row.control().extractRenderState(graphics, mouseX, mouseY, delta);
        });
    }
}
