package dev.koifih.client.render.cape;

import dev.koifih.Adin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CapeImport {
    private static final String EXTENSION = ".png";
    private static final String TITLE = "Add cape";
    private static final String FILTER = "Image or cape texture";
    private static final String THREAD = "adin-cape-picker";

    private static boolean picking;

    public static void pick(Consumer<Cape> onImported) {
        if (picking) return;
        picking = true;
        Thread thread = new Thread(() -> {
            String chosen;
            try {
                chosen = prompt();
            } finally {
                picking = false;
            }
            if (chosen == null) return;
            Path source = Path.of(chosen);
            Minecraft.getInstance().execute(() -> {
                Cape imported = copyIn(source);
                if (imported != null) onImported.accept(imported);
            });
        }, THREAD);
        thread.setDaemon(true);
        thread.start();
    }

    private static String prompt() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer patterns = stack.mallocPointer(1);
            patterns.put(stack.UTF8("*" + EXTENSION));
            patterns.flip();
            return TinyFileDialogs.tinyfd_openFileDialog(TITLE, "", patterns, FILTER, false);
        }
    }

    private static Cape copyIn(Path source) {
        try {
            Files.createDirectories(CapeCatalog.directory());
            Path target = target(source.getFileName().toString());
            Files.copy(source, target);
            return CapeCatalog.add(target);
        } catch (IOException exception) {
            Adin.LOGGER.warn("Cannot import cape {}", source, exception);
            return null;
        }
    }

    private static Path target(String file) {
        if (!file.toLowerCase(Locale.ROOT).endsWith(EXTENSION)) file += EXTENSION;
        String base = file.substring(0, file.length() - EXTENSION.length());
        Path target = CapeCatalog.directory().resolve(file);
        for (int i = 2; Files.exists(target); i++) target = CapeCatalog.directory().resolve(base + "_" + i + EXTENSION);
        return target;
    }
}
