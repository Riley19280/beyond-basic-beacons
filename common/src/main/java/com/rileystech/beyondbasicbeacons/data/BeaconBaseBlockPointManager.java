package com.rileystech.beyondbasicbeacons.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rileystech.beyondbasicbeacons.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BeaconBaseBlockPointManager extends SimpleJsonResourceReloadListener<BeaconBaseBlockPointManager.Entries> {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("beacon_base_block_points");

    private static final HashMap<Block, Integer> blockPointMap = new HashMap<>();

    // Must come after LISTER - static fields initialize top-to-bottom, and the constructor below
    // reads LISTER, so INSTANCE would otherwise be built with a still-null LISTER.
    private static final BeaconBaseBlockPointManager INSTANCE = new BeaconBaseBlockPointManager();

    public record Entry(String block, int points) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("block").forGetter(Entry::block),
                        Codec.INT.fieldOf("points").forGetter(Entry::points)
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

    public BeaconBaseBlockPointManager() {
        super(Entries.CODEC, LISTER);
    }

    @Override
    protected void apply(Map<Identifier, Entries> data, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        blockPointMap.clear();
        for (Entries entries : data.values()) {
            for (Entry entry : entries.values()) {
                Identifier blockId = Identifier.parse(entry.block());
                ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, blockId);
                BuiltInRegistries.BLOCK.get(key).ifPresentOrElse(block -> blockPointMap.put(block.value(), entry.points()),
                        () -> Constants.LOG.warn("BeaconBaseBlockPointManager: could not resolve block '{}' - skipping", entry.block()));
            }
        }
        Constants.LOG.info("BeaconBaseBlockPointManager: loaded {} file(s), {} block point entr{}",
                data.size(), blockPointMap.size(), blockPointMap.size() == 1 ? "y" : "ies");
    }

    public static BeaconBaseBlockPointManager getInstance() {
        return INSTANCE;
    }

    public static HashMap<Block, Integer> getBlockPointMap() {
        return blockPointMap;
    }
}
