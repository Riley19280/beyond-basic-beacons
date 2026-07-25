package com.rileystech.beyondbasicbeacons.platform.services;


import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuTypeHelper {
    <T extends AbstractContainerMenu> MenuType<T> registerMenuType(MenuTypes.MenuSupplier<T> factory);
}
