package dev.koifih.client.gui.clickgui.page;

import dev.koifih.client.feature.Category;
import dev.koifih.client.feature.Feature;
import dev.koifih.client.feature.Features;
import dev.koifih.client.feature.setting.BlockSetting;
import dev.koifih.client.feature.setting.BoolSetting;
import dev.koifih.client.feature.setting.ColorSetting;
import dev.koifih.client.feature.setting.EntitySetting;
import dev.koifih.client.feature.setting.EnumSetting;
import dev.koifih.client.feature.setting.MultiSetting;
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
import dev.koifih.client.rendering.RectRenderer;
import dev.koifih.client.rendering.TextRenderer;
import dev.koifih.client.rendering.Draw;
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
    private static final int SETTING_ROW_HEIGHT = 22;
    private static final int SETTING_ROW_STRIDE = 24;
    private static final int BIND_WIDTH = 42;
    private static final int BIND_HEIGHT = 16;
    private static final int BIND_MODE_WIDTH = 36;
    private static final int FIELD_HEIGHT = 20;
    private static final int HELP_SIZE = 12;
    private static final int PICKER_INSET = 4;

    private final WidgetHost host;
    private final Supplier<Category> category;
    private final Transition overlayReveal = new Transition(0f, OVERLAY_REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
    private final List<Feature> features = new ArrayList<>();
    private final List<AbstractWidget> rowControls = new ArrayList<>();
    private final List<Control> settingControls = new ArrayList<>();
    private final List<String> settingLabels = new ArrayList<>();
    private final List<CatalogPicker> pickers = new ArrayList<>();
    private PanelLayout layout;
    private Keybind bindControl;
    private Feature openFeature;
    private boolean shown;
    private boolean interactive;
    private boolean overlayOpen;

    public ModulesPage(WidgetHost host, Supplier<Category> category) {
        this.host = host;
        this.category = category;
    }

    @Override
    public void init(PanelLayout layout) {
        this.layout = layout;
        rowControls.clear();
        settingControls.clear();
        settingLabels.clear();
        pickers.clear();
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
        if (category.get() != null) features.addAll(Features.in(category.get()));
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
        settingControls.clear();
        settingLabels.clear();
        pickers.clear();
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
        settingLabels.add(Lang.get("keybind"));

        int row = 1;
        for (Setting<?> setting : feature.settings()) {
            Component label = Component.literal(setting.name());
            Control control;
            String rowLabel = setting.name();
            if (setting instanceof BoolSetting bool) {
                control = new Bool(right - toggleWidth, settingY(row, toggleHeight), toggleWidth, toggleHeight, scale, label,
                        bool::get, bool::set);
            } else if (setting instanceof SliderSetting slider) {
                control = new Slider(halfX, settingY(row, bindHeight), halfWidth, bindHeight, scale, label,
                        slider.min(), slider.max(), slider::get, slider::set);
            } else if (setting instanceof RangeSetting range) {
                control = new RangeSlider(halfX, settingY(row, bindHeight), halfWidth, bindHeight, scale, label,
                        range.min(), range.max(), range::low, range::setLow, range::high, range::setHigh);
            } else if (setting instanceof EnumSetting choice) {
                control = bounded(new Dropdown(x, settingY(row, fieldHeight), width, fieldHeight, scale, label,
                        choice.options(), choice::get, choice::set));
                rowLabel = null;
            } else if (setting instanceof MultiSetting multi) {
                control = bounded(new MultiSelect(x, settingY(row, fieldHeight), width, fieldHeight, scale, label,
                        multi.options(), multi::get));
                rowLabel = null;
            } else if (setting instanceof ColorSetting color) {
                control = bounded(new ColorPicker(x, settingY(row, fieldHeight), width, fieldHeight, scale, label,
                        color::get, color::set));
                rowLabel = null;
            } else if (setting instanceof EntitySetting entities) {
                control = picker(new CatalogPicker(x, settingY(row, fieldHeight), width, fieldHeight, scale, label,
                        EntityCatalog.INSTANCE, entities::get));
                rowLabel = null;
            } else if (setting instanceof BlockSetting blocks) {
                control = picker(new CatalogPicker(x, settingY(row, fieldHeight), width, fieldHeight, scale, label,
                        BlockCatalog.INSTANCE, blocks::get));
                rowLabel = null;
            } else {
                continue;
            }
            settingControls.add(host.add(control));
            settingLabels.add(rowLabel);
            row++;
        }
        relayout(layout);
    }

    private <T extends Popup> T bounded(T popup) {
        popup.setBottomLimit(layout.bottom());
        return popup;
    }

    private CatalogPicker picker(CatalogPicker picker) {
        pickers.add(picker);
        return bounded(picker);
    }

    @Override
    public void relayout(PanelLayout layout) {
        this.layout = layout;
        int inset = layout.scaled(PICKER_INSET);
        for (CatalogPicker picker : pickers) {
            picker.setWindow(layout.contentX() + inset, layout.contentY() + inset,
                    layout.contentWidth() - 2 * inset, layout.contentHeight() - 2 * inset);
        }
    }

    private int rowHeight() {
        return layout.atLeastOne(ROW_HEIGHT);
    }

    private int rowY(int row) {
        return layout.contentY() + layout.padding() + layout.scaled(row * ROW_STRIDE);
    }

    private int settingRowCount() {
        return 1 + (openFeature == null ? 0 : openFeature.settings().size());
    }

    private int overlayX() {
        return layout.contentX() + layout.scaled(OVERLAY_INSET);
    }

    private int overlayWidth() {
        return layout.contentWidth() - 2 * layout.scaled(OVERLAY_INSET);
    }

    private int overlayHeight() {
        return layout.scaled(2 * OVERLAY_PADDING + settingRowCount() * SETTING_ROW_STRIDE);
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

    private int settingRowY(int row) {
        return overlayY() + layout.scaled(OVERLAY_PADDING) + layout.scaled(row * SETTING_ROW_STRIDE);
    }

    private int settingY(int row, int controlHeight) {
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
    }

    @Override
    public List<Popup> popups() {
        List<Popup> popups = new ArrayList<>();
        for (Control control : settingControls) if (control instanceof Popup popup) popups.add(popup);
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
        for (int row = 0; row < settingLabels.size(); row++) {
            String label = settingLabels.get(row);
            if (label == null) continue;
            TextRenderer.drawCentered(graphics, label, settingX(), settingRowY(row) + settingRowHeight() * 0.5f, 8 * scale, Theme.TEXT);
        }
        for (Control control : settingControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        for (Control control : settingControls) {
            if (control instanceof Popup popup) popup.renderPopup(graphics, mouseX, mouseY);
        }
    }
}
