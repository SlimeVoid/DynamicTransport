package net.slimevoid.dynamictransport.core.lib;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.network.PacketMarkerGUIHandler;
import net.slimevoid.dynamictransport.network.packet.PacketMarkerData;
import net.slimevoid.dynamictransport.network.packet.executors.ElevatorCallExecutor;
import net.slimevoid.dynamictransport.network.packet.executors.MarkerConfigurationExecutor;
import net.slimevoid.library.network.PacketIds;
import net.slimevoid.library.network.handlers.PacketPipeline;
import net.slimevoid.library.util.helpers.PacketHelper;

public class PacketLib {

    public static PacketPipeline handler = new PacketPipeline();

    @SideOnly(Side.CLIENT)
    public static void registerClientPacketHandlers() {
    }

    public static void registerPacketHandlers() {
        PacketMarkerGUIHandler guiHandler = new PacketMarkerGUIHandler();
        guiHandler.registerServerExecutor(CommandLib.CALL_ELEVATOR,
                new ElevatorCallExecutor());
        guiHandler.registerServerExecutor(CommandLib.UPDATE_MARKER,
                                new MarkerConfigurationExecutor());

        handler.registerPacketHandler(PacketIds.GUI,
                                      guiHandler);
    }

    public static void sendFloorSelection(String floorNumber, String floorName, BlockPos pos) {
        // create packet
        PacketMarkerData packet = new PacketMarkerData(GuiLib.GUIID_FloorSelection, Integer.valueOf(floorNumber), floorName, pos.getX(), pos.getY(), pos.getZ(), 0);
        PacketHelper.sendToServer(packet);
    }

    public static void sendMarkerConfiguration(int floorY, String floorName, BlockPos pos) {
        PacketMarkerData packet = new PacketMarkerData(GuiLib.GUIID_FLOOR_MARKER, floorY, floorName, pos.getX(), pos.getY(), pos.getZ(), 1);
        PacketHelper.sendToServer(packet);
    }
}
