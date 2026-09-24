package dev.koifih.client.rotation;

public interface Rotator {
    Rotation step(Rotation from, Rotation target, RotationConfig config, float deltaTicks);
}
