package net.slimevoid.dynamictransport.network;

import net.slimevoid.dynamictransport.network.packet.PacketMarkerConfiguration;
import net.slimevoid.library.network.PacketUpdate;
import net.slimevoid.library.network.handlers.SubPacketHandler;

/**
 * Created by Allen on 6/24/2014.
 */
public class PacketMarkerConfigurationHandler extends SubPacketHandler {
    @Override
    protected PacketUpdate createNewPacket() {
        return new PacketMarkerConfiguration();
    }
}
