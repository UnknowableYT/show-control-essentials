package dev.showcontrolessentials.menu;

import dev.showcontrolessentials.ButtonColor;
import dev.showcontrolessentials.MidiConfig;
import dev.showcontrolessentials.block.MidiButtonBlockEntity;
import dev.showcontrolessentials.block.MidiInterfaceBlockEntity;
import dev.showcontrolessentials.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class MidiInterfaceMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final MidiConfig config;
    private final boolean button;
    private final ButtonColor color;
    private final ContainerLevelAccess access;
    private final Block block;

    /**
     * Client-side constructor, invoked from the network buffer opened by NetworkHooks.openScreen.
     */
    public MidiInterfaceMenu(int windowId, Inventory inventory, FriendlyByteBuf buffer) {
        this(windowId, inventory, buffer.readBlockPos(), buffer.readBoolean(),
                buffer.readEnum(ButtonColor.class), MidiConfig.read(buffer), ContainerLevelAccess.NULL);
    }

    /**
     * Server-side constructor for the redstone MIDI Interface.
     */
    public MidiInterfaceMenu(int windowId, Inventory inventory, MidiInterfaceBlockEntity blockEntity) {
        this(windowId, inventory, blockEntity.getBlockPos(), false, ButtonColor.ORANGE,
                blockEntity.getConfig(), ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    /**
     * Server-side constructor for the MIDI Button.
     */
    public MidiInterfaceMenu(int windowId, Inventory inventory, MidiButtonBlockEntity blockEntity, ButtonColor color) {
        this(windowId, inventory, blockEntity.getBlockPos(), true, color, blockEntity.getConfig(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MidiInterfaceMenu(int windowId, Inventory inventory, BlockPos pos, boolean button,
                              ButtonColor color, MidiConfig config, ContainerLevelAccess access) {
        super(dev.showcontrolessentials.registry.ModMenus.MIDI_INTERFACE.get(), windowId);
        this.pos = pos;
        this.button = button;
        this.color = color == null ? ButtonColor.ORANGE : color;
        this.config = config;
        this.access = access;
        this.block = button ? ModBlocks.MIDI_BUTTON.get() : ModBlocks.MIDI_INTERFACE.get();
    }

    public BlockPos getPos() {
        return pos;
    }

    public MidiConfig getConfig() {
        return config;
    }

    public boolean isButton() {
        return button;
    }

    public ButtonColor getColor() {
        return color;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, block);
    }
}
