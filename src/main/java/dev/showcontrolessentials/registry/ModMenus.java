package dev.showcontrolessentials.registry;

import dev.showcontrolessentials.MidiInterfaceMod;
import dev.showcontrolessentials.menu.MidiInterfaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MidiInterfaceMod.MOD_ID);

    public static final RegistryObject<MenuType<MidiInterfaceMenu>> MIDI_INTERFACE =
            MENUS.register("midi_interface", () -> IForgeMenuType.create(MidiInterfaceMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
