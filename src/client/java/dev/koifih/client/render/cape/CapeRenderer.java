package dev.koifih.client.render.cape;

import dev.koifih.Adin;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.util.Colors;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

@Accessors(fluent = true)
public final class CapeRenderer implements Cape {
    @Getter
    private final String id;
    @Getter
    private final boolean animated;
    @Getter
    private final Identifier textureId;
    private CapeTexture texture;
    private long lastKey = Long.MIN_VALUE;

    public CapeRenderer(String id, boolean animated) {
        this.id = id;
        this.animated = animated;
        this.textureId = Adin.id("textures/cape/" + id);
    }

    @Override
    public String label() {
        return id.replace('_', ' ');
    }

    @Override
    public int textureWidth() {
        return CapeLayout.UNITS_WIDTH * CapeArt.SCALE;
    }

    @Override
    public int textureHeight() {
        return CapeLayout.UNITS_HEIGHT * CapeArt.SCALE;
    }

    @Override
    public void update() {
        init();
        int accent = Colors.opaque(Theme.ACCENT);
        float seconds = CapeArt.seconds(animated);
        long key = ((accent & 0xFFFFFFFFL) << 24) | CapeArt.frame(animated, seconds);
        if (key == lastKey) return;
        lastKey = key;
        CapeLayout.stamp(texture.pixels(), CapeArt.paint(seconds, accent), CapeArt.SCALE);
        texture.upload();
    }

    @Override
    public PlayerSkin apply(PlayerSkin skin) {
        init();
        return texture.wear(skin);
    }

    private void init() {
        if (texture == null) {
            texture = new CapeTexture(textureId, CapeLayout.canvas(CapeArt.SCALE, CapeArt.BACKGROUND));
        }
    }
}
