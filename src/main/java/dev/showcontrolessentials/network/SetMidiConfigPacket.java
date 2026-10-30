package dev.showcontrolessentials.network;

import dev.showcontrolessentials.ButtonColor;
import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.block.MidiButtonBlock;
import dev.showcontrolessentials.block.MidiButtonBlockEntity;
import dev.showcontrolessentials.block.MidiInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetMidiConfigPacket(BlockPos pos, ButtonColor color, MidiConfig config) {
    private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0 * 64.0;

    public SetMidiConfigPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(ButtonColor.class), MidiConfig.read(buffer));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(color);
        config.write(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                if (player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_INTERACTION_DISTANCE_SQR) {
                    return;
                }

                if (player.level().getBlockEntity(pos) instanceof MidiInterfaceBlockEntity interfaceEntity) {
                    interfaceEntity.setConfig(config);
                } else if (player.level().getBlockEntity(pos) instanceof MidiButtonBlockEntity buttonEntity) {
                    buttonEntity.setConfig(config);

                    BlockState state = player.level().getBlockState(pos);
                    if (state.hasProperty(MidiButtonBlock.COLOR) && state.getValue(MidiButtonBlock.COLOR) != color) {
                        player.level().setBlock(pos, state.setValue(MidiButtonBlock.COLOR, color), Block.UPDATE_ALL);
                    }
                }
            });
        }
        ctx.setPacketHandled(true);
    }
}
