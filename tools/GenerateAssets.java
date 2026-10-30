import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates the mod's 16x16 textures, block models, blockstates and logo. Run from the
 * project root:
 *   java tools/GenerateAssets.java
 */
public final class GenerateAssets {
    private static final File ASSETS = new File("src/main/resources/assets/showcontrolessentials");
    private static final File BLOCK_TEXTURES = new File(ASSETS, "textures/block");
    private static final File MODELS = new File(ASSETS, "models/block");
    private static final File ITEM_MODELS = new File(ASSETS, "models/item");
    private static final File BLOCKSTATES = new File(ASSETS, "blockstates");

    private record Palette(String id, int rgb) {
    }

    // Keep this in sync with ButtonColor.java.
    private static final Palette[] BUTTON_COLORS = {
            new Palette("red", 0xD9342C),
            new Palette("orange", 0xE0701C),
            new Palette("amber", 0xE0A21C),
            new Palette("yellow", 0xE6D21C),
            new Palette("lime", 0x9ED82C),
            new Palette("green", 0x3FBF4F),
            new Palette("mint", 0x3FD9A0),
            new Palette("cyan", 0x2FD9D9),
            new Palette("azure", 0x2F9FE0),
            new Palette("blue", 0x3355D9),
            new Palette("violet", 0x7A4FE0),
            new Palette("purple", 0xA23FD9),
            new Palette("magenta", 0xD92FBF),
            new Palette("pink", 0xE85A9E),
            new Palette("warm_white", 0xF2D9B0),
            new Palette("cool_white", 0xDCE6F2)
    };

    private static final String[] FACINGS = {"north", "east", "south", "west"};
    private static final int[] FLOOR_Y = {0, 90, 180, 270};
    private static final int[] CEILING_Y = {180, 270, 0, 90};
    private static final int[] WALL_Y = {0, 90, 180, 270};

    public static void main(String[] args) throws Exception {
        BLOCK_TEXTURES.mkdirs();
        MODELS.mkdirs();
        ITEM_MODELS.mkdirs();
        BLOCKSTATES.mkdirs();

        ImageIO.write(renderInterfaceTile(false), "png", new File(BLOCK_TEXTURES, "midi_interface.png"));
        ImageIO.write(renderInterfaceTile(true), "png", new File(BLOCK_TEXTURES, "midi_interface_on.png"));
        ImageIO.write(renderLogo(), "png", new File(ASSETS, "logo.png"));

        writeButtonAssets();

        System.out.println("Wrote textures, models and blockstates to " + ASSETS.getAbsolutePath());
    }

    /**
     * A Theatrical Redstone Interface-style tile: dark metal border, a recessed panel
     * (orange instead of redstone red), and a grey socket in the middle.
     */
    private static BufferedImage renderInterfaceTile(boolean on) {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(on ? 0x4D494449L : 0x4944494DL);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int color;

                if (x == 0 || y == 0 || x == size - 1 || y == size - 1) {
                    color = (x == 0 || y == 0) ? rgb(58, 58, 58) : rgb(27, 27, 27);
                } else if (x == 1 || y == 1 || x == size - 2 || y == size - 2) {
                    color = (x == 1 || y == 1) ? rgb(95, 95, 95) : rgb(72, 72, 72);
                } else if (x == 2 || y == 2 || x == size - 3 || y == size - 3) {
                    color = (x == 2 || y == 2) ? rgb(112, 112, 112) : rgb(86, 86, 86);
                } else if (x == 3 || y == 3 || x == size - 4 || y == size - 4) {
                    color = rgb(48, 48, 48);
                } else {
                    int baseR = on ? 242 : 196;
                    int baseG = on ? 244 : 200;
                    int baseB = on ? 248 : 206;
                    int noise = random.nextInt(13) - 6;

                    int r = clamp(baseR + noise + ((x == 4 || y == 4) ? 8 : 0));
                    int g = clamp(baseG + noise + ((x == 4 || y == 4) ? 8 : 0));
                    int b = clamp(baseB + noise + ((x == 4 || y == 4) ? 6 : 0));
                    color = rgb(r, g, b);
                }

                image.setRGB(x, y, color);
            }
        }

        // Grey socket in the middle, with a light top-left bevel.
        for (int y = 6; y <= 9; y++) {
            for (int x = 6; x <= 9; x++) {
                int color;
                if (x == 6 || y == 6) {
                    color = rgb(108, 108, 108);
                } else if (x == 9 || y == 9) {
                    color = rgb(59, 59, 59);
                } else {
                    color = on ? rgb(250, 250, 250) : rgb(150, 150, 150);
                }
                image.setRGB(x, y, color);
            }
        }

        int dot = on ? rgb(255, 255, 255) : rgb(110, 110, 110);
        image.setRGB(7, 7, dot);
        image.setRGB(8, 8, dot);

        return image;
    }

    /**
     * Coloured stone for the vanilla-style button model. 16x16, no HD.
     */
    private static BufferedImage renderButtonTexture(int rgb, boolean on) {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(0x4254544FL ^ rgb ^ (on ? 0x1000 : 0));

        float scale = on ? 1.14F : 0.82F;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int noise = random.nextInt(19) - 9;
                int r = clamp(Math.round(red(rgb) * scale) + noise);
                int g = clamp(Math.round(green(rgb) * scale) + noise);
                int b = clamp(Math.round(blue(rgb) * scale) + noise);

                // Coloured-stone speckles and a simple bevel.
                if (random.nextInt(9) == 0) {
                    r = clamp(r - 18);
                    g = clamp(g - 18);
                    b = clamp(b - 18);
                }
                if (x == 0 || y == 0) {
                    r = clamp(r + 12);
                    g = clamp(g + 12);
                    b = clamp(b + 12);
                }
                if (x == size - 1 || y == size - 1) {
                    r = clamp(r - 14);
                    g = clamp(g - 14);
                    b = clamp(b - 14);
                }

                image.setRGB(x, y, rgb(r, g, b));
            }
        }

        // A couple of darker cracks so it reads as stone rather than flat plastic.
        image.setRGB(4, 9, darken(image.getRGB(4, 9), 18));
        image.setRGB(5, 9, darken(image.getRGB(5, 9), 12));
        image.setRGB(11, 5, darken(image.getRGB(11, 5), 16));

        return image;
    }

    private static void writeButtonAssets() throws Exception {
        List<String> variants = new ArrayList<>();

        for (Palette palette : BUTTON_COLORS) {
            ImageIO.write(renderButtonTexture(palette.rgb(), false), "png",
                    new File(BLOCK_TEXTURES, "midi_button_" + palette.id() + ".png"));
            ImageIO.write(renderButtonTexture(palette.rgb(), true), "png",
                    new File(BLOCK_TEXTURES, "midi_button_" + palette.id() + "_on.png"));

            writeModel("midi_button_" + palette.id(), "midi_button_" + palette.id(), "minecraft:block/button");
            writeModel("midi_button_" + palette.id() + "_on", "midi_button_" + palette.id() + "_on",
                    "minecraft:block/button_pressed");

            for (boolean powered : new boolean[]{false, true}) {
                for (int i = 0; i < FACINGS.length; i++) {
                    variants.add(buttonVariant(palette.id(), "floor", FACINGS[i], powered, 0, FLOOR_Y[i], false));
                    variants.add(buttonVariant(palette.id(), "ceiling", FACINGS[i], powered, 180, CEILING_Y[i], false));
                    variants.add(buttonVariant(palette.id(), "wall", FACINGS[i], powered, 90, WALL_Y[i], true));
                }
            }
        }

        String blockstate = "{\n  \"variants\": {\n" + String.join(",\n", variants) + "\n  }\n}\n";
        Files.writeString(new File(BLOCKSTATES, "midi_button.json").toPath(), blockstate, StandardCharsets.UTF_8);

        String itemModel = "{\n  \"parent\": \"minecraft:block/button_inventory\",\n  \"textures\": {\n"
                + "    \"texture\": \"showcontrolessentials:block/midi_button_red\"\n  }\n}\n";
        Files.writeString(new File(ITEM_MODELS, "midi_button.json").toPath(), itemModel, StandardCharsets.UTF_8);
    }

    private static String buttonVariant(String color, String face, String facing, boolean powered,
                                        int x, int y, boolean uvlock) {
        String model = "showcontrolessentials:block/midi_button_" + color + (powered ? "_on" : "");
        StringBuilder builder = new StringBuilder();
        builder.append("    \"color=").append(color)
                .append(",face=").append(face)
                .append(",facing=").append(facing)
                .append(",powered=").append(powered)
                .append("\": {\n      \"model\": \"").append(model).append('"');

        if (uvlock) {
            builder.append(",\n      \"uvlock\": true");
        }
        if (x != 0) {
            builder.append(",\n      \"x\": ").append(x);
        }
        if (y != 0) {
            builder.append(",\n      \"y\": ").append(y);
        }
        return builder.append("\n    }").toString();
    }

    private static void writeModel(String modelName, String textureName, String parent) throws Exception {
        String model = "{\n  \"parent\": \"" + parent + "\",\n  \"textures\": {\n    \"texture\": "
                + "\"showcontrolessentials:block/" + textureName + "\"\n  }\n}\n";
        Files.writeString(new File(MODELS, modelName + ".json").toPath(), model, StandardCharsets.UTF_8);
    }

    private static BufferedImage renderLogo() throws Exception {
        int size = 128;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setColor(new Color(0x11, 0x12, 0x16));
        graphics.fill(new RoundRectangle2D.Float(2, 2, size - 4, size - 4, 18, 18));
        graphics.setColor(new Color(0x3D, 0x3F, 0x49));
        graphics.setStroke(new BasicStroke(2));
        graphics.draw(new RoundRectangle2D.Float(3, 3, size - 6, size - 6, 18, 18));

        int cx = 64;
        int cy = 50;
        graphics.setColor(new Color(0x74, 0x77, 0x82));
        graphics.setStroke(new BasicStroke(7));
        graphics.draw(new Ellipse2D.Float(cx - 30, cy - 30, 60, 60));
        graphics.setColor(new Color(0x17, 0x18, 0x1D));
        graphics.fill(new Ellipse2D.Float(cx - 25, cy - 25, 50, 50));
        graphics.setColor(new Color(0xC0, 0x50, 0x10));
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(210 + i * 30);
            int px = (int) Math.round(cx + Math.cos(angle) * 17);
            int py = (int) Math.round(cy + Math.sin(angle) * 17);
            graphics.fill(new Ellipse2D.Float(px - 2.5f, py - 2.5f, 5, 5));
        }

        graphics.setColor(new Color(0xC0, 0x50, 0x10));
        graphics.fill(new RoundRectangle2D.Float(88, 18, 16, 16, 4, 4));
        graphics.setColor(new Color(0xFF, 0xA5, 0x70));
        graphics.fill(new RoundRectangle2D.Float(91, 21, 6, 5, 2, 2));

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics.setColor(new Color(0xB8, 0xBA, 0xC4));
        drawCentered(graphics, "SHOW CONTROL", 94);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        graphics.setColor(new Color(0xF2, 0xF3, 0xF7));
        drawCentered(graphics, "ESSENTIALS", 114);

        graphics.dispose();
        return image;
    }

    private static void drawCentered(Graphics2D graphics, String text, int y) {
        int width = graphics.getFontMetrics().stringWidth(text);
        graphics.drawString(text, (128 - width) / 2, y);
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    private static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    private static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }

    private static int lighten(int color, int amount) {
        return rgb(clamp(red(color) + amount), clamp(green(color) + amount), clamp(blue(color) + amount));
    }

    private static int darken(int color, int amount) {
        return rgb(clamp(red(color) - amount), clamp(green(color) - amount), clamp(blue(color) - amount));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
