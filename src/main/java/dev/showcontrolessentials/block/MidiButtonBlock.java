package dev.showcontrolessentials.block;

import dev.showcontrolessentials.ButtonColor;
import dev.showcontrolessentials.menu.MidiInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * A vanilla-style stone button with a colour property and MIDI output. Sneak + right-click
 * opens the configuration; a plain right-click presses it.
 */
public class MidiButtonBlock extends ButtonBlock implements EntityBlock {
    public static final EnumProperty<ButtonColor> COLOR = EnumProperty.create("color", ButtonColor.class);
    private static final int PULSE_TICKS = 10;

    public MidiButtonBlock() {
        super(Properties.of()
                        .noCollission()
                        .strength(0.5F)
                        .sound(SoundType.STONE),
                BlockSetType.STONE, PULSE_TICKS, false);
        registerDefaultState(defaultBlockState().setValue(COLOR, ButtonColor.ORANGE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MidiButtonBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer
                    && level.getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                                (windowId, inventory, p) -> new MidiInterfaceMenu(windowId, inventory,
                                        buttonEntity, state.getValue(COLOR)),
                                Component.translatable("block.midiinterface.midi_button")),
                        buffer -> {
                            buffer.writeBlockPos(pos);
                            buffer.writeBoolean(true);
                            buffer.writeEnum(state.getValue(COLOR));
                            buttonEntity.getConfig().write(buffer);
                        });
            }
            return InteractionResult.CONSUME;
        }

        if (!state.getValue(POWERED)) {
            if (level.getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
                buttonEntity.sendPress();
            }
            // Vanilla handles the pressed state, click sound, neighbour updates and schedule.
            press(state, level, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean wasPowered = state.getValue(POWERED);
        super.tick(state, level, pos, random);
        if (wasPowered && level.getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
            buttonEntity.sendRelease();
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
            buttonEntity.sendStop();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
            buttonEntity.sendStop();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
