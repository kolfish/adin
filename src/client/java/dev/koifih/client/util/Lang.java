package dev.koifih.client.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public final class Lang {
    public enum Language {
        ENGLISH("en", "English"),
        RUSSIAN("ru", "Russian"),
        POLISH("pl", "Polish"),
        FRENCH("fr", "French"),
        CROATIAN("hr", "Croatian");

        public static final Language[] ALL = values();
        private final String code;
        private final String displayName;

        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private static final Map<Language, Map<String, String>> TRANSLATIONS = new EnumMap<>(Language.class);
    private static Language current = Language.ENGLISH;

    private Lang() {}

    public static Language current() {
        return current;
    }

    public static void set(Language language) {
        current = language;
    }

    public static String getOrDefault(String key, String fallback) {
        String value = translations(current).get(key);
        if (value == null) value = translations(Language.ENGLISH).get(key);
        return value == null ? fallback : value;
    }

    public static String capitalize(String id) {
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    public static String get(String key) {
        String value = translations(current).get(key);
        if (value == null) value = translations(Language.ENGLISH).get(key);
        return value == null ? key : value;
    }

    private static Map<String, String> translations(Language language) {
        return TRANSLATIONS.computeIfAbsent(language, Lang::load);
    }

    private static Map<String, String> load(Language language) {
        String path = "/assets/adin/translations/" + language.code + ".json";
        try (var stream = Lang.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing translations: " + path);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load translations: " + path, exception);
        }
    }
}
