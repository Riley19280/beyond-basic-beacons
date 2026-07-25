package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import com.rileystech.beyondbasicbeacons.platform.services.IMenuTypeHelper;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class FabricMenuTypeHelper implements IMenuTypeHelper {

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerMenuType(MenuTypes.MenuSupplier<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.DEFAULT_FLAGS);
    }

    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> registerExtendedMenuType(
            MenuTypes.ExtendedMenuSupplier<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return new ExtendedMenuType<>(factory::create, codec);
    }
}