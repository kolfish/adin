package dev.koifih.client.module;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.config.Config;
import dev.koifih.client.setting.Setting;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final List<Module> view = Collections.unmodifiableList(modules);
    private final Map<String, Module> byId = new HashMap<>();
    private final Map<Class<? extends Module>, Module> byType = new HashMap<>();

    public <M extends Module> M register(M module) {
        if (byId.containsKey(module.id())) {
            throw new IllegalArgumentException("Duplicate module id: " + module.id());
        }
        modules.add(module);
        byId.put(module.id(), module);
        byType.put(module.getClass(), module);
        return module;
    }

    public List<Module> all() {
        return view;
    }

    public Optional<Module> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Module get(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown module: " + id));
    }

    public <M extends Module> M get(Class<M> type) {
        Module module = byType.get(type);
        if (module == null) throw new IllegalArgumentException("Unregistered module: " + type.getSimpleName());
        return type.cast(module);
    }

    public List<Module> in(Category category) {
        return modules.stream().filter(module -> module.category() == category).toList();
    }

    public boolean isEnabled(Class<? extends Module> type) {
        Module module = byType.get(type);
        return module != null && module.isEnabled();
    }

    public void tickKeybinds(Minecraft client) {
        for (Module module : modules) module.tickKeybind(client);
    }

    public boolean activates(InputConstants.Key key) {
        for (Module module : modules) {
            if (module.activatable() && module.isEnabled() && module.key().equals(key)) return true;
        }
        return false;
    }

    public Map<String, Config.ModuleState> snapshot() {
        Map<String, Config.ModuleState> states = new LinkedHashMap<>();
        for (Module module : modules) {
            Config.ModuleState state = new Config.ModuleState();
            state.enabled = module.isEnabled();
            state.key = module.key().getName();
            state.hold = module.hold();
            state.settings = new LinkedHashMap<>();
            for (Setting<?> setting : module.settings()) state.settings.put(setting.id(), setting.save());
            states.put(module.id(), state);
        }
        return states;
    }

    public void apply(Map<String, Config.ModuleState> states) {
        for (Module module : modules) {
            Config.ModuleState state = states.get(module.id());
            if (state == null) continue;
            module.setHold(state.hold);
            module.setKey(parseKey(state.key));
            if (state.settings != null) {
                for (Setting<?> setting : module.settings()) {
                    JsonElement json = state.settings.get(setting.id());
                    if (json != null) setting.load(json);
                }
            }
            module.setEnabled(state.enabled);
        }
    }

    private static InputConstants.Key parseKey(String name) {
        if (name == null) return InputConstants.UNKNOWN;
        try {
            return InputConstants.getKey(name);
        } catch (IllegalArgumentException exception) {
            return InputConstants.UNKNOWN;
        }
    }
}
