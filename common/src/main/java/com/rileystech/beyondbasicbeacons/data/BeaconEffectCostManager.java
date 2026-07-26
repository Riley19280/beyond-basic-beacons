package com.rileystech.beyondbasicbeacons.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Reads datapack files under data/<namespace>/beacon_effect_costs/*.json, each shaped as:
// { "values": [ { "effect": "minecraft:strength", "multiplier": 1.5 }, ... ] }
// Structural mirror of BeaconBaseBlockPointManager, keyed by MobEffect instead of Block.
public class BeaconEffectCostManager extends SimpleJsonResourceReloadListener<BeaconEffectCostManager.Entries> {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("beacon_effect_costs");

    private static final HashMap<MobEffect, Double> multiplierMap = new HashMap<>();

    // Must come after LISTER - static fields initialize top-to-bottom, and the constructor below
    // reads LISTER, so INSTANCE would otherwise be built with a still-null LISTER.
    private static final BeaconEffectCostManager INSTANCE = new BeaconEffectCostManager();

    public record Entry(String effect, double multiplier) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("effect").forGetter(Entry::effect),
                        Codec.DOUBLE.fieldOf("multiplier").forGetter(Entry::multiplier)
                ).apply(instance, Entry::new)
        );
    }

    public record Entries(List<Entry> values) {
        public static final Codec<Entries> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Entry.CODEC.listOf().fieldOf("values").forGetter(Entries::values)
                ).apply(instance, Entries::new)
        );
    }

    public BeaconEffectCostManager() {
        super(Entries.CODEC, LISTER);
    }

    @Override
    protected void apply(Map<Identifier, Entries> data, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        multiplierMap.clear();
        for (Entries entries : data.values()) {
            for (Entry entry : entries.values()) {
                Identifier effectId = Identifier.parse(entry.effect());
                ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, effectId);
                BuiltInRegistries.MOB_EFFECT.get(key).ifPresent(effect -> multiplierMap.put(effect.value(), entry.multiplier()));
            }
        }
    }

    public static BeaconEffectCostManager getInstance() {
        return INSTANCE;
    }

    public static double getMultiplier(MobEffect effect) {
        return multiplierMap.getOrDefault(effect, 1.0);
    }
}
