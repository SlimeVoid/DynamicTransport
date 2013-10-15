package slimevoid.dynamictransport.network.packet.executor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import slimevoid.dynamictransport.container.ContainerDynamicElevator;
import slimevoid.dynamictransport.network.packet.PacketGui;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoidlib.IPacketExecutor;
import slimevoidlib.network.PacketUpdate;

public class PacketGuiExecutor implements IPacketExecutor {


		@Override
		public void execute(PacketUpdate packet, World world,
				EntityPlayer entityplayer) {
			if (packet instanceof PacketGui) {
				PacketGui packetGui = (PacketGui) packet;
				if (packet.targetExists(world)) {
					TileEntity tileentity = packetGui.getTarget(world);
					if (tileentity != null && tileentity instanceof TileEntityElevator) {
						if (entityplayer.openContainer instanceof ContainerDynamicElevator) {
							((ContainerDynamicElevator) entityplayer.openContainer).handleGuiEvent(packetGui);
						}
					}
				}
			}
		}

	

}
