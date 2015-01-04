package net.slimevoid.dynamictransport.network.packet.executors;

        import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.network.packet.PacketMarkerData;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.IPacketExecutor;
import net.slimevoid.library.network.PacketUpdate;

        /**
  * Created by Allen on 6/24/2014.
  */
        public class MarkerConfigurationExecutor implements IPacketExecutor {
        @Override
        public void execute(PacketUpdate packet, World world, EntityPlayer entityplayer) {
                if (packet instanceof PacketMarkerData) {
                        PacketMarkerData callPacket = (PacketMarkerData) packet;
                        if (callPacket.targetExists(world)) {
                                TileEntityFloorMarker marker = (TileEntityFloorMarker) world.getTileEntity(new BlockPos(callPacket.xPosition,
                                                callPacket.yPosition,
                                                callPacket.zPosition));
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