package dev.koifih.client.util;

import dev.koifih.client.AdinClient;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

import java.lang.reflect.Field;

public final class InputHelper {
    private static Field moveVectorField;

    static {
        for (Field f : ClientInput.class.getDeclaredFields()) {
            if (f.getType() == Vec2.class) {
                f.setAccessible(true);
                moveVectorField = f;
                break;
            }
        }
    }

    private InputHelper() {}

    public static void correctInput(Object inputObj, LocalPlayer player) {
        if (!(inputObj instanceof ClientInput input) || moveVectorField == null) return;
        try {
            Vec2 move = (Vec2) moveVectorField.get(input);
            if (move == null) return;
            Vec2 corrected = AdinClient.ROTATIONS.correctInput(move, player.getYRot());
            if (corrected == move) return;
            moveVectorField.set(input, corrected);
            Input keys = input.keyPresses;
            input.keyPresses = new Input(corrected.y > 0f, corrected.y < 0f, corrected.x > 0f, corrected.x < 0f, keys.jump(), keys.shift(), keys.sprint());
        } catch (Throwable ignored) {}
    }
}
