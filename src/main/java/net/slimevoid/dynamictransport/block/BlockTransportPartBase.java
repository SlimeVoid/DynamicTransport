package net.slimevoid.dynamictransport.block;

import net.slimevoid.dynamictransport.item.ModItems;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class BlockTransportPartBase extends BlockCamoBase {

    public BlockTransportPartBase(Material blockMaterialIn) {
        super(blockMaterialIn);
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTransportPart();
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,  EntityPlayer player, EnumHand handIn, EnumFacing hitFace, float hitx, float hity, float hitz) {
        ItemStack heldItem = player.getHeldItem(handIn);
        if(!heldItem.isEmpty() && heldItem.getItem() == ModItems.getElevatorTool() && heldItem.hasTagCompound()){
            BlockPos controller = NBTUtil.getPosFromTag(heldItem.getTagCompound().getCompoundTag("pos"));
            TileEntity e = worldIn.getTileEntity(controller);
            if(e instanceof TileEntityElevatorController){
                if(!worldIn.isRemote) {
                    TileEntityElevatorController te = (TileEntityElevatorController) e;
                    player.sendStatusMessage(te.addPart(pos),true);
                    worldIn.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
            return false;
        }
        return super.onBlockActivated(worldIn,pos,state,player,handIn,hitFace,hitx,hity,hitz);
    }


}
