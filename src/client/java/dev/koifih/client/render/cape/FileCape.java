package dev.koifih.client.render.cape;

import com.mojang.blaze3d.platform.NativeImage;
import dev.koifih.Adin;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Accessors(fluent = true)
public final class FileCape implements Cape {
    private static final String EXTENSION = ".png";
    private static final String ILLEGAL = "\\/:*?\"<>|";

    @Getter
    private Path path;
    @Getter
    private String id;
    @Getter
    private String label;
    @Getter
    private final Identifier textureId;
    @Getter
    private int textureWidth = CapeLayout.UNITS_WIDTH;
    @Getter
    private int textureHeight = CapeLayout.UNITS_HEIGHT;
    @Getter
    private boolean broken;
    private CapeTexture texture;

    FileCape(Path path) {
        this.path = path;
        String file = path.getFileName().toString();
        this.id = idFor(file);
        this.label = labelFor(file);
        this.textureId = Adin.id("textures/cape/custom/" + sanitize(file) + "_" + Integer.toHexString(file.hashCode()));
    }

    private static String idFor(String file) {
        return "custom/" + file;
    }

    private static String labelFor(String file) {
        boolean png = file.toLowerCase(Locale.ROOT).endsWith(EXTENSION);
        return png ? file.substring(0, file.length() - EXTENSION.length()) : file;
    }

    @Override
    public boolean animated() {
        return false;
    }

    public boolean rename(String newLabel) {
        String cleaned = cleanFileName(newLabel);
        if (cleaned.isEmpty() || cleaned.equals(label)) return false;
        Path target = path.resolveSibling(cleaned + EXTENSION);
        if (Files.exists(target)) return false;
        try {
            Files.move(path, target);
        } catch (Exception exception) {
            Adin.LOGGER.warn("Cannot rename cape {}", path, exception);
            return false;
        }
        path = target;
        label = cleaned;
        id = idFor(target.getFileName().toString());
        return true;
    }

    @Override
    public void update() {
        if (broken || texture != null) return;
        try (InputStream stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            if (!CapeLayout.isCape(image)) {
                NativeImage cape = CapeImage.toCape(image);
                image.close();
                image = cape;
            }
            texture = new CapeTexture(textureId, image);
            textureWidth = texture.width();
            textureHeight = texture.height();
        } catch (Exception exception) {
            broken = true;
            Adin.LOGGER.warn("Cannot load cape {}", path, exception);
        }
    }

    @Override
    public PlayerSkin apply(PlayerSkin skin) {
        update();
        return texture == null ? skin : texture.wear(skin);
    }

    private static String cleanFileName(String name) {
        StringBuilder out = new StringBuilder();
        for (char c : name.trim().toCharArray()) {
            if (ILLEGAL.indexOf(c) < 0) out.append(c);
        }
        return out.toString().trim();
    }

    private static String sanitize(String name) {
        StringBuilder out = new StringBuilder();
        for (char c : name.toLowerCase(Locale.ROOT).toCharArray()) {
            out.append(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.' || c == '_' || c == '-' ? c : '_');
        }
        return out.toString();
    }
}
