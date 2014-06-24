package net.slimevoid.dynamictransport.network.packet.executors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.network.packet.PacketMarkerConfiguration;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.IPacketExecutor;
import net.slimevoid.library.network.PacketUpdate;

/**
 * Created by Allen on 6/24/2014.
 */
public class PacketMarkerConfigurationExecutor implements IPacketExecutor {
    @Override
    public void execute(PacketUpdate packet, World world, EntityPlayer entityplayer) {
        if (packet instanceof PacketMarkerConfiguration) {
            PacketMarkerConfiguration callPacket = (PacketMarkerConfiguration) packet;
            if (callPacket.targetExists(world)) {
                TileEntityFloorMarker marker = (TileEntityFloorMarker) world.getTileEntity(callPacket.xPosition,
                        callPacket.yPosition,
                        callPacket.zPosition);
                String floorName = callPacket.getFloorName();
                marker.setFloorY(callPacket.getDestinationY());
                if (!callPacket.getFloorName().equals("none")) {
                    marker.setFloorName(floorName);
                }
                marker.updateBlock();
            }
        }
    }
}
