package dev.showcontrolessentials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * The per-block MIDI configuration. Immutable, and always sanitised before use.
 */
public record MidiConfig(String port, int channel, MidiMessageType type, int number, int velocity, boolean analog) {
    public static final int MAX_PORT_LENGTH = 128;

    public MidiConfig {
        port = port == null ? "" : port;
        type = type == null ? MidiMessageType.NOTE : type;
    }

    public static MidiConfig defaults() {
        return new MidiConfig(
                Config.defaultPort(),
                Config.defaultChannel(),
                Config.defaultMessage(),
                Config.defaultNumber(),
                Config.defaultVelocity(),
                false
        ).sanitized();
    }

    public MidiConfig sanitized() {
        String cleanPort = port == null ? "" : port.trim();
        cleanPort = cleanPort.replaceAll("[\\p{Cntrl}]", "");
        if (cleanPort.length() > MAX_PORT_LENGTH) {
            cleanPort = cleanPort.substring(0, MAX_PORT_LENGTH);
        }

        return new MidiConfig(
                cleanPort,
                clamp(channel, 1, 16),
                type == null ? MidiMessageType.NOTE : type,
                clamp(number, 0, 127),
                clamp(velocity, 1, 127),
                analog
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(port, MAX_PORT_LENGTH);
        buf.writeByte(channel);
        buf.writeEnum(type);
        buf.writeByte(number);
        buf.writeByte(velocity);
        buf.writeBoolean(analog);
    }

    public static MidiConfig read(FriendlyByteBuf buf) {
        return new MidiConfig(
                buf.readUtf(MAX_PORT_LENGTH),
                buf.readUnsignedByte(),
                buf.readEnum(MidiMessageType.class),
                buf.readUnsignedByte(),
                buf.readUnsignedByte(),
                buf.readBoolean()
        ).sanitized();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("port", port);
        tag.putInt("channel", channel);
        tag.putString("type", type.name());
        tag.putInt("number", number);
        tag.putInt("velocity", velocity);
        tag.putBoolean("analog", analog);
        return tag;
    }

    public static MidiConfig load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return defaults();
        }
        return new MidiConfig(
                tag.getString("port"),
                tag.getInt("channel"),
                MidiMessageType.byName(tag.getString("type")),
                tag.getInt("number"),
                tag.getInt("velocity"),
                tag.getBoolean("analog")
        ).sanitized();
    }
}
