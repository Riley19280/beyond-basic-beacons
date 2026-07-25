package com.rileystech.beyondbasicbeacons.menu;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class BeaconMenu extends AbstractContainerMenu {

    public static String IDENTIFIER = "bbb_beacon_menu";

    private final Container container = new SimpleContainer(1) {

        public boolean canPlaceItem(int index, ItemStack itemStack) {
            return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }
    };
    private final ContainerLevelAccess access;

    // Client-side: constructed from the BlockPos carried alongside the vanilla menu-open packet
    // (see BeaconBlockEntityFabricMixin), pointing at the client's own local level.
    public BeaconMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    // Server-side: constructed from BeaconBlockEntityMixin.bbb_addCustomBeaconMenu.
    public BeaconMenu(int containerId, Container container, ContainerLevelAccess access) {
        super(MenuTypes.BBB_BEACON_MENU, containerId);
        this.access = access;
    }

    // The beacon's own data (effects/range/hideParticles/levels/points/neighbor count) lives on
    // the block entity itself and is kept in sync by vanilla's normal block-entity update packet
    // (BeaconBlockEntityMixin writes it via saveAdditional, which vanilla's getUpdateTag already
    // routes through) - so it's read directly here rather than duplicated onto this menu.
    public Optional<NeighboringBeaconAccess> getBeaconAccess() {
        return this.access.evaluate((level, pos) -> (NeighboringBeaconAccess) level.getBlockEntity(pos));
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
