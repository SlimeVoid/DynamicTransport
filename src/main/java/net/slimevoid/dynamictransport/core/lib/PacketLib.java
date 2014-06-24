package net.slimevoid.dynamictransport.core.lib;

import net.slimevoid.dynamictransport.network.PacketMarkerGUIHandler;
import net.slimevoid.dynamictransport.network.packet.PacketMarkerData;
import net.slimevoid.dynamictransport.network.packet.executors.ElevatorCallExecutor;
import net.slimevoid.dynamictransport.network.packet.executors.MarkerConfigurationExecutor;
import net.slimevoid.library.network.PacketIds;
import net.slimevoid.library.network.handlers.PacketPipeline;
import net.slimevoid.library.util.helpers.PacketHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

    public static void sendFloorSelection(String floorNumber, String floorName, int x, int y, int z) {
        // create packet
        PacketMarkerData packet = new PacketMarkerData(GuiLib.GUIID_FloorSelection, Integer.valueOf(floorNumber), floorName, x, y, z, 0);
        PacketHelper.sendToServer(packet);
    }

    public static void sendMarkerConfiguration(int floorY, String floorName, int x, int y, int z) {
        PacketMarkerData packet = new PacketMarkerData(GuiLib.GUIID_FLOOR_MARKER, floorY, floorName, x, y, z , 1);
        PacketHelper.sendToServer(packet);
    }
}
