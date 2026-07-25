package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import com.rileystech.beyondbasicbeacons.platform.services.IMenuTypeHelper;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuTypeHelperNeoForge implements IMenuTypeHelper {

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerMenuType(MenuTypes.MenuSupplier<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.DEFAULT_FLAGS);
    }
}