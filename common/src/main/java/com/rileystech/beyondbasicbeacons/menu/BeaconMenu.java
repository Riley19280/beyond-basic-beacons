package com.rileystech.beyondbasicbeacons.menu;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public class BeaconMenu extends AbstractContainerMenu {

    public static String IDENTIFIER = "bbb_beacon_menu";

    private static final int NUM_DATA_SLOTS = 1;

    private final Container container = new SimpleContainer(1) {

        public boolean canPlaceItem(int index, ItemStack itemStack) {
            return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }
    };
    private final ContainerLevelAccess access;

    public BeaconMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainerData(NUM_DATA_SLOTS), ContainerLevelAccess.NULL);
    }

    public BeaconMenu(int containerId, Container container, ContainerData beaconData, ContainerLevelAccess access) {
        super(MenuTypes.BBB_BEACON_MENU, containerId);
        this.access = access;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.BEACON);
    }
}
