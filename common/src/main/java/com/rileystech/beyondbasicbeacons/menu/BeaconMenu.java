package com.rileystech.beyondbasicbeacons.menu;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BeaconMenu extends AbstractContainerMenu {

    public static String IDENTIFIER = "bbb_beacon_menu";

    private final Container container = new SimpleContainer(1) {

        public boolean canPlaceItem(int index, ItemStack itemStack) {
            return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }
    };
    private final ContainerLevelAccess access;
    private final BeaconSettingsManager.Settings settings;
    private final List<Holder.Reference<MobEffect>> allowedEffects;

    // Client-side: constructed from the BlockPos+Settings+allowedEffects carried alongside the
    // vanilla menu-open packet (see BeaconBlockEntityFabricMixin), pointing at the client's own
    // local level. Settings/allowedEffects are carried explicitly here rather than read live via
    // BeaconSettingsManager/BeaconEffectFilterManager on the client - those managers' reload
    // listeners only ever run on the logical server, so the client's own copy would otherwise
    // always be Settings.DEFAULT / an unfiltered effect list.
    public BeaconMenu(int containerId, Inventory inventory, BeaconMenuData data) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), data.pos()), data.settings(), data.allowedEffects());
    }

    // Server-side: constructed from BeaconBlockEntityMixin.bbb_addCustomBeaconMenu.
    public BeaconMenu(int containerId, Container container, ContainerLevelAccess access) {
        this(containerId, container, access, BeaconSettingsManager.getSettings(), BeaconEffectFilterManager.getAllowedEffectIds());
    }

    private BeaconMenu(int containerId, Container container, ContainerLevelAccess access,
                        BeaconSettingsManager.Settings settings, List<String> allowedEffectIds) {
        super(MenuTypes.BBB_BEACON_MENU, containerId);
        this.access = access;
        this.settings = settings;
        this.allowedEffects = resolveEffects(allowedEffectIds);
    }

    private static List<Holder.Reference<MobEffect>> resolveEffects(List<String> effectIds) {
        List<Holder.Reference<MobEffect>> resolved = new ArrayList<>();
        for (String effectId : effectIds) {
            ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, Identifier.parse(effectId));
            BuiltInRegistries.MOB_EFFECT.get(key).ifPresent(resolved::add);
        }
        return resolved;
    }

    // The beacon's own data (effects/range/hideParticles/levels/points/neighbor count) lives on
    // the block entity itself and is kept in sync by vanilla's normal block-entity update packet
    // (BeaconBlockEntityMixin writes it via saveAdditional, which vanilla's getUpdateTag already
    // routes through) - so it's read directly here rather than duplicated onto this menu.
    public Optional<NeighboringBeaconAccess> getBeaconAccess() {
        return this.access.evaluate((level, pos) -> (NeighboringBeaconAccess) level.getBlockEntity(pos));
    }

    public BeaconSettingsManager.Settings getSettings() {
        return this.settings;
    }

    public List<Holder.Reference<MobEffect>> getAllowedEffects() {
        return this.allowedEffects;
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
