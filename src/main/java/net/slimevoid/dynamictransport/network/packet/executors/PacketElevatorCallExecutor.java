package net.slimevoid.dynamictransport.network.packet.executors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.network.packet.PacketElevatorCall;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.library.IPacketExecutor;
import net.slimevoid.library.network.PacketUpdate;

public class PacketElevatorCallExecutor implements IPacketExecutor {

    @Override
    public void execute(PacketUpdate packet, World world, EntityPlayer entityplayer) {
        if (packet instanceof PacketElevatorCall) {
            PacketElevatorCall callPacket = (PacketElevatorCall) packet;
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
