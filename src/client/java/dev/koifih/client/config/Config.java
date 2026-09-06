package dev.koifih.client.config;

import com.google.gson.JsonElement;
import java.util.Map;

public final class Config {
    public enum Scope { COLORS, SETTINGS, BOTH }

    public static final class Colors {
        public String theme;
        public int accent;
    }

    public static final class ModuleState {
        public boolean enabled;
        public String key;
        public boolean hold;
        public Map<String, JsonElement> settings;
    }

    public String name = "";
    public String description = "";
    public Scope scope = Scope.BOTH;
    public String author = "";
    public long created;
    public Colors colors;
    public Map<String, ModuleState> modules;
}
