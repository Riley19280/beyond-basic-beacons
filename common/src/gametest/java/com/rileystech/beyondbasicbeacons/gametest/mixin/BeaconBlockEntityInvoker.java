package com.rileystech.beyondbasicbeacons.gametest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Vanilla only calls updateBase/applyEffects every 80 ticks, and only while the beacon has a beam
// (sky access) - GameTests are enclosed in barriers, so call them directly instead. These still run
// through BeaconBlockEntityMixin's HEAD injections, which is exactly the code under test.
@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityInvoker {
    @Invoker("updateBase")
    static int bbbtest_updateBase(Level level, int x, int y, int z) {
        throw new AssertionError("mixin not applied");
    }

    @Invoker("applyEffects")
    static void bbbtest_applyEffects(Level level, BlockPos pos, int levels,
                                     @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
        throw new AssertionError("mixin not applied");
    }
}
