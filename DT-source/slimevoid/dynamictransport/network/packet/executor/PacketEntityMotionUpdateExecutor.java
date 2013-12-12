package slimevoid.dynamictransport.network.packet.executor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import slimevoid.dynamictransport.network.packet.PacketEntityMotionUpdate;
import slimevoidlib.IPacketExecutor;
import slimevoidlib.network.PacketUpdate;

public class PacketEntityMotionUpdateExecutor implements IPacketExecutor {

	@Override
	public void execute(PacketUpdate packet, World world, EntityPlayer entityplayer) {
		if (packet instanceof PacketEntityMotionUpdate) {
			PacketEntityMotionUpdate packetEntityMotionUpdate = (PacketEntityMotionUpdate) packet;
			if (packet.targetExists(world)) {
				Entity target = packetEntityMotionUpdate.getEntity(world);
				target.posY = packetEntityMotionUpdate.motionY;
			}
		}
	}

}
