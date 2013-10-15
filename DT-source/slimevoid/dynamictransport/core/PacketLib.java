package slimevoid.dynamictransport.core;

import slimevoid.dynamictransport.network.CommonPacketHandler;
import slimevoid.dynamictransport.network.handler.PacketGuiHandler;
import slimevoid.dynamictransport.network.packet.executor.PacketGuiExecutor;
import slimevoidlib.network.PacketIds;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLib {
	@SideOnly(Side.CLIENT)
	public static void registerClientPacketHandlers() {

	}

	public static void registerPacketHandlers() {
		PacketGuiHandler packetGuiHandler = new PacketGuiHandler();
		packetGuiHandler.registerPacketHandler(CommandLib.RESET_ELEAVTOR, new PacketGuiExecutor());

		CommonPacketHandler.registerPacketHandler(PacketIds.GUI, packetGuiHandler);
		
	}

}
