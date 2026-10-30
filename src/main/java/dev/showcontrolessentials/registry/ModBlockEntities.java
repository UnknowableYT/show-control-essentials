package dev.showcontrolessentials.registry;

import dev.showcontrolessentials.MidiInterfaceMod;
import dev.showcontrolessentials.block.MidiInterfaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MidiInterfaceMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<MidiInterfaceBlockEntity>> MIDI_INTERFACE =
            BLOCK_ENTITIES.register("midi_interface", () -> BlockEntityType.Builder
                    .of(MidiInterfaceBlockEntity::new, ModBlocks.MIDI_INTERFACE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<dev.showcontrolessentials.block.MidiButtonBlockEntity>> MIDI_BUTTON =
            BLOCK_ENTITIES.register("midi_button", () -> BlockEntityType.Builder
                    .of(dev.showcontrolessentials.block.MidiButtonBlockEntity::new, ModBlocks.MIDI_BUTTON.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
