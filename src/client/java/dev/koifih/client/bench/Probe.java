package dev.koifih.client.bench;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.mojang.blaze3d.platform.NativeImage;
import dev.koifih.Adin;
import dev.koifih.client.AdinClient;
import dev.koifih.client.ui.clickgui.ClickGui;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Probe {
    private static final Path TRIGGER = Path.of("adin-probe.json");
    private static final String[] ENABLED = {"arraylist", "watermark", "keybinds", "notifications", "sprint", "esp", "nametags"};

    private enum Stage { TITLE, LOADING, SETTLE, HUD, GUI, DONE }

    private static Stage stage = Stage.TITLE;
    private static long stageStart = System.currentTimeMillis();

    public static void init() {
        if (!Files.exists(TRIGGER)) return;
        ClientTickEvents.END_CLIENT_TICK.register(Probe::tick);
    }

    private static void tick(Minecraft client) {
        long elapsed = System.currentTimeMillis() - stageStart;
        switch (stage) {
            case TITLE -> {
                if (!(client.gui.screen() instanceof TitleScreen)) return;
                client.createWorldOpenFlows().openWorld("New World (1)", () -> finish(client));
                enter(Stage.LOADING);
            }
            case LOADING -> {
                if (client.player != null && client.level != null && client.gui.screen() == null) enter(Stage.SETTLE);
                else if (elapsed > 120_000) finish(client);
            }
            case SETTLE -> {
                if (elapsed < 4000) return;
                client.options.pauseOnLostFocus = false;
                for (String id : ENABLED) AdinClient.MODULES.get(id).setEnabled(true);
                enter(Stage.HUD);
            }
            case HUD -> {
                if (elapsed < 2000) return;
                shoot(client, "adin-probe-hud.png");
                client.gui.setScreen(new ClickGui());
                enter(Stage.GUI);
            }
            case GUI -> {
                if (elapsed < 1500) return;
                shoot(client, "adin-probe-gui.png");
                finish(client);
            }
            case DONE -> {
            }
        }
    }

    private static void shoot(Minecraft client, String name) {
        Screenshot.takeScreenshot(client.gameRenderer.mainRenderTarget(), image -> {
            try (NativeImage captured = image) {
                Path path = client.gameDirectory.toPath().resolve("screenshots").resolve(name);
                Files.createDirectories(path.getParent());
                captured.writeToFile(path);
                Adin.LOGGER.info("[probe] wrote {}", path);
            } catch (IOException exception) {
                Adin.LOGGER.error("[probe] screenshot failed", exception);
            }
        });
    }

    private static void enter(Stage next) {
        stage = next;
        stageStart = System.currentTimeMillis();
    }

    private static void finish(Minecraft client) {
        if (stage == Stage.DONE) return;
        enter(Stage.DONE);
        try {
            Files.deleteIfExists(TRIGGER);
        } catch (IOException ignored) {
        }
        client.stop();
    }
}
