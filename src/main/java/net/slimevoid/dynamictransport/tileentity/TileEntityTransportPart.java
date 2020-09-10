package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityTransportPart extends TileEntityCamo {
    private BlockPos controller = null;

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if(controller != null)
            compound.setTag("controller", NBTUtil.createPosTag(controller));
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("controller"))
            controller = NBTUtil.getPosFromTag(compound.getCompoundTag("controller"));
    }

    public void setComputer(BlockPos value) {
        controller = value;
        markDirty();
    }

    @Nullable
    public BlockPos getController() {
        return controller;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }
}
