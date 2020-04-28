package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.slimevoid.dynamictransport.core.RegistryHandler;

import javax.annotation.Nonnull;

public class TransportPartTileEntity extends CamoTileEntity {
    private BlockPos controller = null;
    public TransportPartTileEntity(){super(RegistryHandler.TRANSPORT_PART_TILE_ENTITY.get());}

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(controller != null)
            compound.put("controller", NBTUtil.writeBlockPos(controller));
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        if(compound.contains("controller"))
            controller = NBTUtil.readBlockPos(compound.getCompound("controller"));
    }

    public void setComputer(BlockPos value) {
        controller = value;
        markDirty();
    }

    public BlockPos getController() {
        return controller;
    }
}
