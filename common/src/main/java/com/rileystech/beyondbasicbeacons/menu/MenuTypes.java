package com.rileystech.beyondbasicbeacons.menu;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuTypes {

    public static final MenuType<BeaconMenu> BBB_BEACON_MENU = registerExtended(BeaconMenu.IDENTIFIER, BeaconMenu::new, BeaconMenuData.STREAM_CODEC);

    public static void register() {
        // no-op; referencing the class triggers the static field initializers above
    }

    private static <T extends AbstractContainerMenu, D> MenuType<T> registerExtended(
            String id, ExtendedMenuSupplier<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        MenuType<T> menuType = Services.MENU_TYPE.registerExtendedMenuType(factory, codec);
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Constants.MOD_ID, id), menuType);
    }

    @FunctionalInterface
    public interface MenuSupplier<T extends AbstractContainerMenu> {
        T create(int i, Inventory inventory);
    }

    @FunctionalInterface
    public interface ExtendedMenuSupplier<T extends AbstractContainerMenu, D> {
        T create(int containerId, Inventory inventory, D data);
    }
}
