package dev.showcontrolessentials.network;

import dev.showcontrolessentials.Config;
import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.MidiInterfaceMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.core.BlockPos;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int nextMessageId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MidiInterfaceMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(MidiTriggerPacket.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MidiTriggerPacket::encode)
                .decoder(MidiTriggerPacket::new)
                .consumerMainThread((packet, context) -> packet.handle(context))
                .add();

        CHANNEL.messageBuilder(SetMidiConfigPacket.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetMidiConfigPacket::encode)
                .decoder(SetMidiConfigPacket::new)
                .consumerMainThread((packet, context) -> packet.handle(context))
                .add();
    }

    /**
     * Sends a MIDI trigger to every player near the interface. MIDI is produced on the client,
     * because that is the machine connected to the lighting console.
     */
    public static void sendTrigger(ServerLevel level, BlockPos pos, MidiConfig config, int power, boolean wasPowered) {
        MidiTriggerPacket packet = new MidiTriggerPacket(pos, config, power, wasPowered);
        double radius = (double) Config.triggerRadius();
        double radiusSquared = radius * radius;
        Vec3 center = Vec3.atCenterOf(pos);

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) <= radiusSquared) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }
}
