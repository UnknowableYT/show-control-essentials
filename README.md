# Show Control Essentials

**Show Control Essentials** is a small Forge 1.20.1 show-control addon for Minecraft. It adds
a **MIDI Interface** block and a pressable **MIDI Button**, both of which send configurable
MIDI messages from your PC so you can fire cues, sequences, macros and executors in a
MIDI-capable lighting console or show-control application.

The mod id is now `showcontrolessentials`. Worlds created with the previous `midiinterface`
id will lose the old blocks/items on first load, so back up before upgrading.

It is designed to sit next to **Theatrical** and **Theatrical Extra Lights** (no hard
dependency, no mixins, no changes to their blocks) and follows the same "Redstone Interface"
idea: put it on a redstone line, configure it, done.

---

## What it does

- Reads redstone strength 0-15 from any side (like a redstone lamp input).
- Sends one of these on the rising/falling edge:
  - **Note On / Note Off** (best for console buttons and executors)
  - **Control Change** (fixed on/off value, or value follows redstone strength)
  - **Program Change** (for other gear)
- Per-block configuration: MIDI port, channel 1-16, note/CC/program number, velocity/value,
  and analog CC mode.
- MIDI is sent from the **client PC** that is near the block, because that is the machine
  running loopMIDI and your console or show-control software.
- The block lights up while it is receiving redstone.
- The block texture follows Theatrical's **Redstone Interface** design, with a clean white inlay.
- Works in singleplayer and on dedicated servers (players near the block send the MIDI).
- **MIDI Button** - a vanilla-style stone button (place it on a floor, wall or ceiling) that
  sends a momentary Note On/Off (or CC pulse) when right-clicked. It comes in 16 stage
  colours; the colour row in the GUI can be clicked or scrolled.
- Both blocks appear in the new **Show Control Essentials** creative tab and in
  **Theatrical's** tab.
- All block and item textures are 16x16.

## Install

1. Close Minecraft.
2. Put `showcontrolessentials-forge-1.20.1-0.1.0.jar` in your Theatrical profile's `mods` folder.
3. Start the Theatrical profile with Forge 1.20.1.

No dependencies other than Forge. Theatrical / Extra Lights are optional.

## Crafting

**MIDI Interface**

```
Iron Ingot   Redstone   Iron Ingot
Redstone     Note Block Redstone
Iron Ingot   Redstone   Iron Ingot
```

**MIDI Button**

```
Iron Ingot   Redstone     Iron Ingot
Redstone     Stone Button Redstone
Iron Ingot   Redstone     Iron Ingot
```

Both blocks are in the new **Show Control Essentials** creative tab and at the front of
**Theatrical's** creative tab. They are not added to the vanilla redstone tab.

## Using the block

1. Place the **MIDI Interface** next to your redstone line.
2. Right-click it to open the configuration screen.
3. Set:
   - **MIDI Port** - the exact Windows MIDI output name (for example `MA2 LOOPMIDI`).
     Click **Find** to cycle through detected outputs.
   - **Channel** - 1-16. Use the same channel in your console. You can **scroll** over the
     channel control to change it (hold Shift to jump 5 channels), or click it to cycle.
   - **Message** - Note, Control Change, or Program Change.
   - **Note / Controller / Program** - 0-127. The mouse wheel adjusts this field while hovered.
   - **Velocity / On value** - 1-127. The mouse wheel adjusts this field while hovered.
   - **CC mode** (Control Change only):
     - `Follow redstone: OFF` - sends the on-value while powered, 0 when unpowered.
     - `Follow redstone: ON` - maps redstone 0-15 to MIDI 0-127 (good for faders).
4. Press **Test** to send the message immediately.
5. Press **Save**.

Rising edge sends the "on" message; falling edge sends Note Off (or CC 0). A redstone
signal of 1-15 is enough; the block does not need a full 15.

### MIDI Button

- Place it like a vanilla stone button: on the floor, a wall, or the ceiling.
- **Right-click** the button to press it. It sends a momentary pulse (Note On, then Note Off
  about 10 ticks later) and plays a click sound.
- **Sneak + right-click** to open the configuration screen.
- The **Button color** row offers 16 stage colours (red, orange, amber, yellow, lime, green,
  mint, cyan, azure, blue, violet, purple, magenta, pink, warm white, cool white). Scroll over
  the row or click the button to change it; the block updates immediately after saving.
- The colour is stored on the block state, so it is per-button and survives restarts.

## Bugs and suggestions

Use the **Issues** tab on GitHub:

- [Report a bug](https://github.com/UnknowableYT/show-control-essentials/issues/new?template=bug_report.yml)
- [Suggest a feature or item](https://github.com/UnknowableYT/show-control-essentials/issues/new?template=suggestion.yml)
- [Browse all issues](https://github.com/UnknowableYT/show-control-essentials/issues)

The forms ask for the mod version, Minecraft/Forge version, and logs so problems can be
reproduced quickly.

## Console setup

All supported consoles can use the same loopMIDI port. Create the port and start loopMIDI
**before** Minecraft and the console, then set the block's MIDI Port to that exact name.

1. In Minecraft, set the block's **MIDI Port**, **Channel**, **Message**, and note/CC number.
2. Press **Test** and leave the block screen open while you map it on the console.
3. Select the same MIDI port as the console's MIDI input and use the same channel.

| Console | MIDI input | Best message for this mod | Notes |
| --- | --- | --- | --- |
| dot2 onPC | Notes | **Note** | Remote Inputs only use notes |
| grandMA2 onPC | Notes | **Note** | MIDI Remotes only use notes |
| grandMA3 onPC | Notes + CC | **Note** for buttons, **CC** for faders | MIDI Remotes |
| MagicQ | Notes + CC | **Note** / **CC** | MIDI Learn + Autom |
| QLC+ | Notes, CC, PC | **Note** / **CC** | Virtual Console input |

> MA2 and dot2 only analyse MIDI notes in their remote tables; use Note mode there. Note
> velocity is a fixed value in this mod. Use analog **Control Change** for faders on MA3,
> MagicQ, or QLC+.

### dot2 onPC

1. Create loopMIDI and start dot2 onPC.
2. **Tools -> MIDI Configuration** -> set **MIDI In** to the loopMIDI port; wait for the green tick.
3. **Setup -> Remote Inputs Configuration** -> add a row; select an executor or command.
4. Enter the block's **MIDI Note** and **MIDI Channel**, or use the MIDI monitor/learn to capture.
5. Press **Test**; the assigned executor/command should react.

On a hardware dot2, the MIDI Configuration window is not needed.

### grandMA2 onPC

1. Create loopMIDI and start grandMA2 onPC.
2. Click the **yellow ball** (top-left) -> **Options -> MIDI** -> set **MIDI In Device** to the
   loopMIDI port. Disable command-wing MIDI if you use USB/virtual MIDI.
3. **Setup -> Remote Input Setup -> MIDI Remotes** -> add a remote.
4. Set **Type** (`Exec`), **Button** (executor button or `Fader`), **Note**, and **Channel**.
5. Press **Test**. If the port is missing, restart onPC after starting loopMIDI.

### grandMA3 onPC

1. Create loopMIDI and start grandMA3 onPC.
2. **Menu -> Settings -> onPC Settings -> MIDI In Device** -> select the loopMIDI port.
3. **Menu -> In & Out** -> enable MIDI input; set the MIDI data mode to **In**.
4. **MIDI Remotes** -> insert a remote; set **MIDI Type** (`Note`, `NoteAttack`, or `Control`),
   **Channel**, **MIDI Index** (note/CC number), and **Target**.
5. Press **Test**. Use **Note** for buttons; use **Control** + the mod's analog CC mode for faders.

> grandMA3 onPC accepts one MIDI input device. If you also use an APC mini or another
> controller, merge it into the loopMIDI port with Bome MIDI Translator, or use a second
> port and switch devices.

### ChamSys MagicQ

1. Create loopMIDI and start MagicQ.
2. On MagicQ PC: **Tools -> Virtual (USB) MIDI Setup** -> select the loopMIDI input. On a
   console, use the connected MIDI interface.
3. **Setup -> View Settings -> MIDI / Timecode** -> **MIDI In Type** = `Any chan`, or
   `Requested Chan` with **MIDI In Chan** matching the block. Disable timecode if you only
   need note/CC triggers.
4. Assign the trigger:
   - **MIDI Learn:** open the **Execute** window, select the execute box, press **encoder D**,
     then press **Test** in the block screen.
   - Or **Macro -> View Autom** -> add a MIDI event and set its action to a playback/cue.
5. Use **Note** for buttons and **CC** (analog) for faders.

MagicQ PC may require unlocked mode or the MagicQ USB MIDI/Timecode box for third-party or
virtual MIDI. Check the licensing/status display if MIDI Learn sees nothing.

### QLC+

1. Create loopMIDI and start QLC+.
2. Open **Inputs/Outputs** -> select a universe -> tick the MIDI **input** line for the
   loopMIDI port. Use the configure button if you need to set the MIDI channel/mode.
3. Open the **Virtual Console**; add a **Button** (notes) or **Slider** (CC/faders).
4. Widget properties -> **Input** -> enable **auto-detect**, press **Test** in the block screen,
   and accept the detected channel; or map the universe/channel manually.
5. Switch QLC+ to **Operate** mode and press **Test**; the widget should react.

QLC+ supports Note, CC, and Program Change. Use **Note** for triggers and analog **CC** for a
slider that follows redstone strength.

## Configuration file

`config/showcontrolessentials-common.toml`

- `defaultPort` - default port for newly placed blocks (default `MA2 LOOPMIDI`).
- `defaultChannel` - 1-16.
- `defaultMessage` - `NOTE`, `CONTROL_CHANGE`, or `PROGRAM_CHANGE`.
- `defaultNumber` - 0-127.
- `defaultVelocity` - 1-127.
- `triggerRadius` - how close a player must be to receive the trigger (default 64 blocks).
- `logMidiMessages` - log every sent message (useful when checking the port name).

## Notes / limitations

- MIDI is a **client-side** output. On a dedicated server, each nearby player with the mod
  sends the message from their own PC. Use one player (or a small trigger radius) if you do
  not want duplicates.
- If you rename or delete the loopMIDI port while the game is running, reopen the block
  screen (or restart the game) after fixing it. Messages are logged with the missing port
  name.
- Note On without a matching Note Off can leave a button latched in some software. The mod
  sends Note Off when the redstone drops, when the block is broken, and when you leave the
  world or close the game.
- No MIDI is sent while chunks are loading; the block only reacts to actual signal changes.

## Verified

- Built for Forge 1.20.1 (47.x) and loaded in a Forge 1.20.1 dev client/server.
- MIDI message logic covered by 37 checks (Note On/Off, fixed and analog CC, Program Change,
  channel clamping).
- End-to-end loopback test on the installed `MA2 LOOPMIDI` port received the exact
  Note On payload (`0x90`, channel 5, note 60, velocity 100).

## Building from source

```powershell
# Java 17 is required
cd path\to\MinecraftMIDI
.\gradlew.bat build
```

The jar is written to `build/libs/`. The block textures and logo can be regenerated with:

```powershell
java tools\GenerateAssets.java
```

The MIDI logic can be tested without Minecraft:

```powershell
javac -d out src\main\java\dev\showcontrolessentials\MidiMessageType.java src\main\java\dev\showcontrolessentials\client\MidiMessages.java tools\MidiSelfTest.java
java -cp out MidiSelfTest "MA2 LOOPMIDI"
```
