package dev.koifih.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.koifih.Adin;
import dev.koifih.client.AdinClient;
import dev.koifih.client.gui.Theme;
import net.minecraft.client.Minecraft;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStore() {}

    public static Path directory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Adin.MOD_ID).resolve("configs");
    }

    private static Path path(Config config) {
        String safe = config.name.trim().replaceAll("[^A-Za-z0-9 _-]", "_");
        if (safe.isEmpty()) safe = "config";
        return directory().resolve(safe + ".json");
    }

    public static List<Config> list() {
        List<Config> configs = new ArrayList<>();
        if (!Files.isDirectory(directory())) return configs;
        try (Stream<Path> files = Files.list(directory())) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    Config config = GSON.fromJson(reader, Config.class);
                    if (config != null && config.name != null) configs.add(config);
                } catch (Exception exception) {
                    Adin.LOGGER.warn("Skipping unreadable config {}", file, exception);
                }
            }
        } catch (IOException exception) {
            Adin.LOGGER.warn("Cannot list configs", exception);
        }
        configs.sort(Comparator.comparingLong((Config config) -> config.created).reversed());
        return configs;
    }

    public static void save(Config config) {
        try {
            Files.createDirectories(directory());
            try (Writer writer = Files.newBufferedWriter(path(config), StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            Adin.LOGGER.warn("Cannot save config {}", config.name, exception);
        }
    }

    public static void delete(Config config) {
        try {
            Files.deleteIfExists(path(config));
        } catch (IOException exception) {
            Adin.LOGGER.warn("Cannot delete config {}", config.name, exception);
        }
    }

    public static Config create(String name, String description, Config.Scope scope) {
        Config config = new Config();
        config.name = name.trim();
        config.description = description.trim();
        config.scope = scope;
        config.author = Minecraft.getInstance().getUser().getName();
        config.created = System.currentTimeMillis();
        if (scope != Config.Scope.SETTINGS) {
            config.colors = new Config.Colors();
            config.colors.theme = Theme.mode().name();
            config.colors.accent = Theme.accentRgb();
        }
        if (scope != Config.Scope.COLORS) config.modules = AdinClient.MODULES.snapshot();
        save(config);
        return config;
    }

    public static void apply(Config config, boolean colors, boolean settings) {
        if (colors && config.colors != null) {
            try {
                Theme.setMode(Theme.Mode.valueOf(config.colors.theme));
            } catch (IllegalArgumentException | NullPointerException ignored) {
            }
            Theme.setAccent(config.colors.accent);
        }
        if (settings && config.modules != null) AdinClient.MODULES.apply(config.modules);
    }
}
