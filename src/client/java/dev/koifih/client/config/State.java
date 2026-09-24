package dev.koifih.client.config;

import com.google.gson.JsonElement;
import java.util.Map;

public class State {
    public static final int VERSION = 1;

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

    public int version = VERSION;
    public Colors colors;
    public Map<String, ModuleState> modules;
}
