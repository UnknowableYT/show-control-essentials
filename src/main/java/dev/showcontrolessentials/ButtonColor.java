package dev.showcontrolessentials;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * The stage-friendly colour palette used by the MIDI Button block. The colour is a block
 * state, so changing it updates the model immediately on every client.
 */
public enum ButtonColor implements StringRepresentable {
    RED("red", 0xD9342C),
    ORANGE("orange", 0xE0701C),
    AMBER("amber", 0xE0A21C),
    YELLOW("yellow", 0xE6D21C),
    LIME("lime", 0x9ED82C),
    GREEN("green", 0x3FBF4F),
    MINT("mint", 0x3FD9A0),
    CYAN("cyan", 0x2FD9D9),
    AZURE("azure", 0x2F9FE0),
    BLUE("blue", 0x3355D9),
    VIOLET("violet", 0x7A4FE0),
    PURPLE("purple", 0xA23FD9),
    MAGENTA("magenta", 0xD92FBF),
    PINK("pink", 0xE85A9E),
    WARM_WHITE("warm_white", 0xF2D9B0),
    COOL_WHITE("cool_white", 0xDCE6F2);

    private final String id;
    private final int rgb;

    ButtonColor(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public int getRgb() {
        return rgb;
    }

    public String getTranslationKey() {
        return "midiinterface.color." + id;
    }

    public ButtonColor next() {
        ButtonColor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public ButtonColor previous() {
        ButtonColor[] values = values();
        return values[Math.floorMod(ordinal() - 1, values.length)];
    }

    public static ButtonColor byName(String name) {
        if (name != null) {
            String normalized = name.trim().toUpperCase(Locale.ROOT);
            for (ButtonColor color : values()) {
                if (color.name().equals(normalized) || color.id.equalsIgnoreCase(name)) {
                    return color;
                }
            }
        }
        return ORANGE;
    }
}
