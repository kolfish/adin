package dev.koifih.client.render.cape;

import dev.koifih.Adin;
import dev.koifih.client.config.Storage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CapeCatalog {
    public static final CapeRenderer ADIN = new CapeRenderer("adin", false);
    public static final CapeRenderer ADIN_ANIMATED = new CapeRenderer("adin_animated", true);
    private static final List<Cape> BUILTIN = List.of(ADIN, ADIN_ANIMATED);
    private static final String EXTENSION = ".png";

    private static List<Cape> capes;

    public static List<Cape> all() {
        if (capes == null) scan();
        return capes;
    }

    public static Cape get(String id) {
        for (Cape cape : all()) {
            if (cape.id().equals(id)) return cape;
        }
        return ADIN;
    }

    public static Path directory() {
        return Storage.directory().resolve("capes");
    }

    static Cape add(Path path) {
        Cape cape = new FileCape(path);
        all().add(cape);
        return cape;
    }

    public static boolean delete(Cape cape) {
        if (!(cape instanceof FileCape file)) return false;
        Storage.delete(file.path());
        all().remove(cape);
        return true;
    }

    private static void scan() {
        capes = new ArrayList<>(BUILTIN);
        if (!Files.isDirectory(directory())) return;
        try (Stream<Path> stream = Files.list(directory())) {
            stream.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(EXTENSION))
                    .sorted()
                    .forEach(path -> capes.add(new FileCape(path)));
        } catch (IOException exception) {
            Adin.LOGGER.warn("Cannot list capes in {}", directory(), exception);
        }
    }
}
