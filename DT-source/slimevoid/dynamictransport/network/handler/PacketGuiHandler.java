package slimevoid.dynamictransport.network.handler;

import slimevoid.dynamictransport.network.packet.PacketGui;
import slimevoidlib.network.PacketUpdate;
import slimevoidlib.network.handlers.SubPacketHandler;

public class PacketGuiHandler extends SubPacketHandler {


		@Override
		protected PacketUpdate createNewPacket() {
			return new PacketGui();
		}	

}
