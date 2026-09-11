package dev.koifih.client.ui.clickgui;

import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Point;
import dev.koifih.client.render.Rect;
import dev.koifih.client.render.Scissor;
import dev.koifih.client.render.Style;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.render.entity.EntityFill;
import dev.koifih.client.render.screen.Boxes;
import dev.koifih.client.render.screen.HealthBar;
import dev.koifih.client.render.screen.Projector;
import dev.koifih.client.render.screen.ScreenBuffer;
import dev.koifih.client.render.screen.ScreenRenderer;
import dev.koifih.client.setting.PreviewSetting;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.component.Button;
import dev.koifih.client.ui.component.TextInput;
import dev.koifih.client.ui.EntityPreview;
import dev.koifih.client.ui.SkinCache;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Lang;
import dev.koifih.client.util.Time;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class PreviewWindow {
    private static final int WIDTH = 150;
    private static final int BACK_ICON = 0xe5c4;
    private static final int BACK_SIZE = 14;
    private static final int REVEAL_MILLIS = 180;
    private static final float SLIDE = 14f;
    private static final float DEGREES_PER_PIXEL = 1.5f;
    private static final float CLICK_STEP = 45f;
    private static final float DRAG_THRESHOLD = 3f;
    private static final float DEFAULT_YAW = -30f;
    private static final String DEFAULT_SKIN = "cms3066";
    private static final int NAME_MAX_LENGTH = 16;
    private static final int INPUT_HEIGHT = 16;
    private static final int STATUS_HEIGHT = 10;
    private static final long FETCH_DELAY_MILLIS = 600L;
    private static String skinName = DEFAULT_SKIN;
    private final ClickGui gui;
    private final Transition reveal = new Transition(0f, REVEAL_MILLIS);
    private final Transition yaw = new Transition(DEFAULT_YAW, 200);
    private PanelLayout layout;
    private Button back;
    private TextInput nameInput;
    private PreviewSetting preview;
    private Setting<?> highlighted;
    private String requestedName = "";
    private final Time.Stopwatch sinceEdit = new Time.Stopwatch();
    private boolean open;
    private boolean dragging;
    private boolean dragged;
    private double pressX;
    private double lastX;

    public PreviewWindow(ClickGui gui) {
        this.gui = gui;
    }

    public void init(PanelLayout layout) {
        this.layout = layout;
        back = gui.add(Button.icon(backX(), backY(), layout.atLeastOne(BACK_SIZE), layout.scale(), BACK_ICON,
                Component.literal("Back"), this::close));
        nameInput = gui.add(new TextInput(contentX(), inputY(), contentWidth(), layout.atLeastOne(INPUT_HEIGHT), layout.scale(),
                Component.literal(Lang.get("preview.skin")), NAME_MAX_LENGTH, () -> skinName, this::setSkinName));
        nameInput.setPlaceholder(Lang.get("preview.skin"));
        SkinCache.request(skinName);
        requestedName = skinName;
        updateStates(true);
    }

    public void relayout(PanelLayout layout) {
        this.layout = layout;
        back.setX(backX());
        back.setY(backY());
        nameInput.setX(contentX());
        nameInput.setY(inputY());
        nameInput.setWidth(contentWidth());
    }

    private void setSkinName(String name) {
        skinName = name;
        sinceEdit.reset();
    }

    private void pollSkin() {
        if (skinName.equals(requestedName) || !sinceEdit.elapsed(FETCH_DELAY_MILLIS)) return;
        requestedName = skinName;
        SkinCache.request(skinName);
    }

    private PlayerSkin currentSkin() {
        return requestedName.isBlank() ? null : SkinCache.skin(requestedName);
    }

    private String statusText() {
        if (requestedName.isBlank()) return "";
        return switch (SkinCache.status(requestedName)) {
            case LOADING -> Lang.get("preview.loading");
            case MISSING -> Lang.get("preview.missing");
            case READY -> "";
        };
    }

    public void open(PreviewSetting preview) {
        this.preview = preview;
        open = true;
        reveal.set(1f);
        gui.dropFocus();
    }

    public void close() {
        open = false;
        dragging = false;
        reveal.set(0f);
        gui.dropFocus();
    }

    public boolean isOpen() {
        return open;
    }

    public void updateStates(boolean interactive) {
        boolean ready = open && interactive && reveal.value() >= 1f;
        back.active = ready;
        back.visible = reveal.value() > 0f;
        nameInput.active = ready;
        nameInput.visible = reveal.value() > 0f;
    }

    private boolean onRight() {
        return layout.right() + layout.scaled(PanelLayout.GAP) + width() <= Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    private int width() {
        return layout.scaled(WIDTH);
    }

    private int x() {
        int gap = layout.scaled(PanelLayout.GAP);
        return onRight() ? layout.right() + gap : layout.x() - gap - width();
    }

    private int y() {
        return layout.y();
    }

    private int height() {
        return layout.height();
    }

    private int backX() {
        return x() + (layout.topBarHeight() - layout.atLeastOne(BACK_SIZE)) / 2 + layout.scaled(2);
    }

    private int backY() {
        return y() + (layout.topBarHeight() - layout.atLeastOne(BACK_SIZE)) / 2;
    }

    private int contentX() {
        return x() + layout.padding();
    }

    private int inputY() {
        return y() + layout.topBarHeight() + layout.padding();
    }

    private int contentY() {
        return inputY() + layout.atLeastOne(INPUT_HEIGHT) + layout.scaled(STATUS_HEIGHT);
    }

    private int contentWidth() {
        return width() - 2 * layout.padding();
    }

    private int contentHeight() {
        return y() + height() - layout.padding() - contentY();
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x() && pointX < x() + width() && pointY >= y() && pointY < y() + height();
    }

    private boolean inContent(double pointX, double pointY) {
        return pointX >= contentX() && pointX < contentX() + contentWidth()
                && pointY >= contentY() && pointY < contentY() + contentHeight();
    }

    public void draw(GuiGraphicsExtractor graphics, Setting<?> highlighted, int mouseX, int mouseY, float delta) {
        this.highlighted = highlighted;
        float shown = reveal.value();
        if (shown <= 0f || preview == null) return;
        float slide = (onRight() ? -1f : 1f) * SLIDE * layout.scale() * (1f - shown);
        Opacity.with(shown, () -> Transform.translated(graphics, slide, 0f, () -> drawWindow(graphics, mouseX, mouseY, delta)));
    }

    private void drawWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int radius = layout.atLeastOne(PanelLayout.RADIUS);
        Draw.rect(graphics, x(), y(), width(), height(), radius, Theme.SIDEBAR);
        Draw.rect(graphics, x(), y() + layout.topBarHeight(), width(), height() - layout.topBarHeight(), radius, Theme.MAIN);
        Draw.rect(graphics, x(), y() + layout.topBarHeight(), width(), radius, 0, Theme.MAIN);
        float titleX = back.getX() + back.getWidth() + layout.scaled(PanelLayout.GAP);
        Text.drawCentered(graphics, preview.name(), titleX, y() + layout.topBarHeight() * 0.5f, 8 * layout.scale(), Theme.TEXT);
        back.extractRenderState(graphics, mouseX, mouseY, delta);
        pollSkin();
        nameInput.extractRenderState(graphics, mouseX, mouseY, delta);
        String status = statusText();
        if (!status.isEmpty()) {
            Text.drawCentered(graphics, status, contentX() + layout.scaled(2), inputY() + layout.atLeastOne(INPUT_HEIGHT) + layout.scaled(STATUS_HEIGHT) * 0.5f,
                    6.5f * layout.scale(), Theme.MUTED);
        }
        if (!open) return;
        var clip = new ScreenRectangle(contentX(), contentY(), contentWidth(), contentHeight());
        Scissor.clip(clip, () -> drawModel(graphics));
    }

    private void drawModel(GuiGraphicsExtractor graphics) {
        int x0 = contentX();
        int y0 = contentY();
        int x1 = x0 + contentWidth();
        int y1 = y0 + contentHeight();
        float angle = yaw.value();
        Projector projector = EntityPreview.playerProjector(x0, y0, x1, y1, angle);
        AABB bounds = EntityPreview.playerLocalBounds();
        if (projector == null || bounds == null) return;
        PreviewSetting.Shade shade = preview.shade(highlighted);
        if (shade != null) {
            EntityPreview.drawPlayer(graphics, x0, y0, x1, y1, angle, currentSkin(), fill(shade, projector, bounds, y1), preview.outline());
            return;
        }
        if (!EntityPreview.drawPlayer(graphics, x0, y0, x1, y1, angle, currentSkin(), null, preview.outline())) return;
        bounds = preview.fit(bounds);
        float pixel = 1f / Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
        Point[] corners = Boxes.project(projector, bounds);
        Rect rect = Boxes.bounds(corners);
        ScreenBuffer buffer = new ScreenBuffer();
        float clearance = 0f;
        if (!preview.isFlat()) {
            Boxes.collect(buffer, corners, preview.worldStyle().scaled(pixel));
        } else if (preview.isBoxShown() && rect != null) {
            Style style = preview.screenStyle().scaled(pixel);
            clearance = style.widestStroke() * 0.5f;
            buffer.rect(rect, style);
        }
        HealthBar.Side side = preview.healthSide();
        if (side != null && rect != null) {
            var player = Game.player();
            float fraction = player.getMaxHealth() <= 0f ? 0f : player.getHealth() / player.getMaxHealth();
            HealthBar.collect(buffer, rect, side, fraction, clearance, pixel);
        }
        ScreenRenderer.submit(graphics, buffer);
    }

    private EntityFill fill(PreviewSetting.Shade shade, Projector projector, AABB bounds, int bottom) {
        int color = Opacity.apply(Colors.opaque(shade.rgb()));
        if (shade.effect() > 0) {
            return EntityFill.effect(color, 0, shade.effect(), Vec3.ZERO, EntityPreview.playerScale(contentX(), contentY(),
                    contentX() + contentWidth(), contentY() + contentHeight()));
        }
        if (!shade.gradient()) return EntityFill.solid(color, 0);
        Rect rect = Boxes.bounds(Boxes.project(projector, bounds));
        int guiScale = Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
        int minY = rect == null ? 0 : Math.round((bottom - rect.maxY()) * guiScale);
        int maxY = rect == null ? 0 : Math.round((bottom - rect.minY()) * guiScale);
        return EntityFill.gradient(color, shade.secondaryRgb(), 0, 0, minY, maxY);
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (!open || reveal.value() < 1f || !contains(event.x(), event.y())) return false;
        if (event.button() == 0 && inContent(event.x(), event.y())) {
            dragging = true;
            dragged = false;
            pressX = event.x();
            lastX = event.x();
            gui.dropFocus();
        }
        return !back.isMouseOver(event.x(), event.y()) && !nameInput.isMouseOver(event.x(), event.y());
    }

    public boolean mouseDragged(MouseButtonEvent event) {
        if (!dragging || event.button() != 0) return false;
        if (Math.abs(event.x() - pressX) >= DRAG_THRESHOLD * layout.scale()) dragged = true;
        if (dragged) {
            yaw.snap(yaw.value() + (float) (event.x() - lastX) * DEGREES_PER_PIXEL / layout.scale());
            lastX = event.x();
        }
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (!dragging || event.button() != 0) return false;
        dragging = false;
        if (!dragged) yaw.set(yaw.target() + CLICK_STEP);
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (!open) return false;
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_LEFT) yaw.set(yaw.target() - CLICK_STEP);
        else if (event.key() == GLFW.GLFW_KEY_RIGHT) yaw.set(yaw.target() + CLICK_STEP);
        else return false;
        return true;
    }
}
