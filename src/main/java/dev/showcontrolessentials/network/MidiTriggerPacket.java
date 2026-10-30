package dev.showcontrolessentials.network;

import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.client.ClientMidiHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MidiTriggerPacket(BlockPos pos, MidiConfig config, int power, boolean wasPowered) {
    public MidiTriggerPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), MidiConfig.read(buffer), buffer.readUnsignedByte(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        config.write(buffer);
        buffer.writeByte(power);
        buffer.writeBoolean(wasPowered);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientMidiHandler.handle(pos, config, power, wasPowered)));
        context.get().setPacketHandled(true);
    }
}
