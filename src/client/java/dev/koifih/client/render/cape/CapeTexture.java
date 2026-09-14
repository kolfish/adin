package dev.koifih.client.render.cape;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

@Accessors(fluent = true)
public final class CapeTexture {
    private record Asset(Identifier id) implements ClientAsset.Texture {
        @Override
        public Identifier texturePath() {
            return id;
        }
    }

    private final DynamicTexture texture;
    private final ClientAsset.Texture asset;
    @Getter
    private final int width;
    @Getter
    private final int height;

    public CapeTexture(Identifier id, NativeImage pixels) {
        this.width = pixels.getWidth();
        this.height = pixels.getHeight();
        this.texture = new DynamicTexture(id::toString, pixels);
        this.asset = new Asset(id);
        Minecraft.getInstance().getTextureManager().register(id, texture);
    }

    public NativeImage pixels() {
        return texture.getPixels();
    }

    public void upload() {
        texture.upload();
    }

    public PlayerSkin wear(PlayerSkin skin) {
        return new PlayerSkin(skin.body(), asset, skin.elytra(), skin.model(), skin.secure());
    }
}
