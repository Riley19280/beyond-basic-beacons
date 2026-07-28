package com.rileystech.beyondbasicbeacons.mixin;

import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenuData;
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
// NeoForge build. Supplies the beacon's BlockPos and a snapshot of the current datapack-driven
// settings to the client alongside the vanilla menu-open packet, so the client-side BeaconMenu can
// look up its own local (already-synced) block entity and know the correct settings, instead of
// needing a custom packet round trip just to read data.
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityFabricMixin extends BlockEntity implements ExtendedMenuProvider<BeaconMenuData> {

    public BeaconBlockEntityFabricMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public BeaconMenuData getScreenOpeningData(ServerPlayer player) {
        return new BeaconMenuData(this.getBlockPos(), BeaconSettingsManager.getSettings(), BeaconEffectFilterManager.getAllowedEffectIds());
    }
}
