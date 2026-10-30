package dev.showcontrolessentials.registry;

import dev.showcontrolessentials.MidiInterfaceMod;
import dev.showcontrolessentials.block.MidiInterfaceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MidiInterfaceMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MidiInterfaceMod.MOD_ID);

    public static final RegistryObject<Block> MIDI_INTERFACE =
            BLOCKS.register("midi_interface", MidiInterfaceBlock::new);
    public static final RegistryObject<Block> MIDI_BUTTON =
            BLOCKS.register("midi_button", dev.showcontrolessentials.block.MidiButtonBlock::new);

    public static final RegistryObject<Item> MIDI_INTERFACE_ITEM =
            ITEMS.register("midi_interface", () -> new BlockItem(MIDI_INTERFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MIDI_BUTTON_ITEM =
            ITEMS.register("midi_button", () -> new BlockItem(MIDI_BUTTON.get(), new Item.Properties()));

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
