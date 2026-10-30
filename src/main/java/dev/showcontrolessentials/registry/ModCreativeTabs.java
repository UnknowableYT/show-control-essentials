package dev.showcontrolessentials.registry;

import dev.showcontrolessentials.MidiInterfaceMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Method;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MidiInterfaceMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ESSENTIALS_TAB = TABS.register("essentials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.midiinterface"))
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
        bus.addListener(ModCreativeTabs::addToTheatricalTab);
        bus.addListener(ModCreativeTabs::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModCreativeTabs::appendToTheatricalTab);
    }

    private static void addToTheatricalTab(BuildCreativeModeTabContentsEvent event) {
        if (!isTheatricalTab(event)) {
            return;
        }

        // Keep a copy at the front of Theatrical's tab alongside the dedicated tab.
        event.getEntries().putFirst(new ItemStack(ModBlocks.MIDI_BUTTON_ITEM.get()),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        event.getEntries().putFirst(new ItemStack(ModBlocks.MIDI_INTERFACE_ITEM.get()),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        MidiInterfaceMod.LOGGER.info("[Essentials] Added the MIDI Interface and MIDI Button to creative tab {}",
                event.getTabKey().location());
    }

    private static boolean isTheatricalTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().getNamespace().equals("theatrical")) {
            return true;
        }

        // Fallback for loaders that rename the tab key but keep the display name.
        return event.getTab().getDisplayName().getString().equalsIgnoreCase("Theatrical");
    }

    /**
     * Theatrical builds its tab through Architectury, and under Sinytra Connector the Forge
     * event is not always delivered for it. Ask Architectury to append the blocks directly;
     * this is the same path Theatrical itself uses for its own items.
     */
    private static void appendToTheatricalTab() {
        if (!ModList.get().isLoaded("theatrical") || !ModList.get().isLoaded("architectury")) {
            return;
        }

        try {
            Class<?> theatrical = Class.forName("dev.imabad.theatrical.Theatrical");
            Object tabSupplier = theatrical.getField("TAB").get(null);
            Class<?> creativeTabRegistry = Class.forName("dev.architectury.registry.CreativeTabRegistry");
            Class<?> deferredSupplier = Class.forName("dev.architectury.registry.registries.DeferredSupplier");
            Method append = creativeTabRegistry.getMethod("append", deferredSupplier, ItemLike[].class);
            append.invoke(null, tabSupplier, (Object) new ItemLike[]{
                    ModBlocks.MIDI_INTERFACE_ITEM.get(),
                    ModBlocks.MIDI_BUTTON_ITEM.get()
            });
            MidiInterfaceMod.LOGGER.info("[Essentials] Added the MIDI Interface and MIDI Button to Theatrical's tab via Architectury");
        } catch (Throwable throwable) {
            MidiInterfaceMod.LOGGER.warn("[Essentials] Could not append to Theatrical's creative tab: {}", throwable.toString());
        }
    }
}
