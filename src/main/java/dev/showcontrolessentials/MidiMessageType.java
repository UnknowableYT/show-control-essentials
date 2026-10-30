package dev.showcontrolessentials;

import java.util.Locale;

public enum MidiMessageType {
    NOTE("note"),
    CONTROL_CHANGE("control_change"),
    PROGRAM_CHANGE("program_change");

    private final String id;

    MidiMessageType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return "midiinterface.message." + id;
    }

    public MidiMessageType next() {
        MidiMessageType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static MidiMessageType byName(String name) {
        if (name != null) {
            String normalized = name.trim().toUpperCase(Locale.ROOT);
            for (MidiMessageType type : values()) {
                if (type.name().equals(normalized) || type.id.equalsIgnoreCase(name)) {
                    return type;
                }
            }
        }
        return NOTE;
    }
}
