package dev.koifih.client.render.cape;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public interface Cape {
    String id();

    String label();

    boolean animated();

    void update();

    PlayerSkin apply(PlayerSkin skin);

    Identifier textureId();

    int textureWidth();

    int textureHeight();

    default boolean broken() {
        return false;
    }
}
