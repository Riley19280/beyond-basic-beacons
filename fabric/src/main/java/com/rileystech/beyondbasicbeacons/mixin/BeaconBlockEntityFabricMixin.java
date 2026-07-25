package com.rileystech.beyondbasicbeacons.mixin;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

// Separate from the common BeaconBlockEntityMixin because ExtendedMenuProvider is a Fabric-only
// type (net.fabricmc.fabric.api.menu.v1) - common code can't reference it without breaking the
// NeoForge build. Supplies the beacon's BlockPos to the client alongside the vanilla menu-open
// packet, so the client-side BeaconMenu can look up its own local (already-synced) block entity
// instead of needing a custom packet round trip just to read data.
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityFabricMixin extends BlockEntity implements ExtendedMenuProvider<BlockPos> {

    public BeaconBlockEntityFabricMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos();
    }
}
