package com.rileystech.beyondbasicbeacons.mixin;

import com.mojang.serialization.Codec;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
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
    private int neighboringBeacons = 0;

    @Unique
    private int baseBlockPoints = 0;

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

    @Override
    public int bbb_getBaseBlockPoints() {
        return this.baseBlockPoints;
    }

    @Override
    public void bbb_setBaseBlockPoints(int points) {
        this.baseBlockPoints = points;
    }

    @Override
    public int bbb_getNeighboringBeacons() {
        return this.neighboringBeacons;
    }

    @Override
    public void bbb_setNeighboringBeacons(int count) {
        this.neighboringBeacons = count;
    }

    private static final String BASE_BLOCK_POINTS_TAG = "baseBlockPoints";
    private static final String NEIGHBORING_BEACONS_TAG = "neighboringBeacons";
    private static final String EFFECTS_TAG = "effects";
    private static final String RANGE_TAG = "range";
    private static final String HIDE_PARTICLES_TAG = "hideParticles";

    private static final Codec<List<BeaconEffectEntry>> EFFECTS_CODEC = BeaconEffectEntry.CODEC.listOf();

    @Unique
    private List<BeaconEffectEntry> effects = new ArrayList<>();

    @Unique
    private int range = 1;

    @Unique
    private boolean hideParticles = false;

    @Override
    public int bbb_getLevels() {
        return this.levels;
    }

    @Override
    public List<BeaconEffectEntry> bbb_getEffects() {
        return this.effects;
    }

    @Override
    public void bbb_setEffects(List<BeaconEffectEntry> effects) {
        this.effects = effects;
    }

    @Override
    public int bbb_getRange() {
        return this.range;
    }

    @Override
    public void bbb_setRange(int range) {
        this.range = range;
    }

    @Override
    public boolean bbb_isHideParticles() {
        return this.hideParticles;
    }

    @Override
    public void bbb_setHideParticles(boolean hideParticles) {
        this.hideParticles = hideParticles;
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"), cancellable = true)
    protected void bbb_loadAdditional(ValueInput input, CallbackInfo ci) {
        this.effects = input.read(EFFECTS_TAG, EFFECTS_CODEC).orElseGet(ArrayList::new);
        this.range = input.getIntOr(RANGE_TAG, this.range);
        this.hideParticles = input.getBooleanOr(HIDE_PARTICLES_TAG, this.hideParticles);
        this.baseBlockPoints = input.getIntOr(BASE_BLOCK_POINTS_TAG, this.baseBlockPoints);
        this.neighboringBeacons = input.getIntOr(NEIGHBORING_BEACONS_TAG, this.neighboringBeacons);
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"), cancellable = true)
    protected void bbb_saveAdditional(ValueOutput output, CallbackInfo ci) {
        output.store(EFFECTS_TAG, EFFECTS_CODEC, this.effects);
        output.putInt(RANGE_TAG, this.range);
        output.putBoolean(HIDE_PARTICLES_TAG, this.hideParticles);
        output.putInt(BASE_BLOCK_POINTS_TAG, this.baseBlockPoints);
        output.putInt(NEIGHBORING_BEACONS_TAG, this.neighboringBeacons);
    }

    @Inject(method = "createMenu", at = @At("RETURN"), cancellable = true)
    private void bbb_addCustomBeaconMenu(int containerId, Inventory playerInventory, Player player, @NotNull CallbackInfoReturnable<AbstractContainerMenu> cir) {
        BeaconMenu menu = new BeaconMenu(containerId, playerInventory, ContainerLevelAccess.create(this.level, this.getBlockPos()));

        cir.setReturnValue(cir.getReturnValue() != null ? menu : null);
    }

    @Inject(method = "updateBase", at = @At("HEAD"), cancellable = true)
    private static void bbb_baseUpdating(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        BeaconSettingsManager.Settings settings = BeaconSettingsManager.getSettings();
        int levels = 0;
        int baseBlockPoints = 0;

        for (int step = 1; step <= settings.maxBeaconSize(); levels = step++) {
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

                    baseBlockPoints += BeaconBaseBlockPointManager.getBlockPointMap().getOrDefault(blockState.getBlock(), 0);
                }
            }

            if (!isOk) {
                break;
            }
        }

        int neighboringBeacons = 0;
        int checkRangeXZ = settings.neighboringBeaconCheckRangeXZ();
        int checkRangeY = settings.neighboringBeaconCheckRangeY();
        for (int nx = x - checkRangeXZ; nx <= x + checkRangeXZ; ++nx) {
            for (int ny = y - checkRangeY; ny <= y + checkRangeY; ++ny) {
                for (int nz = z - checkRangeXZ; nz <= z + checkRangeXZ; ++nz) {
                    if (nx == x && ny == y && nz == z) {
                        continue;
                    }
                    if (level.getBlockState(new BlockPos(nx, ny, nz)).is(Blocks.BEACON)) {
                        neighboringBeacons++;
                    }
                }
            }
        }

        baseBlockPoints /= neighboringBeacons + 1;

        BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));
        if (blockEntity instanceof NeighboringBeaconAccess beacon) {
            boolean changed = beacon.bbb_getBaseBlockPoints() != baseBlockPoints
                    || beacon.bbb_getNeighboringBeacons() != neighboringBeacons;
            beacon.bbb_setBaseBlockPoints(baseBlockPoints);
            beacon.bbb_setNeighboringBeacons(neighboringBeacons);
            if (changed) {
                blockEntity.setChanged();
            }
        }

        cir.setReturnValue(levels);
        cir.cancel();
    }

    // Replaces vanilla's fixed primary/secondary power application with our configurable list of
    // effects, using our own range/hideParticles instead of the vanilla levels-based range.
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void bbb_applyEffects(Level level, BlockPos worldPosition, int levels,
                                          @Nullable Holder<MobEffect> primaryPower, @Nullable Holder<MobEffect> secondaryPower,
                                          CallbackInfo ci) {
        ci.cancel();

        if (level.isClientSide()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(worldPosition);
        if (!(blockEntity instanceof NeighboringBeaconAccess beacon)) {
            return;
        }

        List<BeaconEffectEntry> effects = beacon.bbb_getEffects();
        if (effects.isEmpty()) {
            return;
        }

        double range = beacon.bbb_getRange();
        boolean showParticles = !beacon.bbb_isHideParticles();
        int durationTicks = (9 + levels * 2) * 20;

        AABB bb = new AABB(worldPosition).inflate(range)
                .expandTowards(0.0, level.getHeight(), 0.0)
                .expandTowards(0.0, -level.getMinY(), 0.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, bb);

        for (BeaconEffectEntry entry : effects) {
            ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, Identifier.parse(entry.key()));
            BuiltInRegistries.MOB_EFFECT.get(key).ifPresentOrElse(effect -> {
                if (!BeaconEffectFilterManager.isAllowed(effect.value())) {
                    Constants.LOG.info("Effect '{}' is no longer allowed by the effect filter - skipping", entry.key());
                    return;
                }
                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(effect, durationTicks, entry.amplifier(), true, showParticles));
                }
            }, () -> Constants.LOG.info("Could not resolve effect key '{}' to a registered MobEffect - skipping", entry.key()));
        }
    }
}
