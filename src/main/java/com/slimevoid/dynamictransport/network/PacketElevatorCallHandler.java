package com.slimevoid.dynamictransport.network;

import net.slimevoid.library.network.PacketUpdate;
import net.slimevoid.library.network.handlers.SubPacketHandler;

import com.slimevoid.dynamictransport.network.packet.PacketElevatorCall;

public class PacketElevatorCallHandler extends SubPacketHandler {

    @Override
    protected PacketUpdate createNewPacket() {
        return new PacketElevatorCall();
    }

}
