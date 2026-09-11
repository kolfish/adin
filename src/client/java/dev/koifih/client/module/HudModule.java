package dev.koifih.client.module;

import dev.koifih.client.setting.PositionSetting;
import dev.koifih.client.setting.PositionSetting.Anchor;
import dev.koifih.client.setting.PositionSetting.Position;

public abstract class HudModule extends Module {
    private final PositionSetting position;
    private float x;
    private float y;
    private float width;
    private float height;

    protected HudModule(String id, Anchor horizontal, Anchor vertical, float offsetX, float offsetY) {
        super(id);
        position = add(new PositionSetting("position", new Position(horizontal, vertical, offsetX, offsetY)));
    }

    public Position position() {
        return position.get();
    }

    protected float left(float width, float screenWidth) {
        return position.get().x(width, screenWidth);
    }

    protected float top(float height, float screenHeight) {
        return position.get().y(height, screenHeight);
    }

    protected void placed(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public void moveTo(float x, float y, float screenWidth, float screenHeight) {
        position.set(Position.of(x, y, width, height, screenWidth, screenHeight));
    }
}
