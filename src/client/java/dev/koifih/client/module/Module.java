package dev.koifih.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.Event;
import dev.koifih.client.event.Listener;
import dev.koifih.client.event.Priority;
import dev.koifih.client.event.Subscription;
import dev.koifih.client.event.events.ModuleToggleEvent;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.setting.SettingGroup;
import dev.koifih.client.util.Lang;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public static Minecraft mc = Minecraft.getInstance();
    private final String id;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private boolean enabled;
    private InputConstants.Key key = InputConstants.UNKNOWN;
    private boolean hold;
    private boolean keyWasDown;

    protected Module(String id, Category category) {
        this.id = id;
        this.category = category;
    }

    public String id() {
        return id;
    }

    public Category category() {
        return category;
    }

    public String name() {
        return Lang.getOrDefault("module." + id, Lang.capitalize(id));
    }

    public String info() {
        return "";
    }

    public String description() {
        return Lang.getOrDefault("module." + id + ".description", "");
    }

    protected <S extends Setting<?>> S add(S setting) {
        setting.attach(id);
        settings.add(setting);
        return setting;
    }

    protected <G extends SettingGroup> G add(G group) {
        for (Setting<?> setting : group.settings()) add(setting);
        return group;
    }

    public List<Setting<?>> settings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            for (Subscription subscription : subscriptions) subscription.cancel();
            subscriptions.clear();
            onDisable();
        }
        AdinClient.EVENTS.post(new ModuleToggleEvent(this, enabled));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public InputConstants.Key key() {
        return key;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key == null ? InputConstants.UNKNOWN : key;
    }

    public boolean hold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public boolean activatable() {
        return false;
    }

    protected void onActivate() {
    }

    protected void onHold() {
    }

    protected void onRelease() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected <E extends Event> void listen(Class<E> type, Listener<E> listener) {
        listen(type, Priority.NORMAL, listener);
    }

    protected <E extends Event> void listen(Class<E> type, int priority, Listener<E> listener) {
        subscriptions.add(AdinClient.EVENTS.subscribe(type, priority, listener));
    }

    void tickKeybind(Minecraft client) {
        boolean down = key != InputConstants.UNKNOWN && client.gui.screen() == null && isKeyDown(client.getWindow().handle());
        if (activatable()) {
            if (enabled) {
                if (down && !keyWasDown) onActivate();
                if (down) onHold();
                if (!down && keyWasDown) onRelease();
            }
        } else if (hold) {
            if (down != keyWasDown) setEnabled(down);
        } else if (down && !keyWasDown) {
            toggle();
        }
        keyWasDown = down;
    }

    private boolean isKeyDown(long window) {
        return switch (key.getType()) {
            case KEYSYM -> GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
    }
}
