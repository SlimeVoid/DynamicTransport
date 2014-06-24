package net.slimevoid.dynamictransport.network.packet;

import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.CommandLib;
import net.slimevoid.dynamictransport.core.lib.CoreLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.network.PacketGuiEvent;
import net.slimevoid.library.network.PacketPayload;

public class PacketMarkerData extends PacketGuiEvent {

    public PacketMarkerData() {
        super();
        this.setChannel(CoreLib.MOD_CHANNEL);
    }

    public PacketMarkerData(int guiid, int destY, String floorname, int compX, int compY, int compZ, int commandType) {
        this();
        this.payload = new PacketPayload(1, 0, 1, 0);
        this.setGuiID(guiid);
        switch (commandType){
            case 0:
                this.setCommand(CommandLib.CALL_ELEVATOR);
                break;
            case 1:
                this.setCommand(CommandLib.UPDATE_MARKER);
                break;
        }
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
        if (this.command.equals(CommandLib.CALL_ELEVATOR)) {
            return world.getTileEntity(this.xPosition,
                    this.yPosition,
                    this.zPosition) instanceof TileEntityElevatorComputer;
        }else if (this.command.equals(CommandLib.UPDATE_MARKER)) {
            return world.getTileEntity(this.xPosition,
                    this.yPosition,
                    this.zPosition) instanceof TileEntityFloorMarker;
        }
        else {
            return false;
        }
    }

}
