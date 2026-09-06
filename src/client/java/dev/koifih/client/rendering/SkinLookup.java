package dev.koifih.client.rendering;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinLookup {
    public enum Status {
        LOADING,
        READY,
        MISSING
    }

    private static final Map<String, CompletableFuture<Optional<PlayerSkin>>> CACHE = new ConcurrentHashMap<>();

    private SkinLookup() {}

    public static void request(String name) {
        String key = key(name);
        if (key.isEmpty()) return;
        CACHE.computeIfAbsent(key, k -> fetch(name.trim()));
    }

    public static Status status(String name) {
        CompletableFuture<Optional<PlayerSkin>> future = CACHE.get(key(name));
        if (future == null || !future.isDone()) return Status.LOADING;
        return skin(future).isPresent() ? Status.READY : Status.MISSING;
    }

    public static PlayerSkin skin(String name) {
        CompletableFuture<Optional<PlayerSkin>> future = CACHE.get(key(name));
        return future == null || !future.isDone() ? null : skin(future).orElse(null);
    }

    private static CompletableFuture<Optional<PlayerSkin>> fetch(String name) {
        Minecraft minecraft = Minecraft.getInstance();
        return CompletableFuture.supplyAsync(() -> minecraft.services().profileResolver().fetchByName(name), Util.nonCriticalIoPool())
                .thenCompose(profile -> profile.map(SkinLookup::load).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())))
                .exceptionally(exception -> Optional.empty());
    }

    private static CompletableFuture<Optional<PlayerSkin>> load(GameProfile profile) {
        return Minecraft.getInstance().getSkinManager().get(profile);
    }

    private static Optional<PlayerSkin> skin(CompletableFuture<Optional<PlayerSkin>> future) {
        try {
            return future.join();
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
