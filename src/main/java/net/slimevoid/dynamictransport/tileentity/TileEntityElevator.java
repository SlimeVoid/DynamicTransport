package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class TileEntityElevator extends TileEntityTransportPart {

    int overlay = 0;

    public void toggleOverLay(int bitIn){
        int mask = (int)Math.pow(2, bitIn);
        if((overlay & mask) > 0){
            overlay &= ~mask;
        }else{
            overlay |= mask;
        }
        this.markDirtyClient();
    }

    public int getOverlay(){
        return  overlay;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("overlay",overlay);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        overlay = compound.getInteger("overlay");
    }

    public void setOverlay(int overlay) {
        this.overlay = overlay;
        this.markDirtyClient();
    }
}
