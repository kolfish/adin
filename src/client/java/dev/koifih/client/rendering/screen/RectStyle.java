package dev.koifih.client.rendering.screen;

public record RectStyle(int fill, int stroke, float strokeWidth, int outline, float outlineWidth, EdgeStyle edges) {
    public static RectStyle fill(int argb) {
        return new RectStyle(argb, 0, 0f, 0, 0f, EdgeStyle.FULL);
    }

    public static RectStyle stroke(int argb, float width) {
        return new RectStyle(0, argb, width, 0, 0f, EdgeStyle.FULL);
    }

    public RectStyle withFill(int argb) {
        return new RectStyle(argb, stroke, strokeWidth, outline, outlineWidth, edges);
    }

    public RectStyle withStroke(int argb, float width) {
        return new RectStyle(fill, argb, width, outline, outlineWidth, edges);
    }

    public RectStyle withOutline(int argb, float width) {
        return new RectStyle(fill, stroke, strokeWidth, argb, width, edges);
    }

    public RectStyle withEdges(EdgeStyle style) {
        return new RectStyle(fill, stroke, strokeWidth, outline, outlineWidth, style);
    }

    public RectStyle scaled(float factor) {
        return new RectStyle(fill, stroke, strokeWidth * factor, outline, outlineWidth * factor, edges);
    }

    public RectStyle mapColors(java.util.function.IntUnaryOperator mapper) {
        return new RectStyle(mapper.applyAsInt(fill), mapper.applyAsInt(stroke), strokeWidth,
                mapper.applyAsInt(outline), outlineWidth, edges);
    }

    public boolean hasFill() {
        return (fill >>> 24) != 0;
    }

    public boolean hasStroke() {
        return (stroke >>> 24) != 0 && strokeWidth > 0f;
    }

    public boolean hasOutline() {
        return hasStroke() && (outline >>> 24) != 0 && outlineWidth > strokeWidth;
    }

    public float widestStroke() {
        return hasOutline() ? outlineWidth : hasStroke() ? strokeWidth : 0f;
    }
}
