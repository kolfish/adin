package dev.koifih.client.bench;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.mojang.blaze3d.platform.NativeImage;
import dev.koifih.Adin;
import dev.koifih.client.AdinClient;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.CapeSetting;
import dev.koifih.client.setting.ColorSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.ui.UiScale;
import dev.koifih.client.ui.clickgui.ClickGui;
import dev.koifih.client.ui.component.TabButton;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Probe {
    private static final Path TRIGGER = Path.of("adin-probe.json");
    private static final String DIRECTORY = "align";
    private static final String[] ENABLED = {"arraylist", "watermark", "keybinds", "notifications", "sprint", "esp", "nametags", "capes"};
    private static final List<String> COMMANDS = List.of("time set noon", "weather clear",
            "gamerule doDaylightCycle false", "gamerule doWeatherCycle false");

    private record Step(String shot, int settleMillis, Consumer<Minecraft> action) {}

    private enum Stage { TITLE, LOADING, SETTLE, RUN, DONE }

    private static Stage stage = Stage.TITLE;
    private static long stageStart = System.currentTimeMillis();
    private static List<Step> steps = List.of();
    private static int index;
    private static boolean acted;

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
                client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                daylight(client);
                for (String id : ENABLED) AdinClient.MODULES.get(id).setEnabled(true);
                steps = script();
                enter(Stage.RUN);
            }
            case RUN -> run(client, elapsed);
            case DONE -> {
            }
        }
    }

    private static void run(Minecraft client, long elapsed) {
        if (index >= steps.size()) {
            finish(client);
            return;
        }
        Step step = steps.get(index);
        if (!acted) {
            acted = true;
            stageStart = System.currentTimeMillis();
            step.action().accept(client);
            return;
        }
        if (elapsed < step.settleMillis()) return;
        if (step.shot() != null) shoot(client, step.shot());
        index++;
        acted = false;
        stageStart = System.currentTimeMillis();
    }

    private static List<Step> script() {
        List<Step> script = new ArrayList<>();
        script.add(new Step("hud", 2500, client -> client.gui.setScreen(null)));
        script.add(new Step("hud-notification", 800, client -> module("sprint").toggle()));
        script.add(new Step("hud-sky", 1500, client -> {
            client.options.setCameraType(CameraType.FIRST_PERSON);
            client.player.setXRot(-90f);
        }));
        script.add(new Step("gui-combat", 1600, client -> {
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            client.player.setXRot(0f);
            openGui(client);
        }));
        for (String tab : List.of("movement", "render", "hud", "player", "misc", "friends", "configs")) {
            script.add(new Step("gui-" + tab, 1100, client -> click(client, byTab(tab))));
        }
        script.add(new Step("gui-client-settings", 1200, client -> click(client, byLabel("Client settings"))));
        script.add(new Step(null, 900, Probe::openGui));
        script.add(new Step(null, 1100, client -> click(client, byTab("render"))));
        script.add(new Step("gui-esp-settings", 1500, Probe::openEspSettings));
        script.add(new Step("gui-esp-dropdown", 1100, client -> clickSetting(client, EnumSetting.class)));
        script.add(new Step(null, 900, Probe::openGui));
        script.add(new Step(null, 1400, Probe::openEspSettings));
        script.add(new Step("gui-esp-color", 1200, client -> clickSetting(client, ColorSetting.class)));
        script.add(new Step(null, 900, Probe::openGui));
        script.add(new Step("gui-capes", 1600, Probe::openCapes));
        script.add(new Step(null, 900, Probe::openGui));
        script.add(new Step("gui-capes-settings", 1500,
                client -> click(client, byLabel(module("capes").name() + " settings"))));
        for (UiScale scale : UiScale.ALL) {
            script.add(new Step("gui-scale-" + Math.round(scale.factor() * 100), 1700, client -> {
                UiScale.set(scale);
                openGui(client);
            }));
        }
        script.add(new Step("sky-gui-modules", 1700, client -> {
            UiScale.set(UiScale.PERCENT_100);
            client.options.setCameraType(CameraType.FIRST_PERSON);
            client.player.setXRot(-90f);
            openGui(client);
        }));
        script.add(new Step("sky-gui-capes", 1700, Probe::openCapes));
        script.add(new Step("sky-gui-scale-125", 1800, client -> {
            UiScale.set(UiScale.PERCENT_125);
            openGui(client);
        }));
        return List.copyOf(script);
    }

    private static void daylight(Minecraft client) {
        IntegratedServer server = client.getSingleplayerServer();
        if (server == null) return;
        server.execute(() -> {
            CommandSourceStack source = server.createCommandSourceStack();
            for (String command : COMMANDS) server.getCommands().performPrefixedCommand(source, command);
        });
    }

    private static Module module(String id) {
        return AdinClient.MODULES.get(id);
    }

    private static void openGui(Minecraft client) {
        client.gui.setScreen(new ClickGui());
    }

    private static void openEspSettings(Minecraft client) {
        click(client, byLabel(module("esp").name() + " settings"));
    }

    private static void openCapes(Minecraft client) {
        if (!(client.gui.screen() instanceof ClickGui gui)) return;
        for (Setting<?> setting : module("capes").settings()) {
            if (setting instanceof CapeSetting cape) gui.openCapeCatalog(cape);
        }
    }

    private static void clickSetting(Minecraft client, Class<?> type) {
        for (Setting<?> setting : module("esp").settings()) {
            if (!type.isInstance(setting) || !setting.isVisible()) continue;
            click(client, byLabel(setting.name()));
            return;
        }
        Adin.LOGGER.warn("[probe] no visible setting of {}", type.getSimpleName());
    }

    private static Predicate<AbstractWidget> byTab(String id) {
        return widget -> widget instanceof TabButton button && button.tab().id().equals(id);
    }

    private static Predicate<AbstractWidget> byLabel(String label) {
        return widget -> widget.getMessage().getString().equals(label);
    }

    private static void click(Minecraft client, Predicate<AbstractWidget> match) {
        if (!(client.gui.screen() instanceof ClickGui gui)) return;
        for (GuiEventListener child : gui.children()) {
            if (!(child instanceof AbstractWidget widget) || !match.test(widget)) continue;
            double x = widget.getX() + widget.getWidth() / 2.0;
            double y = widget.getY() + widget.getHeight() / 2.0;
            gui.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), false);
            return;
        }
        Adin.LOGGER.warn("[probe] no widget matched");
    }

    private static void shoot(Minecraft client, String name) {
        Screenshot.takeScreenshot(client.gameRenderer.mainRenderTarget(), image -> {
            try (NativeImage captured = image) {
                Path path = client.gameDirectory.toPath().resolve("screenshots").resolve(DIRECTORY).resolve(name + ".png");
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
