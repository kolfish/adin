package dev.koifih.client.ui.clickgui;

import dev.koifih.client.input.Keybinds;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Scissor;
import dev.koifih.client.render.Transform;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.ui.component.Control;
import dev.koifih.client.ui.component.Popup;
import dev.koifih.client.ui.component.TextInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public final class ClickGui extends Screen {
    private static final int PANEL_REVEAL_MILLIS = 200;
    private static final int PAGE_REVEAL_MILLIS = 150;

    private static Sidebar.Tab selectedTab = Sidebar.TABS.get(0);

    private final Transition panelReveal = new Transition(0f, PANEL_REVEAL_MILLIS);
    private final Transition pageReveal = new Transition(1f, PAGE_REVEAL_MILLIS);
    private final Sidebar sidebar = new Sidebar(this, this::selectTab, this::openMenu);
    private final SettingsMenu menu = new SettingsMenu(this);
    private final PreviewWindow preview = new PreviewWindow(this);
    private final ModulesPage modulesPage = new ModulesPage(this, () -> selectedTab.category());
    private final ConfigsPage configsPage = new ConfigsPage(this);
    private final List<Page> pages = List.of(modulesPage, configsPage);
    private final float uiScale = UiScale.current().factor();
    private PanelLayout layout;
    private boolean rebuildPending;
    private boolean closing;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean draggingPanel;
    private double grabX;
    private double grabY;

    public ClickGui() {
        super(Component.translatable("screen.adin.click_gui"));
        panelReveal.set(1f);
    }

    <T extends AbstractWidget> T add(T widget) {
        return addRenderableWidget(widget);
    }

    void remove(AbstractWidget widget) {
        removeWidget(widget);
    }

    void requestRebuild() {
        rebuildPending = true;
    }

    void dropFocus() {
        clearFocus();
    }

    void openPreview(PreviewSetting setting) {
        preview.open(setting);
        updateStates();
    }

    @Override
    protected void init() {
        layout = PanelLayout.of(width, height, dragOffsetX, dragOffsetY, uiScale);
        sidebar.init(layout, () -> selectedTab);
        for (Page page : pages) page.init(layout);
        menu.init(layout);
        preview.init(layout);
        updateStates();
    }

    private Page currentPage() {
        return selectedTab.category() == null ? configsPage : modulesPage;
    }

    private void selectTab(Sidebar.Tab tab) {
        for (Page page : pages) page.reset();
        preview.close();
        if (selectedTab != tab) {
            pageReveal.snap(0f);
            pageReveal.set(1f);
        }
        selectedTab = tab;
        modulesPage.reloadRows();
        updateStates();
    }

    private void openMenu() {
        for (Page page : pages) page.reset();
        preview.close();
        menu.open();
        updateStates();
    }

    private void closeMenu() {
        menu.close();
        updateStates();
    }

    private void updateStates() {
        boolean interactive = menu.isHidden();
        sidebar.setActive(interactive);
        modulesPage.setState(selectedTab.category() != null, interactive);
        configsPage.setState(selectedTab.category() == null, interactive);
        menu.updateStates();
        preview.updateStates(interactive);
    }

    private List<Popup> popups() {
        List<Popup> popups = new ArrayList<>(menu.popups());
        for (Page page : pages) popups.addAll(page.popups());
        return popups;
    }

    @Override
    public void onClose() {
        if (closing) return;
        closing = true;
        panelReveal.set(0f);
    }

    @Override
    public void tick() {
        if (closing && panelReveal.value() <= 0f) super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Theme.update();
        if (rebuildPending) {
            rebuildPending = false;
            rebuildWidgets();
        }
        float reveal = panelReveal.value();
        if (reveal <= 0f) return;
        float centerX = layout.x() + layout.width() * 0.5f;
        float centerY = layout.y() + layout.height() * 0.5f;
        Transform.popIn(graphics, centerX, centerY, reveal, 0.96f, () -> {
            drawPanel(graphics);
            drawContent(graphics, mouseX, mouseY, delta);
            sidebar.draw(graphics, layout, selectedTab, mouseX, mouseY, delta);
            preview.draw(graphics, modulesPage.highlightedSetting(mouseX, mouseY), mouseX, mouseY, delta);
            menu.draw(graphics, mouseX, mouseY, delta);
        });
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int radius = layout.atLeastOne(PanelLayout.RADIUS);
        Draw.rect(graphics, layout.x(), layout.y(), layout.width(), layout.height(), radius, Theme.SIDEBAR);
        Draw.rect(graphics, layout.contentX(), layout.contentY(), layout.contentWidth(), layout.contentHeight(),
                radius, Theme.MAIN);
        int join = Math.min(radius + 1, Math.min(layout.contentWidth(), layout.contentHeight()));
        Draw.rect(graphics, layout.right() - join, layout.contentY(), join, join, 0, Theme.MAIN);
        Draw.rect(graphics, layout.contentX(), layout.bottom() - join, join, join, 0, Theme.MAIN);
    }

    private void drawContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        updateStates();
        Page page = currentPage();
        float shown = pageReveal.value();
        float dim = page == modulesPage ? modulesPage.dimAmount() : 1f;
        var clip = new ScreenRectangle(layout.contentX(), layout.contentY(), layout.contentWidth(), layout.contentHeight());
        Scissor.clip(clip, () -> {
            Opacity.with(shown * dim, () -> Transform.translated(graphics, 0f, 6 * layout.scale() * (1f - shown),
                    () -> page.draw(graphics, mouseX, mouseY, delta)));
            if (page == modulesPage) modulesPage.drawOverlay(graphics, mouseX, mouseY, delta);
        });
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (closing) return true;
        if (getFocused() instanceof Control control && control.capturesInput() && control.keyPressed(event)) return true;
        boolean typing = getFocused() instanceof TextInput input && input.capturesInput();
        if (!typing && Keybinds.OPEN_CLICK_GUI.matches(event)) {
            onClose();
            return true;
        }
        if (preview.keyPressed(event)) return true;
        for (Page page : pages) if (page.keyPressed(event)) return true;
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && menu.isOpen()) {
            closeMenu();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return true;
        if (modulesPage.captureMouse(event) || menu.captureMouse(event)) return true;
        for (Popup popup : popups()) {
            if (popup.isOpen() && popup.mouseClicked(event, doubleClick)) {
                setFocused(popup);
                setDragging(event.button() == 0);
                return true;
            }
        }
        if (Keybinds.OPEN_CLICK_GUI.matchesMouse(event)) {
            onClose();
            return true;
        }
        if (menu.isOpen()) {
            if (menu.contains(event.x(), event.y())) return super.mouseClicked(event, doubleClick);
            closeMenu();
            return true;
        }
        if (preview.mouseClicked(event)) return true;
        for (Page page : pages) if (page.mouseClicked(event, doubleClick)) return true;
        if (event.button() == 0 && layout.inTopBar(event.x(), event.y())) {
            draggingPanel = true;
            grabX = event.x() - layout.x();
            grabY = event.y() - layout.y();
            clearFocus();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (preview.mouseDragged(event)) return true;
        if (!draggingPanel || event.button() != 0) return super.mouseDragged(event, dx, dy);
        PanelLayout before = layout;
        int x = Math.clamp((int) Math.round(event.x() - grabX), 0, Math.max(0, width - before.width()));
        int y = Math.clamp((int) Math.round(event.y() - grabY), 0, Math.max(0, height - before.height()));
        dragOffsetX = x - (width - before.width()) / 2;
        dragOffsetY = y - (height - before.height()) / 2;
        for (var child : children()) {
            if (child instanceof AbstractWidget widget) {
                widget.setX(widget.getX() + x - before.x());
                widget.setY(widget.getY() + y - before.y());
            }
        }
        layout = PanelLayout.of(width, height, dragOffsetX, dragOffsetY, uiScale);
        for (Popup popup : popups()) popup.setBottomLimit(layout.bottom());
        for (Page page : pages) page.relayout(layout);
        menu.relayout(layout);
        preview.relayout(layout);
        sidebar.resetPill();
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        for (Popup popup : popups()) {
            if (popup.isOpen() && popup.mouseScrolled(x, y, dx, dy)) return true;
        }
        if (currentPage().mouseScrolled(x, y, dx, dy)) return true;
        return super.mouseScrolled(x, y, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (preview.mouseReleased(event)) return true;
        if (draggingPanel && event.button() == 0) {
            draggingPanel = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
