package dev.showcontrolessentials.client;

import dev.showcontrolessentials.Config;
import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.MidiInterfaceMod;
import dev.showcontrolessentials.MidiMessageType;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All MIDI work happens on the client and on a dedicated daemon thread, so a slow or
 * disconnected MIDI device can never stall the game thread.
 */
public final class MidiService {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Redstone MIDI Interface");
        thread.setDaemon(true);
        return thread;
    });

    private static final Map<String, OpenPort> OPEN_PORTS = new HashMap<>();
    private static final Set<String> WARNED = new HashSet<>();
    private static final Set<ActiveNote> ACTIVE_NOTES = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(MidiService::shutdown, "Redstone MIDI Interface shutdown"));
    }

    private MidiService() {
    }

    private record OpenPort(MidiDevice device, Receiver receiver) {
    }

    private record ActiveNote(String port, int channel, int note) {
    }

    /**
     * @return the names of all MIDI devices that can receive messages (loopMIDI ports included).
     */
    public static List<String> outputPorts() {
        List<String> ports = new ArrayList<>();
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                String description = safe(info.getDescription()).toLowerCase(Locale.ROOT);
                if (description.contains("sequencer")
                        || description.contains("synthesizer")
                        || description.contains("midi_mapper")) {
                    continue;
                }
                try {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    if (device.getMaxReceivers() == 0) {
                        continue;
                    }
                } catch (MidiUnavailableException ignored) {
                    continue;
                }
                String name = safe(info.getName());
                if (!name.isEmpty() && !ports.contains(name)) {
                    ports.add(name);
                }
            }
        } catch (Throwable throwable) {
            warnOnce("enumerate", "Could not enumerate MIDI devices: " + throwable.getMessage());
        }

        Collections.sort(ports, String.CASE_INSENSITIVE_ORDER);
        return ports;
    }

    public static void handle(MidiConfig config, int power, boolean wasPowered) {
        ShortMessage message;
        try {
            message = MidiMessages.build(config.type(), config.channel(), config.number(), config.velocity(),
                    config.analog(), power, wasPowered);
        } catch (InvalidMidiDataException exception) {
            warnOnce("invalid:" + config, "Invalid MIDI message for " + config + ": " + exception.getMessage());
            return;
        }
        if (message == null) {
            return;
        }
        EXECUTOR.execute(() -> send(config, message));
    }

    /**
     * Sends the configured message once (and a note-off after a short delay for notes).
     */
    public static void playTest(MidiConfig config) {
        ShortMessage on;
        try {
            on = MidiMessages.build(config.type(), config.channel(), config.number(), config.velocity(),
                    config.analog(), 15, false);
        } catch (InvalidMidiDataException exception) {
            return;
        }
        if (on == null) {
            return;
        }

        EXECUTOR.execute(() -> {
            send(config, on);
            if (config.type() == MidiMessageType.NOTE) {
                try {
                    Thread.sleep(275L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    ShortMessage off = MidiMessages.build(config.type(), config.channel(), config.number(),
                            config.velocity(), config.analog(), 0, true);
                    if (off != null) {
                        send(config, off);
                    }
                } catch (InvalidMidiDataException ignored) {
                }
            }
        });
    }

    private static void send(MidiConfig config, ShortMessage message) {
        Receiver receiver = receiverFor(config.port());
        if (receiver == null) {
            return;
        }

        try {
            receiver.send(message, -1L);
            trackNote(config.port(), message);
            if (Config.debug()) {
                MidiInterfaceMod.LOGGER.info("[MIDI] port='{}' channel={} command=0x{} data1={} data2={}",
                        describePort(config.port()), config.channel(),
                        Integer.toHexString(message.getCommand()), message.getData1(), message.getData2());
            }
        } catch (IllegalStateException exception) {
            closePort(config.port());
            warnOnce("send:" + config.port(), "MIDI send failed on '" + config.port() + "': " + exception.getMessage());
        }
    }

    private static void trackNote(String port, ShortMessage message) {
        String key = port == null ? "" : port;
        if (message.getCommand() == ShortMessage.NOTE_ON && message.getData2() > 0) {
            ACTIVE_NOTES.add(new ActiveNote(key, message.getChannel(), message.getData1()));
        } else if (message.getCommand() == ShortMessage.NOTE_OFF
                || (message.getCommand() == ShortMessage.NOTE_ON && message.getData2() == 0)) {
            ACTIVE_NOTES.remove(new ActiveNote(key, message.getChannel(), message.getData1()));
        }
    }

    /**
     * Releases any notes that are still held and closes the ports (used on world logout and
     * on game shutdown). Prevents a button staying latched on the console.
     */
    public static void disconnect() {
        EXECUTOR.execute(MidiService::disconnectNow);
    }

    private static void disconnectNow() {
        releaseActiveNotes();
        closeAll();
    }

    private static void shutdown() {
        releaseActiveNotes();
        closeAll();
    }

    private static void releaseActiveNotes() {
        for (ActiveNote active : new ArrayList<>(ACTIVE_NOTES)) {
            Receiver receiver = receiverFor(active.port());
            if (receiver == null) {
                continue;
            }
            try {
                receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, active.channel(), active.note(), 0), -1L);
            } catch (Exception ignored) {
            }
        }
        ACTIVE_NOTES.clear();
    }

    private static Receiver receiverFor(String requestedPort) {
        String port = requestedPort == null ? "" : requestedPort.trim();

        if (port.isEmpty()) {
            List<String> available = outputPorts();
            if (available.isEmpty()) {
                warnOnce("no-ports", "No MIDI output devices found. Install loopMIDI or connect a MIDI device.");
                return null;
            }
            port = available.get(0);
        }

        OpenPort openPort = OPEN_PORTS.get(port);
        if (openPort != null && openPort.device().isOpen()) {
            return openPort.receiver();
        }
        closePort(port);

        try {
            MidiDevice.Info match = null;
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                if (info.getName().equalsIgnoreCase(port)) {
                    match = info;
                    break;
                }
            }

            if (match == null) {
                warnOnce("missing:" + port, "MIDI output port '" + port + "' was not found. Available ports: " + outputPorts());
                return null;
            }

            MidiDevice device = MidiSystem.getMidiDevice(match);
            if (device.getMaxReceivers() == 0) {
                warnOnce("no-receiver:" + port, "MIDI device '" + port + "' cannot receive MIDI messages.");
                return null;
            }

            device.open();
            Receiver receiver = device.getReceiver();
            OPEN_PORTS.put(port, new OpenPort(device, receiver));
            MidiInterfaceMod.LOGGER.info("[MIDI] Opened output port '{}'", port);
            return receiver;
        } catch (MidiUnavailableException exception) {
            warnOnce("open:" + port, "Could not open MIDI port '" + port + "': " + exception.getMessage());
            return null;
        }
    }

    private static String describePort(String port) {
        return port == null || port.isBlank() ? "(first available)" : port;
    }

    private static void closePort(String port) {
        OpenPort openPort = OPEN_PORTS.remove(port);
        if (openPort == null) {
            return;
        }
        try {
            openPort.receiver().close();
        } catch (Throwable ignored) {
        }
        try {
            openPort.device().close();
        } catch (Throwable ignored) {
        }
    }

    private static void closeAll() {
        for (String port : new ArrayList<>(OPEN_PORTS.keySet())) {
            closePort(port);
        }
    }

    private static void warnOnce(String key, String message) {
        if (WARNED.add(key)) {
            MidiInterfaceMod.LOGGER.warn("[MIDI] {}", message);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
