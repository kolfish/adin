package dev.koifih.client.module;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.config.Config;
import dev.koifih.client.module.impl.combat.AimAssist;
import dev.koifih.client.module.impl.combat.AutoAnchor;
import dev.koifih.client.module.impl.combat.AutoCart;
import dev.koifih.client.module.impl.combat.AutoCrystal;
import dev.koifih.client.module.impl.combat.AutoHitCrystal;
import dev.koifih.client.module.impl.combat.ShieldBreaker;
import dev.koifih.client.module.impl.combat.Triggerbot;
import dev.koifih.client.module.impl.hud.KeybindList;
import dev.koifih.client.module.impl.hud.ModuleList;
import dev.koifih.client.module.impl.hud.Notifications;
import dev.koifih.client.module.impl.hud.Watermark;
import dev.koifih.client.module.impl.misc.Friends;
import dev.koifih.client.module.impl.movement.JumpReset;
import dev.koifih.client.module.impl.movement.MoveFix;
import dev.koifih.client.module.impl.movement.Sprint;
import dev.koifih.client.module.impl.player.AutoTotem;
import dev.koifih.client.module.impl.player.KeyPearl;
import dev.koifih.client.module.impl.player.Refill;
import dev.koifih.client.module.impl.render.BlockEsp;
import dev.koifih.client.module.impl.render.Nametags;
import dev.koifih.client.module.impl.render.esp.Esp;
import dev.koifih.client.setting.Setting;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);
    private final Map<String, Module> byId = new HashMap<>();
    private final Map<Class<? extends Module>, Module> byType = new HashMap<>();

    public ModuleManager() {
        for (Category category : Category.values()) byCategory.put(category, new ArrayList<>());
        register(Category.COMBAT,
                new AimAssist(),
                new Triggerbot(),
                new ShieldBreaker(),
                new AutoHitCrystal(),
                new AutoCrystal(),
                new AutoAnchor(),
                new AutoCart());
        register(Category.MOVEMENT,
                new Sprint(),
                new MoveFix(),
                new JumpReset());
        register(Category.RENDER,
                new Esp(),
                new Nametags(),
                new BlockEsp());
        register(Category.HUD,
                new Notifications(),
                new ModuleList(),
                new Watermark(),
                new KeybindList());
        register(Category.PLAYER,
                new KeyPearl(),
                new AutoTotem(),
                new Refill());
        register(Category.MISC,
                new Friends());
    }

    private void register(Category category, Module... group) {
        for (Module module : group) {
            if (byId.containsKey(module.id())) throw new IllegalArgumentException("Duplicate module id: " + module.id());
            module.category = category;
            modules.add(module);
            byCategory.get(category).add(module);
            byId.put(module.id(), module);
            byType.put(module.getClass(), module);
        }
    }

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> in(Category category) {
        return Collections.unmodifiableList(byCategory.get(category));
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
