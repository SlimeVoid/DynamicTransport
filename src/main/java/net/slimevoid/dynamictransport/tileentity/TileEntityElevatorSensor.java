package net.slimevoid.dynamictransport.tileentity;

import net.slimevoid.dynamictransport.core.lib.BlockLib;

public class TileEntityElevatorSensor extends TileEntityTransportBase{
    @Override
    protected boolean isInMaintenanceMode() {
        return false;
    }

    @Override
    public int getExtendedBlockID() {
        return 0;
    }

    @Override
    public String getInvName() {
        return BlockLib.BLOCK_ELEVATOR_SENSOR;
    }
}
