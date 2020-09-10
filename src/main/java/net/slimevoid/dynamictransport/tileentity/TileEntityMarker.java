package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class TileEntityMarker extends TileEntityTransportPart {
    public int offSet = -2;
    public String floorName = "";

    public String getFloorName() {
        if(floorName == null || floorName.trim().length() == 0)
            return Integer.toString (this.getPos().getY() + offSet);
        return floorName;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("offSet",offSet);
        if(floorName != null)
            compound.setString("floorName", floorName);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        offSet = compound.getInteger("offSet");
        floorName = compound.getString("floorName");
    }

    public int getDestination() {
        return this.getPos().getY() + this.offSet;
    }
}
