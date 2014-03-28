package com.slimevoid.dynamictransport.network.packet;

import net.minecraft.world.World;
import net.slimevoid.library.network.PacketGuiEvent;
import net.slimevoid.library.network.PacketPayload;

import com.slimevoid.dynamictransport.core.lib.CommandLib;
import com.slimevoid.dynamictransport.core.lib.CoreLib;
import com.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;

public class PacketElevatorCall extends PacketGuiEvent {

    public PacketElevatorCall() {
        super();
        this.setChannel(CoreLib.MOD_CHANNEL);
    }

    public PacketElevatorCall(int guiid, int destY, String floorname, int compX, int compY, int compZ) {
        this();
        this.payload = new PacketPayload(1, 0, 1, 0);
        this.setGuiID(guiid);
        this.setCommand(CommandLib.CALL_ELEVATOR);
        this.setDestinationY(destY);
        this.setFloorName(floorname == null || floorname.equals("") ? "none" : floorname);
        this.xPosition = compX;
        this.yPosition = compY;
        this.zPosition = compZ;
    }

    public void setDestinationY(int destinationY) {
        this.payload.setIntPayload(0,
                                   destinationY);
    }

    public int getDestinationY() {
        return this.payload.getIntPayload(0);
    }

    public void setFloorName(String floorname) {
        this.payload.setStringPayload(0,
                                      floorname);
    }

    public String getFloorName() {
        return this.payload.getStringPayload(0);
    }

    @Override
    public boolean targetExists(World world) {
        return world.getTileEntity(this.xPosition,
                                   this.yPosition,
                                   this.zPosition) instanceof TileEntityElevatorComputer;
    }

}
