package dev.showcontrolessentials.client;

import dev.showcontrolessentials.ButtonColor;
import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.MidiMessageType;
import dev.showcontrolessentials.menu.MidiInterfaceMenu;
import dev.showcontrolessentials.network.ModNetwork;
import dev.showcontrolessentials.network.SetMidiConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class MidiInterfaceScreen extends AbstractContainerScreen<MidiInterfaceMenu> {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 224;

    private EditBox portBox;
    private EditBox numberBox;
    private EditBox velocityBox;
    private Button findButton;
    private Button channelButton;
    private Button messageButton;
    private Button analogButton;
    private Button colorButton;

    private List<String> ports = List.of();
    private int portIndex;
    private int channel;
    private MidiMessageType messageType;
    private boolean analog;
    private final boolean button;
    private ButtonColor color;

    public MidiInterfaceScreen(MidiInterfaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.button = menu.isButton();
        this.color = menu.getColor();
        this.imageHeight = button ? 248 : HEIGHT;
        MidiConfig config = menu.getConfig();
        this.channel = config.channel();
        this.messageType = config.type();
        this.analog = config.analog();
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - imageHeight) / 2;
    }

    @Override
    protected void init() {
        super.init();

        MidiConfig config = menu.getConfig();
        ports = MidiService.outputPorts();
        portIndex = indexOfPort(config.port());

        int left = left();
        int top = top();

        portBox = new EditBox(font, left + 112, top + 40, 140, 18, Component.translatable("midiinterface.gui.port"));
        portBox.setMaxLength(MidiConfig.MAX_PORT_LENGTH);
        portBox.setValue(config.port().isEmpty() && !ports.isEmpty() ? ports.get(0) : config.port());
        portBox.setHint(Component.translatable("midiinterface.gui.port_hint"));
        addRenderableWidget(portBox);

        findButton = addRenderableWidget(Button.builder(Component.translatable("midiinterface.gui.find"), button -> cyclePort())
                .bounds(left + 258, top + 40, 50, 18)
                .build());

        channelButton = addRenderableWidget(Button.builder(channelLabel(), button -> {
            channel = channel % 16 + 1;
            button.setMessage(channelLabel());
        }).bounds(left + 112, top + 64, 78, 18).build());

        messageButton = addRenderableWidget(Button.builder(messageLabel(), button -> {
            messageType = messageType.next();
            updateWidgets();
        }).bounds(left + 112, top + 88, 108, 18).build());

        numberBox = new EditBox(font, left + 112, top + 112, 52, 18, Component.empty());
        numberBox.setMaxLength(3);
        numberBox.setFilter(value -> value.isEmpty() || value.matches("\\d{1,3}"));
        numberBox.setValue(Integer.toString(config.number()));
        addRenderableWidget(numberBox);

        velocityBox = new EditBox(font, left + 232, top + 112, 52, 18, Component.empty());
        velocityBox.setMaxLength(3);
        velocityBox.setFilter(value -> value.isEmpty() || value.matches("\\d{1,3}"));
        velocityBox.setValue(Integer.toString(config.velocity()));
        addRenderableWidget(velocityBox);

        analogButton = addRenderableWidget(Button.builder(analogLabel(), button -> {
            analog = !analog;
            button.setMessage(analogLabel());
        }).bounds(left + 112, top + 136, 196, 18).build());

        if (button) {
            colorButton = addRenderableWidget(Button.builder(colorLabel(), clicked -> {
                color = color.next();
                clicked.setMessage(colorLabel());
            }).bounds(left + 112, top + 160, 140, 18).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("midiinterface.gui.test"), button -> test())
                .bounds(left + 12, top + imageHeight - 30, 56, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("midiinterface.gui.save"), button -> save())
                .bounds(left + WIDTH - 152, top + imageHeight - 30, 64, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("midiinterface.gui.close"), button -> onClose())
                .bounds(left + WIDTH - 80, top + imageHeight - 30, 68, 20)
                .build());

        updateWidgets();
    }

    private int indexOfPort(String port) {
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).equalsIgnoreCase(port)) {
                return i;
            }
        }
        return -1;
    }

    private void cyclePort() {
        if (ports.isEmpty()) {
            ports = MidiService.outputPorts();
        }
        if (ports.isEmpty()) {
            return;
        }
        portIndex = (portIndex + 1) % ports.size();
        portBox.setValue(ports.get(portIndex));
    }

    private boolean knownPort() {
        String port = portBox.getValue().trim();
        for (String candidate : ports) {
            if (candidate.equalsIgnoreCase(port)) {
                return true;
            }
        }
        return false;
    }

    private void updateWidgets() {
        if (messageButton != null) {
            messageButton.setMessage(messageLabel());
        }
        if (analogButton != null) {
            analogButton.active = messageType == MidiMessageType.CONTROL_CHANGE;
        }
        if (velocityBox != null) {
            velocityBox.active = messageType != MidiMessageType.PROGRAM_CHANGE;
        }
        if (numberBox != null) {
            numberBox.active = true;
        }
        if (colorButton != null) {
            colorButton.setMessage(colorLabel());
        }
    }

    private Component channelLabel() {
        return Component.translatable("midiinterface.gui.channel", channel);
    }

    private Component messageLabel() {
        return Component.translatable(messageType.getTranslationKey());
    }

    private Component analogLabel() {
        return Component.translatable("midiinterface.gui.analog",
                Component.translatable(analog ? "midiinterface.gui.on" : "midiinterface.gui.off"));
    }

    private Component colorLabel() {
        return Component.translatable("midiinterface.gui.color",
                Component.translatable(color.getTranslationKey()));
    }

    private Component numberLabel() {
        return switch (messageType) {
            case NOTE -> Component.translatable("midiinterface.gui.note");
            case CONTROL_CHANGE -> Component.translatable("midiinterface.gui.controller");
            case PROGRAM_CHANGE -> Component.translatable("midiinterface.gui.program");
        };
    }

    private MidiConfig currentConfig() {
        MidiConfig previous = menu.getConfig();
        return new MidiConfig(
                portBox.getValue(),
                channel,
                messageType,
                parseInt(numberBox.getValue(), previous.number()),
                parseInt(velocityBox.getValue(), previous.velocity()),
                analog
        ).sanitized();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void test() {
        MidiConfig config = currentConfig();
        if (!knownPort()) {
            message(Component.translatable("midiinterface.gui.port_missing"));
            return;
        }
        MidiService.playTest(config);
        message(Component.translatable("midiinterface.gui.test_sent", config.port()));
    }

    private void save() {
        MidiConfig config = currentConfig();
        if (minecraft != null && minecraft.getConnection() != null) {
            ModNetwork.CHANNEL.sendToServer(new SetMidiConfigPacket(menu.getPos(), color, config));
        }
        message(Component.translatable("midiinterface.gui.saved"));
        onClose();
    }

    private void message(Component component) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(component, true);
        }
    }

    @Override
    public void onClose() {
        // AbstractContainerScreen#onClose calls closeContainer() and then super.onClose(),
        // which pops the GUI layer and calls setScreen(null) a second time. Under Sinytra
        // Connector that second call reaches Fabric's ScreenEvents with a null screen and
        // throws "Screen cannot be null". closeContainer() already closes the screen, so
        // skip the redundant pop.
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        } else if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        portBox.tick();
        numberBox.tick();
        velocityBox.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta == 0.0D) {
            return false;
        }

        int direction = delta > 0.0D ? 1 : -1;
        if (button && colorButton != null && isOverColor(mouseX, mouseY)) {
            color = direction > 0 ? color.next() : color.previous();
            colorButton.setMessage(colorLabel());
            return true;
        }
        if (isOverChannel(mouseX, mouseY)) {
            int step = hasShiftDown() ? 5 : 1;
            channel = Math.floorMod(channel - 1 + direction * step, 16) + 1;
            channelButton.setMessage(channelLabel());
            return true;
        }
        if (numberBox != null && numberBox.isMouseOver(mouseX, mouseY)) {
            adjustValue(numberBox, direction * (hasShiftDown() ? 10 : 1), 0, 127);
            return true;
        }
        if (velocityBox != null && velocityBox.isMouseOver(mouseX, mouseY)) {
            adjustValue(velocityBox, direction * (hasShiftDown() ? 10 : 1), 1, 127);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean isOverChannel(double mouseX, double mouseY) {
        int left = left();
        int top = top();
        return mouseX >= left + 14 && mouseX <= left + 192
                && mouseY >= top + 64 && mouseY <= top + 82;
    }

    private boolean isOverColor(double mouseX, double mouseY) {
        int left = left();
        int top = top();
        return mouseX >= left + 14 && mouseX <= left + 280
                && mouseY >= top + 160 && mouseY <= top + 178;
    }

    private void adjustValue(EditBox box, int step, int min, int max) {
        int value = parseInt(box.getValue(), min);
        box.setValue(Integer.toString(Math.max(min, Math.min(max, value + step))));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = left();
        int top = top();

        graphics.fill(left - 6, top - 6, left + WIDTH + 6, top + imageHeight + 6, 0xE615161B);
        int border = 0xFF3D3F49;
        graphics.fill(left - 6, top - 6, left + WIDTH + 6, top - 5, border);
        graphics.fill(left - 6, top + imageHeight + 5, left + WIDTH + 6, top + imageHeight + 6, border);
        graphics.fill(left - 6, top - 6, left - 5, top + imageHeight + 6, border);
        graphics.fill(left + WIDTH + 5, top - 6, left + WIDTH + 6, top + imageHeight + 6, border);

        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("midiinterface.gui.hint"), left + 14, top + 24, 0x8E909C, false);

        int labelColor = 0xB8BAC4;
        graphics.drawString(font, Component.translatable("midiinterface.gui.port"), left + 14, top + 45, labelColor, false);
        graphics.drawString(font, Component.translatable("midiinterface.gui.channel_label"), left + 14, top + 69, labelColor, false);
        graphics.drawString(font, Component.translatable("midiinterface.gui.message"), left + 14, top + 93, labelColor, false);
        graphics.drawString(font, numberLabel(), left + 14, top + 117, labelColor, false);
        graphics.drawString(font, velocityLabel(), left + 174, top + 117, labelColor, false);
        graphics.drawString(font, Component.translatable("midiinterface.gui.analog_label"), left + 14, top + 141, labelColor, false);

        if (button) {
            graphics.drawString(font, Component.translatable("midiinterface.gui.color_label"),
                    left + 14, top + 165, labelColor, false);
            int swatchX = left + 258;
            int swatchY = top + 159;
            graphics.fill(swatchX - 1, swatchY - 1, swatchX + 19, swatchY + 19, 0xFF111111);
            graphics.fill(swatchX, swatchY, swatchX + 18, swatchY + 18, 0xFF000000 | color.getRgb());
        }

        int previewY = top + (button ? 190 : 166);
        int statusY = top + (button ? 204 : 180);
        graphics.drawString(font, previewText(), left + 14, previewY, 0xE8C07A, false);
        String status = statusText().getString();
        graphics.drawString(font, font.plainSubstrByWidth(status, WIDTH - 28), left + 14, statusY,
                knownPort() ? 0x8FD18F : 0xF08A7A, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The whole layout is custom-drawn in renderBg.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (findButton.isHoveredOrFocused()) {
            Component tooltip = ports.isEmpty()
                    ? Component.translatable("midiinterface.gui.no_ports")
                    : Component.translatable("midiinterface.gui.available_ports", String.join(", ", ports));
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else if (channelButton.isHoveredOrFocused()) {
            graphics.renderTooltip(font, Component.translatable("midiinterface.gui.channel_tooltip"), mouseX, mouseY);
        } else if (button && colorButton != null && colorButton.isHoveredOrFocused()) {
            graphics.renderTooltip(font, Component.translatable("midiinterface.gui.color_tooltip"), mouseX, mouseY);
        }
    }

    private Component previewText() {
        return switch (messageType) {
            case NOTE -> Component.translatable("midiinterface.gui.preview_note",
                    channel, numberBox.getValue(), velocityBox.getValue());
            case CONTROL_CHANGE -> Component.translatable("midiinterface.gui.preview_cc",
                    channel, numberBox.getValue(), velocityBox.getValue(),
                    Component.translatable(analog
                            ? "midiinterface.gui.preview_cc_analog"
                            : "midiinterface.gui.preview_cc_switch"));
            case PROGRAM_CHANGE -> Component.translatable("midiinterface.gui.preview_program",
                    channel, numberBox.getValue());
        };
    }

    private Component velocityLabel() {
        return Component.translatable(messageType == MidiMessageType.CONTROL_CHANGE
                ? "midiinterface.gui.value"
                : "midiinterface.gui.velocity");
    }

    private Component statusText() {
        if (ports.isEmpty()) {
            return Component.translatable("midiinterface.gui.no_outputs");
        }
        if (knownPort()) {
            return Component.translatable("midiinterface.gui.ready", ports.size());
        }
        return Component.translatable("midiinterface.gui.port_missing_help", ports.size());
    }
}
