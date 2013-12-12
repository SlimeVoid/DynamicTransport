package slimevoid.dynamictransport.core.lib;

import slimevoid.dynamictransport.client.network.ClientPacketHandler;
import slimevoid.dynamictransport.network.handler.PacketEntityMotionUpdateHandler;
import slimevoid.dynamictransport.network.packet.executor.PacketEntityMotionUpdateExecutor;
import slimevoidlib.network.PacketIds;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLib {
	@SideOnly(Side.CLIENT)
	public static void registerClientPacketHandlers() {
		PacketEntityMotionUpdateHandler clientLoginHandler = new PacketEntityMotionUpdateHandler();
		clientLoginHandler.registerPacketHandler(	"MOTION",
													new PacketEntityMotionUpdateExecutor());

		ClientPacketHandler.registerPacketHandler(	PacketIds.ENTITY,
													clientLoginHandler);

	}

}
