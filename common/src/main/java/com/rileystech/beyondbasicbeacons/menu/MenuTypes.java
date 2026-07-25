package com.rileystech.beyondbasicbeacons.menu;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuTypes {

    public static final MenuType<BeaconMenu> BBB_BEACON_MENU = register(BeaconMenu.IDENTIFIER, BeaconMenu::new);

    public static void register() {
        // no-op; referencing the class triggers the static field initializers above
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(String id, MenuSupplier<T> factory) {
        MenuType<T> menuType = Services.MENU_TYPE.registerMenuType(factory);
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Constants.MOD_ID, id), menuType);
    }

    @FunctionalInterface
    public interface MenuSupplier<T extends AbstractContainerMenu> {
        T create(int i, Inventory inventory);
    }
}