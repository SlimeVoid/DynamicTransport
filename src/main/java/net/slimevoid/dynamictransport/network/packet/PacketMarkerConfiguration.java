package net.slimevoid.dynamictransport.network.packet;

import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.CommandLib;
import net.slimevoid.dynamictransport.core.lib.CoreLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.network.PacketGuiEvent;
import net.slimevoid.library.network.PacketPayload;

public class PacketMarkerConfiguration extends PacketGuiEvent {

    public PacketMarkerConfiguration() {
    super();
    this.setChannel(CoreLib.MOD_CHANNEL);
}

    public PacketMarkerConfiguration(int guiId, int destY, String floorName, int markerX, int markerY, int markerZ) {
    this();
    this.payload = new PacketPayload(1, 0, 1, 0);
    this.setGuiID(guiId);
    this.setCommand(CommandLib.UPDATE_MARKER);
    this.setDestinationY(destY);
    this.setFloorName(floorName == null || floorName.equals("") ? "none" : floorName);
    this.xPosition = markerX;
    this.yPosition = markerY;
    this.zPosition = markerZ;
}

    public void setDestinationY(int destinationY) {
        this.payload.setIntPayload(0,
                destinationY);
    }

    public int getDestinationY() {
        return this.payload.getIntPayload(0);
    }

    public void setFloorName(String floorName) {
        this.payload.setStringPayload(0,
                floorName);
    }

    public String getFloorName() {
        return this.payload.getStringPayload(0);
    }

    @Override
    public boolean targetExists(World world) {
        return world.getTileEntity(this.xPosition,
                this.yPosition,
                this.zPosition) instanceof TileEntityFloorMarker;
    }
}

