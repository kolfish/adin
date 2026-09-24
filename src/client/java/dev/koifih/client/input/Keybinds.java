package dev.koifih.client.input;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.Adin;
import dev.koifih.client.mixin.accessor.KeyMappingAccessor;
import dev.koifih.client.ui.clickgui.ClickGui;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Keybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Adin.id("keybinds"));

    public static final KeyMapping OPEN_CLICK_GUI = register("open_click_gui", GLFW.GLFW_KEY_RIGHT_SHIFT);

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(Keybinds::onClientTick);
    }

    private static boolean wasClickGuiDown;

    private static void onClientTick(Minecraft client) {
        boolean pressed = false;
        while (OPEN_CLICK_GUI.consumeClick()) pressed = true;

        if (client.getWindow() != null) {
            long window = client.getWindow().handle();
            InputConstants.Key key = clickGuiKey();
            boolean isDown = false;
            if (key != InputConstants.UNKNOWN && client.gui.screen() == null) {
                isDown = switch (key.getType()) {
                    case KEYSYM -> GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
                    case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
                    default -> false;
                };
            }
            if (isDown && !wasClickGuiDown) pressed = true;
            wasClickGuiDown = isDown;
        }

        if (pressed && client.gui.screen() == null && client.player != null) {
            client.gui.setScreen(new ClickGui());
        }
    }

    public static InputConstants.Key clickGuiKey() {
        try {
            return ((KeyMappingAccessor) OPEN_CLICK_GUI).adin$getKey();
        } catch (Throwable t) {
            try {
                java.lang.reflect.Field field = KeyMapping.class.getDeclaredField("key");
                field.setAccessible(true);
                return (InputConstants.Key) field.get(OPEN_CLICK_GUI);
            } catch (Throwable ignored) {
                return OPEN_CLICK_GUI.getDefaultKey();
            }
        }
    }

    public static void setClickGuiKey(InputConstants.Key key) {
        if (key == InputConstants.UNKNOWN) key = OPEN_CLICK_GUI.getDefaultKey();
        OPEN_CLICK_GUI.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    private static KeyMapping register(String name, int key) {
        var mapping = new KeyMapping("key." + Adin.MOD_ID + "." + name, InputConstants.Type.KEYSYM, key, CATEGORY);
        try {
            return KeyMappingHelper.registerKeyMapping(mapping);
        } catch (Throwable ignored) {
            return mapping;
        }
    }
}
