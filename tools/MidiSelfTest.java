import dev.showcontrolessentials.MidiMessageType;
import dev.showcontrolessentials.client.MidiMessages;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless checks for the MIDI message logic. Run with:
 *   javac -d <out> src/main/java/dev/showcontrolessentials/MidiMessageType.java \
 *                  src/main/java/dev/showcontrolessentials/client/MidiMessages.java tools/MidiSelfTest.java
 *   java -cp <out> MidiSelfTest "MA2 LOOPMIDI"
 */
public final class MidiSelfTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        ShortMessage noteOn = MidiMessages.build(MidiMessageType.NOTE, 5, 60, 100, false, 15, false);
        expect(noteOn, ShortMessage.NOTE_ON, 4, 60, 100, "note on at rising edge");

        ShortMessage noteOff = MidiMessages.build(MidiMessageType.NOTE, 5, 60, 100, false, 0, true);
        expect(noteOff, ShortMessage.NOTE_OFF, 4, 60, 0, "note off at falling edge");

        check(MidiMessages.build(MidiMessageType.NOTE, 5, 60, 100, false, 7, true) == null,
                "note does not retrigger on analog change");

        ShortMessage ccOn = MidiMessages.build(MidiMessageType.CONTROL_CHANGE, 1, 21, 100, false, 15, false);
        expect(ccOn, ShortMessage.CONTROL_CHANGE, 0, 21, 100, "fixed CC on value");

        ShortMessage ccOff = MidiMessages.build(MidiMessageType.CONTROL_CHANGE, 1, 21, 100, false, 0, true);
        expect(ccOff, ShortMessage.CONTROL_CHANGE, 0, 21, 0, "fixed CC off value");

        ShortMessage ccAnalog = MidiMessages.build(MidiMessageType.CONTROL_CHANGE, 1, 21, 100, true, 7, false);
        expect(ccAnalog, ShortMessage.CONTROL_CHANGE, 0, 21, 59, "analog CC value 7/15");

        ShortMessage ccFull = MidiMessages.build(MidiMessageType.CONTROL_CHANGE, 1, 21, 100, true, 15, false);
        expect(ccFull, ShortMessage.CONTROL_CHANGE, 0, 21, 127, "analog CC full value");

        ShortMessage pc = MidiMessages.build(MidiMessageType.PROGRAM_CHANGE, 16, 12, 100, false, 15, false);
        expect(pc, ShortMessage.PROGRAM_CHANGE, 15, 12, 0, "program change on rising edge");

        check(MidiMessages.build(MidiMessageType.PROGRAM_CHANGE, 16, 12, 100, false, 0, true) == null,
                "program change does not send on falling edge");

        System.out.println("All " + checks + " message checks passed.");

        String port = args.length > 0 ? args[0] : null;
        if (port != null) {
            loopback(port);
        }
    }

    private static void expect(ShortMessage message, int command, int channel, int data1, int data2, String label) {
        check(message != null, label + " exists");
        if (message == null) {
            return;
        }
        check(message.getCommand() == command, label + " command (was " + message.getCommand() + ")");
        check(message.getChannel() == channel, label + " channel (was " + (message.getChannel() + 1) + ")");
        check(message.getData1() == data1, label + " data1 (was " + message.getData1() + ")");
        check(message.getData2() == data2, label + " data2 (was " + message.getData2() + ")");
    }

    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) {
            throw new AssertionError("FAILED: " + label);
        }
        System.out.println("ok - " + label);
    }

    private static void loopback(String portName) throws Exception {
        MidiDevice output = null;
        MidiDevice input = null;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (!info.getName().equalsIgnoreCase(portName)) {
                continue;
            }
            MidiDevice device = MidiSystem.getMidiDevice(info);
            if (device.getMaxReceivers() != 0) {
                output = device;
            }
            if (device.getMaxTransmitters() != 0) {
                input = device;
            }
        }

        if (output == null || input == null) {
            throw new AssertionError("Port '" + portName + "' does not have both an output and an input side.");
        }

        List<String> received = new ArrayList<>();
        input.open();
        output.open();
        Transmitter transmitter = input.getTransmitter();
        transmitter.setReceiver(new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                if (message instanceof ShortMessage shortMessage) {
                    received.add(shortMessage.getCommand() + "/" + (shortMessage.getChannel() + 1)
                            + "/" + shortMessage.getData1() + "/" + shortMessage.getData2());
                }
            }

            @Override
            public void close() {
            }
        });

        Thread.sleep(150);
        output.getReceiver().send(MidiMessages.build(MidiMessageType.NOTE, 5, 60, 100, false, 15, false), -1);
        Thread.sleep(400);

        transmitter.close();
        input.close();
        output.close();

        System.out.println("Loopback received: " + received);
        check(!received.isEmpty(), "MIDI received on '" + portName + "'");
        check(received.get(0).startsWith(ShortMessage.NOTE_ON + "/5/60/100"),
                "loopback note on payload (was " + received + ")");
    }
}
