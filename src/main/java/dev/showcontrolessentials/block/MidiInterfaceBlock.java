package dev.showcontrolessentials.block;

import dev.showcontrolessentials.menu.MidiInterfaceMenu;
import dev.showcontrolessentials.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class MidiInterfaceBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public MidiInterfaceBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(POWERED) ? 7 : 0));
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MidiInterfaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                            (windowId, inventory, p) -> new MidiInterfaceMenu(windowId, inventory, interfaceEntity),
                            state.getBlock().getName()),
                    buffer -> {
                        buffer.writeBlockPos(pos);
                        buffer.writeBoolean(false);
                        buffer.writeEnum(dev.showcontrolessentials.ButtonColor.ORANGE);
                        interfaceEntity.getConfig().write(buffer);
                    });
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
            interfaceEntity.updateSignal();
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            // Evaluate the signal shortly after placement (e.g. when placed onto a live redstone line).
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
            interfaceEntity.updateSignal();
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
            // Prevent stuck notes / a CC that stays latched after the block is broken.
            interfaceEntity.sendStop();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
            interfaceEntity.sendStop();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
