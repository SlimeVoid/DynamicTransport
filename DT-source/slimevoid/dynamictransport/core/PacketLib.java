package slimevoid.dynamictransport.core;

import slimevoid.dynamictransport.client.network.ClientPacketHandler;
import slimevoid.dynamictransport.network.CommonPacketHandler;
import slimevoid.dynamictransport.network.handler.PacketEntityMotionUpdateHandler;
import slimevoid.dynamictransport.network.handler.PacketGuiHandler;
import slimevoid.dynamictransport.network.packet.executor.PacketEntityMotionUpdateExecutor;
import slimevoid.dynamictransport.network.packet.executor.PacketGuiExecutor;
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

	public static void registerPacketHandlers() {
		PacketGuiHandler packetGuiHandler = new PacketGuiHandler();
		packetGuiHandler.registerPacketHandler(	CommandLib.RESET_ELEAVTOR,
												new PacketGuiExecutor());

		CommonPacketHandler.registerPacketHandler(	PacketIds.GUI,
													packetGuiHandler);

		PacketEntityMotionUpdateHandler clientLoginHandler = new PacketEntityMotionUpdateHandler();
		clientLoginHandler.registerPacketHandler(	"MOTION",
													new PacketEntityMotionUpdateExecutor());

		CommonPacketHandler.registerPacketHandler(	PacketIds.ENTITY,
													clientLoginHandler);

	}

}
