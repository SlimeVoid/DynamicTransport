package net.slimevoid.dynamictransport.network;

import net.slimevoid.dynamictransport.network.packet.PacketMarkerData;
import net.slimevoid.library.network.PacketUpdate;
import net.slimevoid.library.network.handlers.SubPacketHandler;

public class PacketMarkerGUIHandler extends SubPacketHandler {

    @Override
    protected PacketUpdate createNewPacket() {
        return new PacketMarkerData();
    }

}
