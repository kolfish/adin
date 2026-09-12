package dev.koifih.client.util;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.mixin.accessor.MouseHandlerAccessor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Clicks {
    public static boolean simulate;
    private static final boolean[] held = new boolean[2];

    public static void init() {
        AdinClient.EVENTS.subscribe(PreTickEvent.class, Priority.HIGHEST, event -> release(event.client()));
    }

    public static boolean left(Minecraft client, Entity target) {
        return client.hitResult instanceof EntityHitResult hit && hit.getEntity() == target && press(client, GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    public static boolean right(Minecraft client, BlockHitResult target) {
        return client.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && hit.getBlockPos().equals(target.getBlockPos()) && hit.getDirection() == target.getDirection()
                && press(client, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    public static boolean right(Minecraft client, BlockPos target) {
        return client.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && hit.getBlockPos().equals(target) && press(client, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    public static boolean right(Minecraft client) {
        return client.hitResult != null && client.hitResult.getType() == HitResult.Type.MISS && press(client, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    public static boolean simulating(int button) {
        return button >= 0 && button < held.length && held[button];
    }

    private static boolean press(Minecraft client, int button) {
        if (!simulate || client.gui.screen() != null || !client.mouseHandler.isMouseGrabbed()) return false;
        if (held[button]) send(client, button, GLFW.GLFW_RELEASE);
        held[button] = true;
        send(client, button, GLFW.GLFW_PRESS);
        return true;
    }

    private static void release(Minecraft client) {
        for (int button = 0; button < held.length; button++) {
            if (!held[button]) continue;
            held[button] = false;
            send(client, button, GLFW.GLFW_RELEASE);
        }
    }

    private static void send(Minecraft client, int button, int action) {
        ((MouseHandlerAccessor) client.mouseHandler).adin$onButton(client.getWindow().handle(), new MouseButtonInfo(button, 0), action);
    }
}
