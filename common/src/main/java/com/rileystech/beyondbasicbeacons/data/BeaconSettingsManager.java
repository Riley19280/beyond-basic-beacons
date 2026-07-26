package com.rileystech.beyondbasicbeacons.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

// Reads a single datapack file at data/<namespace>/beacon_settings/*.json (datapacks should all
// target the same conventional path, e.g. beacon_settings/settings.json, so pack-priority override
// naturally yields exactly one entry - no merge logic needed here, unlike BeaconBaseBlockPointManager).
public class BeaconSettingsManager extends SimpleJsonResourceReloadListener<BeaconSettingsManager.Settings> {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("beacon_settings");

    private static volatile Settings settings = Settings.DEFAULT;

    // Must come after LISTER/settings - static fields initialize top-to-bottom, and the
    // constructor below reads LISTER, so INSTANCE would otherwise be built with a still-null LISTER.
    private static final BeaconSettingsManager INSTANCE = new BeaconSettingsManager();

    public record Settings(int minRange, int maxRange, int minAmplifier, int maxAmplifier,
                            int maxBeaconSize, int neighboringBeaconCheckRangeXZ, int neighboringBeaconCheckRangeY,
                            int maxEffectsPerBeacon) {
        public static final Settings DEFAULT = new Settings(1, 100, 0, 4, 5, 2, 1, 10);

        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("min_range").forGetter(Settings::minRange),
                        Codec.INT.fieldOf("max_range").forGetter(Settings::maxRange),
                        Codec.INT.fieldOf("min_amplifier").forGetter(Settings::minAmplifier),
                        Codec.INT.fieldOf("max_amplifier").forGetter(Settings::maxAmplifier),
                        Codec.INT.fieldOf("max_beacon_size").forGetter(Settings::maxBeaconSize),
                        Codec.INT.fieldOf("neighboring_beacon_check_range_xz").forGetter(Settings::neighboringBeaconCheckRangeXZ),
                        Codec.INT.fieldOf("neighboring_beacon_check_range_y").forGetter(Settings::neighboringBeaconCheckRangeY),
                        Codec.INT.fieldOf("max_effects_per_beacon").forGetter(Settings::maxEffectsPerBeacon)
                ).apply(instance, Settings::new)
        );
    }

    public BeaconSettingsManager() {
        super(Settings.CODEC, LISTER);
    }

    @Override
    protected void apply(Map<Identifier, Settings> data, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        settings = data.values().stream().findFirst().orElse(Settings.DEFAULT);
    }

    public static BeaconSettingsManager getInstance() {
        return INSTANCE;
    }

    public static Settings getSettings() {
        return settings;
    }
}
