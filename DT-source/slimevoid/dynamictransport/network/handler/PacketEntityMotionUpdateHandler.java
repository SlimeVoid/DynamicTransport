package slimevoid.dynamictransport.network.handler;

import slimevoid.dynamictransport.network.packet.PacketEntityMotionUpdate;
import slimevoidlib.network.PacketUpdate;
import slimevoidlib.network.handlers.SubPacketHandler;

public class PacketEntityMotionUpdateHandler extends SubPacketHandler {

	@Override
	protected PacketUpdate createNewPacket() {
		return new PacketEntityMotionUpdate();
	}

}
