package net.slimevoid.dynamictransport.network.packet.executors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.network.packet.PacketMarkerData;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.library.IPacketExecutor;
import net.slimevoid.library.network.PacketUpdate;

public class ElevatorCallExecutor implements IPacketExecutor {

    @Override
    public void execute(PacketUpdate packet, World world, EntityPlayer entityplayer) {
        if (packet instanceof PacketMarkerData) {
            PacketMarkerData callPacket = (PacketMarkerData) packet;
            if (callPacket.targetExists(world)) {
                TileEntityElevatorComputer comp = (TileEntityElevatorComputer) world.getTileEntity(callPacket.xPosition,
                                                                                                   callPacket.yPosition,
                                                                                                   callPacket.zPosition);
                String floorname = callPacket.getFloorName();
                if (callPacket.getFloorName().equals("none")) {
                    comp.callElevator(callPacket.getDestinationY(),
                                      "");
                } else {
                    comp.callElevator(callPacket.getDestinationY(),
                                      floorname);
                }
            }
        }
    }
}
