package dev.koifih.client.ui.component;

import dev.koifih.client.render.AdinIcon;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.video.VideoPlayer;
import dev.koifih.client.ui.video.Videos;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class VideoPopup extends Popup {
    private static final float WIDTH = 200f;
    private static final float PADDING = 4f;
    private static final float TABS_HEIGHT = 14f;
    private static final float GAP = 4f;
    private static final float ASPECT = 9f / 16f;

    private final List<Videos.Clip> clips;
    private final Transition highlight = new Transition(0f, 150);
    private VideoPlayer player;
    private int variant;
    private int areaX;
    private int areaWidth;

    public VideoPopup(int x, int y, int size, float scale, List<Videos.Clip> clips) {
        super(x, y, size, size, scale, Component.literal(Lang.get("video.watch")));
        this.clips = clips;
    }

    public void setArea(int x, int width) {
        areaX = x;
        areaWidth = width;
    }

    private void play() {
        release();
        player = Videos.open(clips.get(variant));
    }

    private void release() {
        if (player != null) player.close();
        player = null;
    }

    private void select(int next) {
        if (next == variant) return;
        variant = next;
        play();
    }

    @Override
    protected void onOpen() {
        play();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        release();
    }

    private boolean tabbed() {
        return clips.size() > 1;
    }

    private float videoWidth() {
        return popupWidth() - 2 * px(PADDING);
    }

    private float tabsSpan() {
        return tabbed() ? px(TABS_HEIGHT + GAP) : 0f;
    }

    @Override
    protected float popupWidth() {
        return Math.min(areaWidth, px(WIDTH));
    }

    @Override
    protected float popupHeight() {
        return videoWidth() * ASPECT + 2 * px(PADDING) + tabsSpan();
    }

    @Override
    protected float popupX() {
        return areaX + (areaWidth - popupWidth()) / 2f;
    }

    @Override
    protected void drawValue(GuiGraphicsExtractor graphics) {
        float iconSize = getWidth() * 0.8f;
        boolean open = isOpen();
        Draw.icon(graphics, AdinIcon.PLAY, getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2, iconSize,
                open ? Theme.ACCENT : Theme.DIM, Theme.ACCENT, open ? Theme.ACCENT : Theme.TEXT, Theme.ROW);
    }

    @Override
    protected void drawPopup(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float shown, int mouseX, int mouseY) {
        boolean below = y > getY();
        Transform.popIn(graphics, x + width / 2f, below ? y : y + height, shown, 0.9f, () -> {
            Draw.bordered(graphics, x, y, width, height, px(5), Theme.POPUP, Theme.POPUP_BORDER);
            float top = y + px(PADDING);
            if (tabbed()) {
                drawTabs(graphics, x + px(PADDING), top, videoWidth());
                top += tabsSpan();
            }
            int drawWidth = Math.round(videoWidth());
            int drawHeight = Math.round(videoWidth() * ASPECT);
            if (player != null) player.draw(graphics, Math.round(x + px(PADDING)), Math.round(top), drawWidth, drawHeight);
            else rect(graphics, x + px(PADDING), top, drawWidth, drawHeight, px(3), Theme.CONTROL);
        });
    }

    private void drawTabs(GuiGraphicsExtractor graphics, float x, float y, float width) {
        highlight.set(variant);
        float tab = width / clips.size();
        float height = px(TABS_HEIGHT);
        rect(graphics, x, y, width, height, px(4), Theme.CONTROL);
        rect(graphics, x + tab * highlight.value(), y, tab, height, px(4), Theme.CONTROL_ACTIVE);
        for (int i = 0; i < clips.size(); i++) {
            String label = fit(Lang.get(clips.get(i).label()), tab - px(6), px(6.5f));
            float selected = Math.clamp(1f - Math.abs(highlight.value() - i), 0f, 1f);
            text(graphics, label, x + tab * i + (tab - Text.width(label, px(6.5f))) / 2, y + height / 2, px(6.5f),
                    Colors.lerp(Theme.DIM, Theme.TEXT, selected));
        }
    }

    @Override
    protected void clickPopup(double x, double y) {
        if (!tabbed()) return;
        float top = popupY() + px(PADDING);
        if (y < top || y >= top + px(TABS_HEIGHT)) return;
        select(Math.clamp((int) ((x - popupX() - px(PADDING)) / (videoWidth() / clips.size())), 0, clips.size() - 1));
    }

    @Override
    protected void popupKeyPressed(KeyEvent event) {
        if (!tabbed()) return;
        if (event.isLeft()) select(Math.max(0, variant - 1));
        else if (event.isRight()) select(Math.min(clips.size() - 1, variant + 1));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(Lang.get("video.watch")));
        output.add(NarratedElementType.USAGE, Component.literal(tabbed()
                ? "Enter opens the video. Left and right switch clips." : "Enter opens the video."));
    }
}
