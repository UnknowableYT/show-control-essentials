package dev.showcontrolessentials;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> DEFAULT_PORT;
    private static final ForgeConfigSpec.IntValue DEFAULT_CHANNEL;
    private static final ForgeConfigSpec.EnumValue<MidiMessageType> DEFAULT_MESSAGE;
    private static final ForgeConfigSpec.IntValue DEFAULT_NUMBER;
    private static final ForgeConfigSpec.IntValue DEFAULT_VELOCITY;
    private static final ForgeConfigSpec.IntValue TRIGGER_RADIUS;
    private static final ForgeConfigSpec.BooleanValue DEBUG;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.comment("Redstone MIDI Interface settings").push("midi");

        DEFAULT_PORT = BUILDER
                .comment("Default MIDI output port name. Use the name shown in Windows' MIDI device list.",
                        "For grandMA3 onPC the usual setup is loopMIDI (e.g. \"MA2 LOOPMIDI\") selected as MIDI In Device.",
                        "You can change the port per block in its configuration screen.")
                .define("defaultPort", "MA2 LOOPMIDI");

        DEFAULT_CHANNEL = BUILDER
                .comment("Default MIDI channel, 1-16.")
                .defineInRange("defaultChannel", 1, 1, 16);

        DEFAULT_MESSAGE = BUILDER
                .comment("Default message type: NOTE, CONTROL_CHANGE or PROGRAM_CHANGE.")
                .defineEnum("defaultMessage", MidiMessageType.NOTE);

        DEFAULT_NUMBER = BUILDER
                .comment("Default note / controller / program number, 0-127.")
                .defineInRange("defaultNumber", 60, 0, 127);

        DEFAULT_VELOCITY = BUILDER
                .comment("Default velocity / control value for the 'on' state, 1-127.")
                .defineInRange("defaultVelocity", 127, 1, 127);

        TRIGGER_RADIUS = BUILDER
                .comment("Players within this distance (in blocks) receive the MIDI trigger and send it from their PC.")
                .defineInRange("triggerRadius", 64, 8, 512);

        DEBUG = BUILDER
                .comment("Log every MIDI message to the Minecraft log.")
                .define("logMidiMessages", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private Config() {
    }

    public static String defaultPort() {
        return value(DEFAULT_PORT, "MA2 LOOPMIDI");
    }

    public static int defaultChannel() {
        return value(DEFAULT_CHANNEL, 1);
    }

    public static MidiMessageType defaultMessage() {
        return value(DEFAULT_MESSAGE, MidiMessageType.NOTE);
    }

    public static int defaultNumber() {
        return value(DEFAULT_NUMBER, 60);
    }

    public static int defaultVelocity() {
        return value(DEFAULT_VELOCITY, 127);
    }

    public static int triggerRadius() {
        return value(TRIGGER_RADIUS, 64);
    }

    public static boolean debug() {
        return value(DEBUG, false);
    }

    private static <T> T value(ForgeConfigSpec.ConfigValue<T> configValue, T fallback) {
        try {
            return configValue.get();
        } catch (IllegalStateException ignored) {
            // Config is not loaded yet (or we are outside the game); use a sane default.
            return fallback;
        }
    }
}
