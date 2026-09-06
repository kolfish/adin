package dev.koifih.client.rendering.screen;

import java.util.ArrayList;
import java.util.List;

public final class OverlayCollector {
    public record Rect(ScreenRect bounds, RectStyle style) {}

    public record Line(float x0, float y0, float x1, float y1, int color, float width) {}

    private final List<Rect> rects = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();

    public void rect(ScreenRect bounds, RectStyle style) {
        rects.add(new Rect(bounds, style));
    }

    public void line(float x0, float y0, float x1, float y1, int color, float width) {
        lines.add(new Line(x0, y0, x1, y1, color, width));
    }

    public void line(ScreenPoint from, ScreenPoint to, int color, float width) {
        line(from.x(), from.y(), to.x(), to.y(), color, width);
    }

    public List<Rect> rects() {
        return List.copyOf(rects);
    }

    public List<Line> lines() {
        return List.copyOf(lines);
    }
}
