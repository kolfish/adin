package dev.koifih.client.module.impl.combat;

import dev.koifih.client.event.events.PreTickEvent;
import dev.koifih.client.module.Category;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.SliderSetting;
import dev.koifih.client.util.Game;
import dev.koifih.client.util.Hotbar;
import dev.koifih.client.util.Reach;
import dev.koifih.client.util.Time;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ShieldBreaker extends Module {
    private enum Stage {
        IDLE,
        SWITCHED,
        STRUCK,
        BROKEN,
        STUNNED
    }

    private static final long RESPONSE_MILLIS = 200L;
    private static final long STUN_MILLIS = 150L;
    private static final int CONFIRM_TICKS = 3;

    private final SliderSetting delay = add(new SliderSetting("delay", 100, 0, 500));
    private final BoolSetting facing = add(new BoolSetting("facing", true));
    private final BoolSetting stun = add(new BoolSetting("stun", true));
    private final BoolSetting switchBack = add(new BoolSetting("switchBack", true));
    private Stage stage = Stage.IDLE;
    private Player engaged;
    private int shieldedTicks;
    private final Time.Stopwatch sinceStage = new Time.Stopwatch();
    private int originalSlot = Hotbar.NONE;

    public ShieldBreaker() {
        super("shieldBreaker", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        listen(PreTickEvent.class, this::onTick);
    }

    @Override
    protected void onDisable() {
        reset(Game.player());
    }

    private void onTick(PreTickEvent event) {
        Minecraft client = event.client();
        LocalPlayer player = client.player;
        if (!Game.playing(client) || player.isBlocking() || (stage != Stage.IDLE && !engaged.isAlive())) {
            reset(player);
            return;
        }
        Player target = crosshairPlayer(client, player);
        shieldedTicks = target != null && shielded(player, target) ? shieldedTicks + 1 : 0;
        switch (stage) {
            case IDLE -> {
                if (shieldedTicks >= CONFIRM_TICKS) begin(player, target);
            }
            case SWITCHED -> strike(client, player, target);
            case STRUCK -> struck();
            case BROKEN -> stun(client, player);
            case STUNNED -> {
                if (elapsed(delay.get())) reset(player);
            }
        }
    }

    private void begin(LocalPlayer player, Player target) {
        int selected = Hotbar.selected(player);
        int axe = player.getMainHandItem().is(ItemTags.AXES) ? selected : Hotbar.find(player, stack -> stack.is(ItemTags.AXES));
        if (axe == Hotbar.NONE) return;
        engaged = target;
        originalSlot = axe == selected ? Hotbar.NONE : selected;
        Hotbar.select(player, axe);
        advance(Stage.SWITCHED);
    }

    private void strike(Minecraft client, LocalPlayer player, Player target) {
        if (!elapsed(delay.get())) return;
        if (target == engaged && shielded(player, target) && Reach.canHit(player, target)) {
            hit(client, player);
            advance(Stage.STRUCK);
        } else {
            reset(player);
        }
    }

    private void struck() {
        if (!engaged.isBlocking()) advance(stun.get() ? Stage.BROKEN : Stage.STUNNED);
        else if (elapsed(RESPONSE_MILLIS)) advance(Stage.STUNNED);
    }

    private void stun(Minecraft client, LocalPlayer player) {
        if (Reach.canHit(player, engaged)) hit(client, player);
        else if (!elapsed(STUN_MILLIS)) return;
        advance(Stage.STUNNED);
    }

    private void hit(Minecraft client, LocalPlayer player) {
        client.gameMode.attack(player, engaged);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private void reset(LocalPlayer player) {
        if (player != null && switchBack.get() && originalSlot != Hotbar.NONE) Hotbar.select(player, originalSlot);
        originalSlot = Hotbar.NONE;
        engaged = null;
        stage = Stage.IDLE;
    }

    private void advance(Stage next) {
        stage = next;
        sinceStage.reset();
    }

    private boolean elapsed(long millis) {
        return sinceStage.elapsed(millis);
    }

    private boolean shielded(LocalPlayer player, Player target) {
        return target.isBlocking() && (!facing.get() || facingUs(player, target));
    }

    private static boolean facingUs(LocalPlayer player, Player target) {
        Vec3 toPlayer = player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
        return target.getViewVector(1f).multiply(1.0, 0.0, 1.0).dot(toPlayer) > 0.0;
    }

    private static Player crosshairPlayer(Minecraft client, LocalPlayer player) {
        if (!(client.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player target)) return null;
        return target != player && target.isAlive() && !target.isSpectator() ? target : null;
    }
}
