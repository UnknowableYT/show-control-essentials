package dev.showcontrolessentials;

import com.mojang.logging.LogUtils;
import dev.showcontrolessentials.registry.ModBlockEntities;
import dev.showcontrolessentials.registry.ModBlocks;
import dev.showcontrolessentials.registry.ModCreativeTabs;
import dev.showcontrolessentials.registry.ModMenus;
import dev.showcontrolessentials.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MidiInterfaceMod.MOD_ID)
public final class MidiInterfaceMod {
    public static final String MOD_ID = "showcontrolessentials";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MidiInterfaceMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(bus);
        ModBlockEntities.register(bus);
        ModMenus.register(bus);
        ModCreativeTabs.register(bus);

        ModNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
