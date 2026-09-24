package dev.koifih.client.render.screen;

import dev.koifih.client.render.Point;
import dev.koifih.client.render.Rect;
import dev.koifih.client.render.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScreenBuffer {
    public record Shape(Rect bounds, Style style) {}

    public record Line(float x0, float y0, float x1, float y1, int color, float width) {}

    public record Quad(Point a, Point b, Point c, Point d, int color) {}

    private final List<Shape> shapes = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();
    private final List<Quad> quads = new ArrayList<>();

    public void rect(Rect bounds, Style style) {
        shapes.add(new Shape(bounds, style));
    }

    public void line(float x0, float y0, float x1, float y1, int color, float width) {
        lines.add(new Line(x0, y0, x1, y1, color, width));
    }

    public void line(Point from, Point to, int color, float width) {
        line(from.x(), from.y(), to.x(), to.y(), color, width);
    }

    public void quad(Point a, Point b, Point c, Point d, int color) {
        quads.add(new Quad(a, b, c, d, color));
    }

    public List<Shape> shapes() {
        return Collections.unmodifiableList(shapes);
    }

    public List<Line> lines() {
        return Collections.unmodifiableList(lines);
    }

    public List<Quad> quads() {
        return Collections.unmodifiableList(quads);
    }

    public boolean isEmpty() {
        return shapes.isEmpty() && lines.isEmpty() && quads.isEmpty();
    }
}
