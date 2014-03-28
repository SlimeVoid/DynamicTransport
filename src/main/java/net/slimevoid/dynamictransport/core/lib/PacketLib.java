package net.slimevoid.dynamictransport.core.lib;

import net.slimevoid.dynamictransport.network.PacketElevatorCallHandler;
import net.slimevoid.dynamictransport.network.packet.PacketElevatorCall;
import net.slimevoid.dynamictransport.network.packet.executors.PacketElevatorCallExecutor;
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
        PacketElevatorCallHandler callHandler = new PacketElevatorCallHandler();
        callHandler.registerServerExecutor(CommandLib.CALL_ELEVATOR,
                                           new PacketElevatorCallExecutor());

        handler.registerPacketHandler(PacketIds.GUI,
                                      callHandler);
    }

    public static void sendFloorSelection(String floorNumber, String floorName, int x, int y, int z) {
        // create packet
        PacketElevatorCall packet = new PacketElevatorCall(GuiLib.GUIID_FloorSelection, Integer.valueOf(floorNumber), floorName, x, y, z);
        PacketHelper.sendToServer(packet);
    }
}
