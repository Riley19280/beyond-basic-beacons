package com.rileystech.beyondbasicbeacons.mixin;

import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenuData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

// NeoForge counterpart of BeaconBlockEntityFabricMixin. NeoForge's ServerPlayer.openMenu calls
// MenuProvider#writeClientSideData (from IMenuProviderExtension) when vanilla's BeaconBlock opens the
// menu, and the client's menu factory (MenuTypeHelperNeoForge) decodes it back into BeaconMenuData -
// giving the client the beacon's position, the server's settings, and the allowed effect list.
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityNeoForgeMixin extends BlockEntity {

    public BeaconBlockEntityNeoForgeMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        BeaconMenuData.STREAM_CODEC.encode(buffer, new BeaconMenuData(
                this.getBlockPos(), BeaconSettingsManager.getSettings(), BeaconEffectFilterManager.getAllowedEffectIds()));
    }
}
