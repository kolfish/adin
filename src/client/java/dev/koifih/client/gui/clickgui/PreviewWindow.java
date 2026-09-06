package dev.koifih.client.gui.clickgui;

import dev.koifih.client.feature.setting.PreviewSetting;
import dev.koifih.client.gui.Theme;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;
import dev.koifih.client.gui.component.IconButton;
import dev.koifih.client.rendering.Draw;
import dev.koifih.client.rendering.GuiRenderQueue;
import dev.koifih.client.rendering.Opacity;
import dev.koifih.client.rendering.Previews;
import dev.koifih.client.rendering.RectRenderer;
import dev.koifih.client.rendering.Scissor;
import dev.koifih.client.rendering.TextRenderer;
import dev.koifih.client.rendering.screen.HealthBar;
import dev.koifih.client.rendering.screen.OverlayCollector;
import dev.koifih.client.rendering.screen.RectStyle;
import dev.koifih.client.rendering.screen.ScreenLineRenderState;
import dev.koifih.client.rendering.screen.ScreenPoint;
import dev.koifih.client.rendering.screen.ScreenQuadRenderState;
import dev.koifih.client.rendering.screen.ScreenRect;
import dev.koifih.client.rendering.screen.ScreenRectRenderState;
import dev.koifih.client.rendering.world.BoxStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
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
    private static final int[] EDGES = {
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 2, 1, 3, 4, 6, 5, 7,
            0, 4, 1, 5, 2, 6, 3, 7
    };
    private static final int[][] FACES = {
            {0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}
    };

    private final WidgetHost host;
    private final Transition reveal = new Transition(0f, REVEAL_MILLIS, Easing.EASE_OUT_CUBIC);
    private final Transition yaw = new Transition(DEFAULT_YAW, 200, Easing.EASE_OUT_CUBIC);
    private PanelLayout layout;
    private IconButton back;
    private PreviewSetting preview;
    private boolean open;
    private boolean dragging;
    private boolean dragged;
    private double pressX;
    private double lastX;

    public PreviewWindow(WidgetHost host) {
        this.host = host;
    }

    public void init(PanelLayout layout) {
        this.layout = layout;
        back = host.add(new IconButton(backX(), backY(), layout.atLeastOne(BACK_SIZE), layout.scale(), BACK_ICON,
                Component.literal("Back"), this::close));
        updateStates(true);
    }

    public void relayout(PanelLayout layout) {
        this.layout = layout;
        back.setX(backX());
        back.setY(backY());
    }

    public void open(PreviewSetting preview) {
        this.preview = preview;
        open = true;
        reveal.set(1f);
        host.dropFocus();
    }

    public void close() {
        open = false;
        dragging = false;
        reveal.set(0f);
        host.dropFocus();
    }

    public boolean isOpen() {
        return open;
    }

    public void updateStates(boolean interactive) {
        back.active = open && interactive && reveal.value() >= 1f;
        back.visible = reveal.value() > 0f;
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

    private int contentY() {
        return y() + layout.topBarHeight() + layout.padding();
    }

    private int contentWidth() {
        return width() - 2 * layout.padding();
    }

    private int contentHeight() {
        return height() - layout.topBarHeight() - 2 * layout.padding();
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x() && pointX < x() + width() && pointY >= y() && pointY < y() + height();
    }

    private boolean inContent(double pointX, double pointY) {
        return pointX >= contentX() && pointX < contentX() + contentWidth()
                && pointY >= contentY() && pointY < contentY() + contentHeight();
    }

    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float shown = reveal.value();
        if (shown <= 0f || preview == null) return;
        float slide = (onRight() ? -1f : 1f) * SLIDE * layout.scale() * (1f - shown);
        Opacity.with(shown, () -> Draw.translated(graphics, slide, 0f, () -> drawWindow(graphics, mouseX, mouseY, delta)));
    }

    private void drawWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int radius = layout.atLeastOne(PanelLayout.RADIUS);
        RectRenderer.draw(graphics, x(), y(), width(), height(), radius, Theme.SIDEBAR);
        RectRenderer.draw(graphics, x(), y() + layout.topBarHeight(), width(), height() - layout.topBarHeight(), radius, Theme.MAIN);
        RectRenderer.draw(graphics, x(), y() + layout.topBarHeight(), width(), radius, 0, Theme.MAIN);
        float titleX = back.getX() + back.getWidth() + layout.scaled(PanelLayout.GAP);
        TextRenderer.drawCentered(graphics, preview.name(), titleX, y() + layout.topBarHeight() * 0.5f, 8 * layout.scale(), Theme.TEXT);
        back.extractRenderState(graphics, mouseX, mouseY, delta);
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
        if (!Previews.drawPlayer(graphics, x0, y0, x1, y1, angle)) return;
        Previews.Projector projector = Previews.playerProjector(x0, y0, x1, y1, angle);
        AABB bounds = Previews.playerLocalBounds();
        if (projector == null || bounds == null) return;
        bounds = preview.fit(bounds);
        float pixel = 1f / Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
        Matrix3x2fc pose = new Matrix3x2f(graphics.pose());
        ScreenRect rect = projector.bounds(bounds);
        float clearance = 0f;
        if (preview.isFlat()) {
            if (preview.isBoxShown()) {
                RectStyle style = preview.rectStyle().scaled(pixel).mapColors(Opacity::apply);
                clearance = style.widestStroke() * 0.5f;
                GuiRenderQueue.submit(graphics, new ScreenRectRenderState(pose, rect, style, Scissor.current()));
            }
        } else {
            drawBox(graphics, pose, projector, bounds, preview.boxStyle().scaled(pixel));
        }
        HealthBar.Side side = preview.healthSide();
        if (side == null) return;
        var player = Minecraft.getInstance().player;
        float fraction = player.getMaxHealth() <= 0f ? 0f : player.getHealth() / player.getMaxHealth();
        OverlayCollector shapes = new OverlayCollector();
        HealthBar.collect(shapes, rect, side, fraction, clearance, pixel);
        for (OverlayCollector.Rect bar : shapes.rects()) {
            GuiRenderQueue.submit(graphics, new ScreenRectRenderState(pose, bar.bounds(), bar.style().mapColors(Opacity::apply), Scissor.current()));
        }
    }

    private void drawBox(GuiGraphicsExtractor graphics, Matrix3x2fc pose, Previews.Projector projector, AABB box, BoxStyle style) {
        ScreenPoint[] corners = new ScreenPoint[8];
        for (int i = 0; i < 8; i++) {
            corners[i] = projector.project((i & 1) == 0 ? box.minX : box.maxX, (i & 2) == 0 ? box.minY : box.maxY,
                    (i & 4) == 0 ? box.minZ : box.maxZ);
        }
        ScreenRectangle scissor = Scissor.current();
        if (style.hasFill()) {
            int fill = Opacity.apply(style.fill());
            for (int[] face : FACES) {
                GuiRenderQueue.submit(graphics, new ScreenQuadRenderState(pose, corners[face[0]], corners[face[1]],
                        corners[face[2]], corners[face[3]], fill, scissor));
            }
        }
        if (style.hasOutline()) drawEdges(graphics, pose, corners, Opacity.apply(style.outline()), style.outlineWidth(), scissor);
        if (style.hasStroke()) drawEdges(graphics, pose, corners, Opacity.apply(style.stroke()), style.strokeWidth(), scissor);
    }

    private static void drawEdges(GuiGraphicsExtractor graphics, Matrix3x2fc pose, ScreenPoint[] corners, int color,
                                  float width, ScreenRectangle scissor) {
        for (int i = 0; i < EDGES.length; i += 2) {
            ScreenPoint a = corners[EDGES[i]];
            ScreenPoint b = corners[EDGES[i + 1]];
            var line = new OverlayCollector.Line(a.x(), a.y(), b.x(), b.y(), color, width);
            GuiRenderQueue.submit(graphics, new ScreenLineRenderState(pose, line, scissor));
        }
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (!open || reveal.value() < 1f || !contains(event.x(), event.y())) return false;
        if (event.button() == 0 && inContent(event.x(), event.y())) {
            dragging = true;
            dragged = false;
            pressX = event.x();
            lastX = event.x();
            host.dropFocus();
        }
        return !back.isMouseOver(event.x(), event.y());
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
