package dev.showcontrolessentials.registry;

import dev.showcontrolessentials.MidiInterfaceMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MidiInterfaceMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ESSENTIALS_TAB = TABS.register("essentials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.showcontrolessentials"))
                    .icon(() -> new ItemStack(ModBlocks.MIDI_INTERFACE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.MIDI_INTERFACE_ITEM.get());
                        output.accept(ModBlocks.MIDI_BUTTON_ITEM.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
