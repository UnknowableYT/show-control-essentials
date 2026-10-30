package dev.showcontrolessentials.client;

import dev.showcontrolessentials.MidiConfig;
import net.minecraft.core.BlockPos;

/**
 * Client-only entry point for network triggers. Keeping this in its own class means the server
 * never touches javax.sound.midi.
 */
public final class ClientMidiHandler {
    private ClientMidiHandler() {
    }

    public static void handle(BlockPos pos, MidiConfig config, int power, boolean wasPowered) {
        MidiService.handle(config, power, wasPowered);
    }
}
