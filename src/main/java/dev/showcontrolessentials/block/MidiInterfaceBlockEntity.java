package dev.showcontrolessentials.block;

import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.network.ModNetwork;
import dev.showcontrolessentials.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MidiInterfaceBlockEntity extends BlockEntity {
    private static final String TAG_CONFIG = "Config";
    private static final String TAG_LAST_POWER = "LastPower";

    private MidiConfig config = MidiConfig.defaults();
    private int lastPower;

    public MidiInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIDI_INTERFACE.get(), pos, state);
    }

    public MidiConfig getConfig() {
        return config;
    }

    public int getLastPower() {
        return lastPower;
    }

    public void setConfig(MidiConfig newConfig) {
        MidiConfig sanitized = newConfig.sanitized();

        if (level instanceof ServerLevel serverLevel) {
            // Release anything that the old configuration may have left latched, then re-trigger.
            if (lastPower > 0) {
                ModNetwork.sendTrigger(serverLevel, worldPosition, config, 0, true);
            }
            config = sanitized;
            setChanged();
            if (lastPower > 0) {
                ModNetwork.sendTrigger(serverLevel, worldPosition, config, lastPower, false);
            }
        } else {
            config = sanitized;
            setChanged();
        }
    }

    /**
     * Called whenever a neighbouring block changes. Only sends anything when the analog redstone
     * level actually changed, so redstone dust updates cannot spam the MIDI port.
     */
    public void updateSignal() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int power = serverLevel.getBestNeighborSignal(worldPosition);
        if (power == lastPower) {
            return;
        }

        int previousPower = lastPower;
        lastPower = power;

        boolean powered = power > 0;
        BlockState state = getBlockState();
        if (state.hasProperty(MidiInterfaceBlock.POWERED)
                && state.getValue(MidiInterfaceBlock.POWERED) != powered) {
            serverLevel.setBlock(worldPosition, state.setValue(MidiInterfaceBlock.POWERED, powered), Block.UPDATE_ALL);
        }

        setChanged();
        ModNetwork.sendTrigger(serverLevel, worldPosition, config, power, previousPower > 0);
    }

    /**
     * Sends the "off" side of the configured message, if the block is currently active.
     */
    public void sendStop() {
        if (level instanceof ServerLevel serverLevel && lastPower > 0) {
            lastPower = 0;
            ModNetwork.sendTrigger(serverLevel, worldPosition, config, 0, true);
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_CONFIG, config.save());
        tag.putInt(TAG_LAST_POWER, lastPower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_CONFIG)) {
            config = MidiConfig.load(tag.getCompound(TAG_CONFIG));
        } else {
            config = MidiConfig.defaults();
        }
        lastPower = tag.getInt(TAG_LAST_POWER);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put(TAG_CONFIG, config.save());
        return tag;
    }
}
