package dev.showcontrolessentials.block;

import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.network.ModNetwork;
import dev.showcontrolessentials.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MidiButtonBlockEntity extends BlockEntity {
    private static final String TAG_CONFIG = "Config";

    private MidiConfig config = MidiConfig.defaults();

    public MidiButtonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIDI_BUTTON.get(), pos, state);
    }

    public MidiConfig getConfig() {
        return config;
    }

    public void setConfig(MidiConfig newConfig) {
        config = newConfig.sanitized();
        setChanged();
    }

    public void sendPress() {
        if (level instanceof ServerLevel serverLevel) {
            ModNetwork.sendTrigger(serverLevel, worldPosition, config, 15, false);
        }
    }

    public void sendRelease() {
        if (level instanceof ServerLevel serverLevel) {
            ModNetwork.sendTrigger(serverLevel, worldPosition, config, 0, true);
        }
    }

    /**
     * Used when the block is broken mid-press so a Note On cannot stay latched.
     */
    public void sendStop() {
        BlockState state = getBlockState();
        if (state.hasProperty(MidiButtonBlock.POWERED) && state.getValue(MidiButtonBlock.POWERED)) {
            sendRelease();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_CONFIG, config.save());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlockState();
            if (state.hasProperty(MidiButtonBlock.POWERED) && state.getValue(MidiButtonBlock.POWERED)) {
                // A chunk was saved mid-press; make sure the Note Off still happens.
                serverLevel.scheduleTick(worldPosition, state.getBlock(), 1);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        config = tag.contains(TAG_CONFIG) ? MidiConfig.load(tag.getCompound(TAG_CONFIG)) : MidiConfig.defaults();
    }
}
