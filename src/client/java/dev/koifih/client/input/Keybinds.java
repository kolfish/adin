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

    private static void onClientTick(Minecraft client) {
        boolean pressed = false;
        while (OPEN_CLICK_GUI.consumeClick()) pressed = true;
        if (pressed && client.gui.screen() == null && client.player != null) {
            client.gui.setScreen(new ClickGui());
        }
    }

    public static InputConstants.Key clickGuiKey() {
        return ((KeyMappingAccessor) OPEN_CLICK_GUI).adin$getKey();
    }

    public static void setClickGuiKey(InputConstants.Key key) {
        if (key == InputConstants.UNKNOWN) key = OPEN_CLICK_GUI.getDefaultKey();
        OPEN_CLICK_GUI.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    private static KeyMapping register(String name, int key) {
        var mapping = new KeyMapping("key." + Adin.MOD_ID + "." + name, InputConstants.Type.KEYSYM, key, CATEGORY);
        return KeyMappingHelper.registerKeyMapping(mapping);
    }
}
