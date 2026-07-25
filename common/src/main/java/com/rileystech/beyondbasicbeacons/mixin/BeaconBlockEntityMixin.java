package com.rileystech.beyondbasicbeacons.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import com.rileystech.beyondbasicbeacons.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity implements NeighboringBeaconAccess {
    @Unique
    private static int MAX_BEACON_SIZE = 10;
    @Unique
    private static int NEIGHBORING_BEACON_CHECK_RANGE_XZ = 2;
    @Unique
    private static int NEIGHBORING_BEACON_CHECK_RANGE_Y = 1;

    @Unique
    private int neighboringBeaconCount = 0;

    @Unique
    private int baseBlockPointTotal = 0;

    @Shadow
    private LockCode lockKey;
    @Shadow
    int levels;
    @Shadow
    @Final
    private ContainerData dataAccess;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Shadow
    public abstract Component getDisplayName();

    @Override
    public int bbb_getBaseBlockPointTotal() {
        return this.baseBlockPointTotal;
    }

    @Override
    public void bbb_setBaseBlockPointTotal(int total) {
        this.baseBlockPointTotal = total;
    }

    @Override
    public int bbb_getNeighboringBeaconCount() {
        return this.neighboringBeaconCount;
    }

    @Override
    public void bbb_setNeighboringBeaconCount(int count) {
        this.neighboringBeaconCount = count;
    }

    private static final String EFFECTS_TAG = "effects";

    public record Entry(String key, int value) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("effect").forGetter(Entry::key),
                        Codec.INT.fieldOf("amplifier").forGetter(Entry::value)
                ).apply(instance, Entry::new)
        );
    }

    private static final Codec<List<Entry>> EFFECTS_CODEC = Entry.CODEC.listOf();

    @Unique
    private List<Entry> effects = new ArrayList<>();

    @Inject(method = "loadAdditional", at = @At("RETURN"), cancellable = true)
    protected void bbb_loadAdditional(ValueInput input, CallbackInfo ci) {
         this.effects = new ArrayList<>(input.read(EFFECTS_TAG, EFFECTS_CODEC).orElseGet(List::of));
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"), cancellable = true)
    protected void bbb_saveAdditional(ValueOutput output, CallbackInfo ci) {
        output.store(EFFECTS_TAG, EFFECTS_CODEC, effects);
    }

    @Inject(method = "createMenu", at = @At("RETURN"), cancellable = true)
    private void bbb_addCustomBeaconMenu(int containerId, Inventory playerInventory, Player player, @NotNull CallbackInfoReturnable<AbstractContainerMenu> cir) {
        BeaconMenu menu = new BeaconMenu(containerId, playerInventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()));
        menu.setTotal

        cir.setReturnValue(cir.getReturnValue() != null ? menu : null);
    }

    @Inject(method = "updateBase", at = @At("HEAD"), cancellable = true)
    private static void bbb_baseUpdating(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        int levels = 0;
        int baseBlockPointTotal = 0;

        for (int step = 1; step <= BeaconBlockEntityMixin.MAX_BEACON_SIZE; levels = step++) {
            int ly = y - step;
            if (ly < level.getMinY()) {
                break;
            }

            boolean isOk = true;

            for (int lx = x - step; lx <= x + step && isOk; ++lx) {
                for (int lz = z - step; lz <= z + step; ++lz) {
                    BlockState blockState = level.getBlockState(new BlockPos(lx, ly, lz));
                    if (!blockState.is(BlockTags.BEACON_BASE_BLOCKS)) {
                        isOk = false;
                        break;
                    }
                    Constants.LOG.debug("Block state is {}", blockState.typeHolder().getRegisteredName());
                    switch (blockState.typeHolder().getRegisteredName()) {
                        case "minecraft:iron_block":
                            baseBlockPointTotal += 1;
                        case "minecraft:gold_block":
                            baseBlockPointTotal += 2;
                        case "minecraft:diamond_block":
                            baseBlockPointTotal += 3;
                        case "minecraft:emerald_block":
                            baseBlockPointTotal += 3;
                        case "minecraft:netherite_block":
                            baseBlockPointTotal += 10;
                    }
                }
            }

            if (!isOk) {
                break;
            }
        }

        int neighboringBeaconCount = 0;
        for (int nx = x - NEIGHBORING_BEACON_CHECK_RANGE_XZ; nx <= x + NEIGHBORING_BEACON_CHECK_RANGE_XZ; ++nx) {
            for (int ny = y - NEIGHBORING_BEACON_CHECK_RANGE_Y; ny <= y + NEIGHBORING_BEACON_CHECK_RANGE_Y; ++ny) {
                for (int nz = z - NEIGHBORING_BEACON_CHECK_RANGE_XZ; nz <= z + NEIGHBORING_BEACON_CHECK_RANGE_XZ; ++nz) {
                    if (nx == x && ny == y && nz == z) {
                        continue;
                    }
                    if (level.getBlockState(new BlockPos(nx, ny, nz)).is(Blocks.BEACON)) {
                        neighboringBeaconCount++;
                    }
                }
            }
        }

        if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof NeighboringBeaconAccess beacon) {
            beacon.bbb_setBaseBlockPointTotal(baseBlockPointTotal);
            beacon.bbb_setNeighboringBeaconCount(neighboringBeaconCount);
        }

        cir.setReturnValue(levels);
    }
}
