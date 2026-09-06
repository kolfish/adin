package dev.koifih.client.render.screen;

import dev.koifih.client.render.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScreenBuffer {
    public record Rect(ScreenRect bounds, Style style) {}

    public record Line(float x0, float y0, float x1, float y1, int color, float width) {}

    public record Quad(ScreenPoint a, ScreenPoint b, ScreenPoint c, ScreenPoint d, int color) {}

    private final List<Rect> rects = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();
    private final List<Quad> quads = new ArrayList<>();

    public void rect(ScreenRect bounds, Style style) {
        rects.add(new Rect(bounds, style));
    }

    public void line(float x0, float y0, float x1, float y1, int color, float width) {
        lines.add(new Line(x0, y0, x1, y1, color, width));
    }

    public void line(ScreenPoint from, ScreenPoint to, int color, float width) {
        line(from.x(), from.y(), to.x(), to.y(), color, width);
    }

    public void quad(ScreenPoint a, ScreenPoint b, ScreenPoint c, ScreenPoint d, int color) {
        quads.add(new Quad(a, b, c, d, color));
    }

    public List<Rect> rects() {
        return Collections.unmodifiableList(rects);
    }

    public List<Line> lines() {
        return Collections.unmodifiableList(lines);
    }

    public List<Quad> quads() {
        return Collections.unmodifiableList(quads);
    }

    public boolean isEmpty() {
        return rects.isEmpty() && lines.isEmpty() && quads.isEmpty();
    }
}
