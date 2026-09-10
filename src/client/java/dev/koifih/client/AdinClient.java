package dev.koifih.client;

import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.event.events.TickEvent;
import dev.koifih.client.input.Keybinds;
import dev.koifih.client.module.ModuleManager;
import dev.koifih.client.module.impl.combat.AimAssist;
import dev.koifih.client.module.impl.combat.AutoAnchor;
import dev.koifih.client.module.impl.combat.AutoCrystal;
import dev.koifih.client.module.impl.combat.AutoHitCrystal;
import dev.koifih.client.module.impl.combat.ShieldBreaker;
import dev.koifih.client.module.impl.combat.Triggerbot;
import dev.koifih.client.module.impl.hud.KeybindList;
import dev.koifih.client.module.impl.hud.ModuleList;
import dev.koifih.client.module.impl.hud.Notifications;
import dev.koifih.client.module.impl.hud.Watermark;
import dev.koifih.client.module.impl.movement.JumpReset;
import dev.koifih.client.module.impl.movement.MoveFix;
import dev.koifih.client.module.impl.movement.Sprint;
import dev.koifih.client.module.impl.player.AutoTotem;
import dev.koifih.client.module.impl.player.KeyPearl;
import dev.koifih.client.module.impl.render.BlockEsp;
import dev.koifih.client.module.impl.render.Nametags;
import dev.koifih.client.module.impl.render.esp.Esp;
import dev.koifih.client.render.screen.ScreenRenderer;
import dev.koifih.client.render.blocks.BlockIndex;
import dev.koifih.client.render.blocks.BlockRenderer;
import dev.koifih.client.render.world.WorldRenderer;
import dev.koifih.client.rotation.RotationManager;
import dev.koifih.client.ui.hud.HudEditor;
import dev.koifih.client.ui.preview.EntityPreview;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class AdinClient implements ClientModInitializer {
    public static final EventBus EVENTS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final RotationManager ROTATIONS = new RotationManager();

    @Override
    public void onInitializeClient() {
        MODULES.register(new AimAssist());
        MODULES.register(new Triggerbot());
        MODULES.register(new ShieldBreaker());
        MODULES.register(new AutoHitCrystal());
        MODULES.register(new AutoCrystal());
        MODULES.register(new AutoAnchor());
        MODULES.register(new Sprint());
        MODULES.register(new KeyPearl());
        MODULES.register(new AutoTotem());
        MODULES.register(new MoveFix());
        MODULES.register(new JumpReset());
        MODULES.register(new Esp());
        MODULES.register(new Nametags());
        MODULES.register(new BlockEsp());
        MODULES.register(new Notifications());
        MODULES.register(new ModuleList());
        MODULES.register(new Watermark());
        MODULES.register(new KeybindList());
        Keybinds.register();
        ROTATIONS.init();
        HudEditor.init();
        WorldRenderer.init();
        BlockIndex.init(BlockRenderer::onSectionChanged);
        BlockRenderer.init();
        ScreenRenderer.init();
        EVENTS.subscribe(PreTickEvent.class, event -> MODULES.tickKeybinds(event.client()));
        ClientTickEvents.START_CLIENT_TICK.register(client -> EVENTS.post(new PreTickEvent(client)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> EVENTS.post(new TickEvent(client)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EntityPreview.clear());
    }
}
