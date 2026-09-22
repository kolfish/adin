package dev.koifih.client.module.impl.render.world;

import dev.koifih.client.event.events.TickEvent;
import dev.koifih.client.module.Module;
import dev.koifih.client.setting.EnumSetting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.client.resources.sounds.DirectionalSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;

public final class WorldModifier extends Module {
    private static final Atmosphere[] ATMOSPHERES = Atmosphere.values();
    private static final float WEATHER_STEP = 0.05f;
    private static final int FLASH_SOUND_DELAY = 30;
    private static final int MIN_STRIKE_DELAY = 80;
    private static final int MAX_STRIKE_DELAY = 300;
    private static final int STRIKE_RANGE = 64;

    private final EnumSetting theme = add(new EnumSetting("theme", 0,
            Arrays.stream(ATMOSPHERES).map(Atmosphere::label).toArray(String[]::new))
            .withArt(Arrays.stream(ATMOSPHERES).map(Atmosphere::icon).toList(),
                    Arrays.stream(ATMOSPHERES).map(Atmosphere::image).toList()));
    private final EndFlashState flashes = new EndFlashState();
    private float weather;
    private float previousWeather;
    private int strikeDelay;
    private int nextBoltId = -1_000_000;

    public WorldModifier() {
        super("worldModifier");
    }

    @Override
    public String info() {
        return theme.selected();
    }

    public Atmosphere atmosphere() {
        return isEnabled() ? ATMOSPHERES[theme.get()] : null;
    }

    public float rainLevel(float partialTicks, float vanilla) {
        Atmosphere atmosphere = atmosphere();
        return atmosphere == null ? vanilla : blend(partialTicks, vanilla, atmosphere.rain());
    }

    public float thunderLevel(float partialTicks, float vanilla) {
        Atmosphere atmosphere = atmosphere();
        return atmosphere == null ? vanilla : blend(partialTicks, vanilla, atmosphere.thunder());
    }

    public EndFlashState flashes() {
        return atmosphere() == Atmosphere.END ? flashes : null;
    }

    private float blend(float partialTicks, float vanilla, float target) {
        return Mth.lerp(Mth.lerp(partialTicks, previousWeather, weather), vanilla, target);
    }

    @Override
    protected void onEnable() {
        weather = 0f;
        previousWeather = 0f;
        strikeDelay = MIN_STRIKE_DELAY;
        listen(TickEvent.class, this::onTick);
    }

    private void onTick(TickEvent event) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        previousWeather = weather;
        weather = Math.min(1f, weather + WEATHER_STEP);
        Atmosphere atmosphere = atmosphere();
        if (atmosphere == Atmosphere.END) tickFlashes(level);
        if (atmosphere == Atmosphere.THUNDER && --strikeDelay <= 0) strike(level);
    }

    private void tickFlashes(ClientLevel level) {
        flashes.tick(level.getDefaultClockTime());
        if (!flashes.flashStartedThisTick() || level.endFlashState() != flashes) return;
        mc.getSoundManager().playDelayed(new DirectionalSoundInstance(SoundEvents.WEATHER_END_FLASH, SoundSource.WEATHER,
                level.getRandom(), mc.gameRenderer.mainCamera(), flashes.getXAngle(), flashes.getYAngle()), FLASH_SOUND_DELAY);
    }

    private void strike(ClientLevel level) {
        RandomSource random = level.getRandom();
        strikeDelay = Mth.randomBetweenInclusive(random, MIN_STRIKE_DELAY, MAX_STRIKE_DELAY);
        BlockPos column = mc.player.blockPosition().offset(random.nextInt(STRIKE_RANGE * 2) - STRIKE_RANGE, 0,
                random.nextInt(STRIKE_RANGE * 2) - STRIKE_RANGE);
        if (!level.hasChunkAt(column)) return;
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
        if (bolt == null) return;
        bolt.setVisualOnly(true);
        bolt.snapTo(Vec3.atBottomCenterOf(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column)));
        bolt.setId(nextBoltId--);
        level.addEntity(bolt);
    }
}
