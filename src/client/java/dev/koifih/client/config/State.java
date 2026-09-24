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
<<<<<<< HEAD
        public String bindKey;
=======
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
        public boolean hold;
        public Map<String, JsonElement> settings;
    }

    public int version = VERSION;
    public Colors colors;
    public Map<String, ModuleState> modules;
}
