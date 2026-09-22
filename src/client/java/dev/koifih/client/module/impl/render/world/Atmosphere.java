package dev.koifih.client.module.impl.render.world;

import dev.koifih.Adin;
import dev.koifih.client.render.AdinIcon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum Atmosphere {
    SNOW("Snow", AdinIcon.SNOW, Biome.Precipitation.SNOW, null, null),
    RAIN("Rain", AdinIcon.RAIN, Biome.Precipitation.RAIN, null, null),
    THUNDER("Thunder", AdinIcon.THUNDER, Biome.Precipitation.RAIN, null, null),
    NETHER("Nether", AdinIcon.NETHER, Biome.Precipitation.NONE, DimensionType.Skybox.NONE, nether()),
    END("End", AdinIcon.END, Biome.Precipitation.NONE, DimensionType.Skybox.END, end()),
    DEEP_DARK("Deep Dark", AdinIcon.DEEP_DARK, Biome.Precipitation.NONE, DimensionType.Skybox.NONE, deepDark());

    private static final Set<EnvironmentAttribute<?>> AMBIENCE = BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.entrySet().stream()
            .filter(entry -> entry.getKey().identifier().getPath().matches("(visual|audio)/.*"))
            .map(entry -> entry.getValue())
            .collect(Collectors.toUnmodifiableSet());

    private final String label;
    private final AdinIcon icon;
    private final Biome.Precipitation precipitation;
    private final DimensionType.Skybox skybox;
    private final EnvironmentAttributeMap attributes;

    public Identifier image() {
        return Adin.id("textures/world/" + name().toLowerCase(Locale.ROOT) + ".png");
    }

    public float rain() {
        return precipitation == Biome.Precipitation.NONE ? 0f : 1f;
    }

    public float thunder() {
        return this == THUNDER ? 1f : 0f;
    }

    public <V> V apply(EnvironmentAttribute<V> attribute, V value) {
        if (attributes == null || !AMBIENCE.contains(attribute)) return value;
        return attributes.applyModifier(attribute, attribute.defaultValue());
    }

    private static EnvironmentAttributeMap nether() {
        return EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF330808)
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 10f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 96f)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0xFF7A7AFF)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0f)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF302821)
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.CRIMSON_SPORE, 0.0125f),
                        new AmbientParticle(ParticleTypes.ASH, 0.00625f)))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
                        Optional.of(SoundEvents.AMBIENT_NETHER_WASTES_LOOP),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0)),
                        List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
                .build();
    }

    private static EnvironmentAttributeMap end() {
        return EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF181318)
                .set(EnvironmentAttributes.SKY_COLOR, 0xFF000000)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0xFFAC60CD)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0f)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF3F473F)
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Musics.END))
                .build();
    }

    private static EnvironmentAttributeMap deepDark() {
        return EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, 0xFF060B0E)
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 40f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0f)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF0F171B)
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ParticleTypes.SCULK_CHARGE_POP, 0.01f)))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK))
                .build();
    }
}
