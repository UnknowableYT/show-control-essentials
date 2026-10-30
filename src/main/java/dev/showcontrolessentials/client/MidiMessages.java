package dev.showcontrolessentials.client;

import dev.showcontrolessentials.MidiMessageType;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * Pure MIDI message construction, kept separate from device handling so it can be tested
 * without a running game.
 */
public final class MidiMessages {
    private MidiMessages() {
    }

    public static ShortMessage build(MidiMessageType type, int channel, int number, int velocity,
                                     boolean analog, int power, boolean wasPowered) throws InvalidMidiDataException {
        int midiChannel = Math.max(0, Math.min(15, channel - 1));
        int clampedPower = Math.max(0, Math.min(15, power));

        return switch (type) {
            case NOTE -> {
                if (clampedPower > 0 && !wasPowered) {
                    yield new ShortMessage(ShortMessage.NOTE_ON, midiChannel, number, velocity);
                }
                if (clampedPower == 0 && wasPowered) {
                    yield new ShortMessage(ShortMessage.NOTE_OFF, midiChannel, number, 0);
                }
                yield null;
            }
            case CONTROL_CHANGE -> {
                int value = analog
                        ? Math.round(clampedPower * 127.0F / 15.0F)
                        : (clampedPower > 0 ? velocity : 0);
                yield new ShortMessage(ShortMessage.CONTROL_CHANGE, midiChannel, number, value);
            }
            case PROGRAM_CHANGE -> (clampedPower > 0 && !wasPowered)
                    ? new ShortMessage(ShortMessage.PROGRAM_CHANGE, midiChannel, number, 0)
                    : null;
        };
    }
}
