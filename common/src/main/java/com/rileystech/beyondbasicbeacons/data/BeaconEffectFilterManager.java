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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Reads datapack files under data/<namespace>/beacon_effect_filters/*.json, each shaped as:
// { "blacklist": ["minecraft:instant_damage"], "whitelist": [] }
// Unlike BeaconSettingsManager (one canonical object), every loaded file contributes to the
// blacklist/whitelist - they're unioned across all files, same merge style as BeaconBaseBlockPointManager.
public class BeaconEffectFilterManager extends SimpleJsonResourceReloadListener<BeaconEffectFilterManager.Entries> {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("beacon_effect_filters");

    private static Set<MobEffect> blacklist = Set.of();
    private static Set<MobEffect> whitelist = Set.of();

    // Must come after LISTER - static fields initialize top-to-bottom, and the constructor below
    // reads LISTER, so INSTANCE would otherwise be built with a still-null LISTER.
    private static final BeaconEffectFilterManager INSTANCE = new BeaconEffectFilterManager();

    public record Entries(List<String> blacklist, List<String> whitelist) {
        public static final Codec<Entries> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.listOf().optionalFieldOf("blacklist", List.of()).forGetter(Entries::blacklist),
                        Codec.STRING.listOf().optionalFieldOf("whitelist", List.of()).forGetter(Entries::whitelist)
                ).apply(instance, Entries::new)
        );
    }

    public BeaconEffectFilterManager() {
        super(Entries.CODEC, LISTER);
    }

    @Override
    protected void apply(Map<Identifier, Entries> data, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Set<MobEffect> newBlacklist = new HashSet<>();
        Set<MobEffect> newWhitelist = new HashSet<>();
        for (Entries entries : data.values()) {
            resolveInto(entries.blacklist(), newBlacklist);
            resolveInto(entries.whitelist(), newWhitelist);
        }
        blacklist = newBlacklist;
        whitelist = newWhitelist;
    }

    private static void resolveInto(List<String> effectIds, Set<MobEffect> target) {
        for (String effectId : effectIds) {
            Identifier id = Identifier.parse(effectId);
            ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id);
            BuiltInRegistries.MOB_EFFECT.get(key).ifPresent(effect -> target.add(effect.value()));
        }
    }

    public static BeaconEffectFilterManager getInstance() {
        return INSTANCE;
    }

    public static boolean isAllowed(MobEffect effect) {
        if (!whitelist.isEmpty() && !whitelist.contains(effect)) {
            return false;
        }
        return !blacklist.contains(effect);
    }

    // Server-only (this manager's reload listener is PackType.SERVER_DATA, so a real client's own
    // copy is always empty) - used to hand the resolved allowed-effect list to the client via
    // BeaconMenuData at menu-open time, instead of the client trying to filter with its own
    // (always-empty) blacklist/whitelist.
    public static List<String> getAllowedEffectIds() {
        return BuiltInRegistries.MOB_EFFECT.listElements()
                .filter(effect -> isAllowed(effect.value()))
                .map(effect -> effect.key().identifier().toString())
                .toList();
    }
}
