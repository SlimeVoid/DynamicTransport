package net.slimevoid.dynamictransport.network;

import net.slimevoid.dynamictransport.network.packet.PacketElevatorCall;
import net.slimevoid.library.network.PacketUpdate;
import net.slimevoid.library.network.handlers.SubPacketHandler;

public class PacketElevatorCallHandler extends SubPacketHandler {

    @Override
    protected PacketUpdate createNewPacket() {
        return new PacketElevatorCall();
    }

}
