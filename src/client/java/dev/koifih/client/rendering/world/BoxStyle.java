package dev.koifih.client.rendering.world;

public record BoxStyle(int fill, int stroke, float strokeWidth, int outline, float outlineWidth) {
    public static final float MIN_STROKE_WIDTH = 1f;
    public static final float MIN_OUTLINE_BORDER = 2f;

    public static BoxStyle fill(int argb) {
        return new BoxStyle(argb, 0, 0f, 0, 0f);
    }

    public static BoxStyle stroke(int argb, float width) {
        return new BoxStyle(0, argb, width, 0, 0f);
    }

    public BoxStyle withFill(int argb) {
        return new BoxStyle(argb, stroke, strokeWidth, outline, outlineWidth);
    }

    public BoxStyle withStroke(int argb, float width) {
        return new BoxStyle(fill, argb, width, outline, outlineWidth);
    }

    public BoxStyle withOutline(int argb, float width) {
        return new BoxStyle(fill, stroke, strokeWidth, argb, width);
    }

    public BoxStyle scaled(float factor) {
        float scaledStroke = Math.max(MIN_STROKE_WIDTH, strokeWidth * factor);
        float border = (outlineWidth - strokeWidth) * factor;
        float scaledOutline = border >= MIN_OUTLINE_BORDER ? scaledStroke + border : 0f;
        return new BoxStyle(fill, stroke, scaledStroke, outline, scaledOutline);
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
}
