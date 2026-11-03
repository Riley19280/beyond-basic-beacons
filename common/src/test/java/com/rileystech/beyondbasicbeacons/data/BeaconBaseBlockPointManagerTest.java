package com.rileystech.beyondbasicbeacons.data;

import com.rileystech.beyondbasicbeacons.MinecraftBootstrap;
import com.rileystech.beyondbasicbeacons.TestJson;
import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager.Entries;
import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rileystech.beyondbasicbeacons.TestJson.decode;
import static com.rileystech.beyondbasicbeacons.TestJson.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconBaseBlockPointManagerTest {

    private static final String BUNDLED_DATAPACK = "data/beyond_basic_beacons/beacon_base_block_points/beyond_basic_beacons.json";

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.init();
    }

    @AfterEach
    void reset() {
        load();
    }

    @SafeVarargs
    private static void load(List<Entry>... files) {
        Map<Identifier, Entries> data = new LinkedHashMap<>();
        for (int i = 0; i < files.length; i++) {
            data.put(Identifier.parse("test:points_" + i), new Entries(files[i]));
        }
        BeaconBaseBlockPointManager.getInstance().apply(data, null, null);
    }

    private static Integer points(Block block) {
        return BeaconBaseBlockPointManager.getBlockPointMap().get(block);
    }

    @Test
    void valuesIsRequired() {
        assertTrue(parse(Entries.CODEC, "{}").isError());
    }

    @Test
    void bundledDatapackMatchesReadme() {
        Entries bundled = decode(Entries.CODEC, TestJson.resource(BUNDLED_DATAPACK));
        BeaconBaseBlockPointManager.getInstance().apply(Map.of(Identifier.parse("beyond_basic_beacons:beyond_basic_beacons"), bundled), null, null);

        assertEquals(Map.of(
                Blocks.IRON_BLOCK, 1,
                Blocks.GOLD_BLOCK, 2,
                Blocks.EMERALD_BLOCK, 3,
                Blocks.DIAMOND_BLOCK, 3,
                Blocks.NETHERITE_BLOCK, 10
        ), BeaconBaseBlockPointManager.getBlockPointMap());
    }

    @Test
    void entriesAreMergedAcrossDatapacks() {
        load(List.of(new Entry("minecraft:iron_block", 1)), List.of(new Entry("minecraft:gold_block", 2)));
        assertEquals(1, points(Blocks.IRON_BLOCK));
        assertEquals(2, points(Blocks.GOLD_BLOCK));
    }

    @Test
    void laterEntryOverridesDuplicates() {
        load(List.of(new Entry("minecraft:iron_block", 1)), List.of(new Entry("minecraft:iron_block", 7)));
        assertEquals(7, points(Blocks.IRON_BLOCK));
    }

    @Test
    void unknownBlocksAreSkipped() {
        load(List.of(new Entry("minecraft:not_a_real_block", 5), new Entry("minecraft:iron_block", 1)));
        assertEquals(Map.of(Blocks.IRON_BLOCK, 1), BeaconBaseBlockPointManager.getBlockPointMap());
    }

    @Test
    void reloadClearsPreviousValues() {
        load(List.of(new Entry("minecraft:iron_block", 1)));
        load();
        assertNull(points(Blocks.IRON_BLOCK));
    }
}
