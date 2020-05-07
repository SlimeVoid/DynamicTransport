package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;
import net.slimevoid.dynamictransport.tileentity.TransportPartTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_TOOL;

public abstract class BaseTransportPartBlock extends BaseCamoBlock {
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TransportPartTileEntity();
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack heldItem = player.getHeldItem(handIn);
        if(!heldItem.isEmpty() && heldItem.getItem() == ELEVATOR_TOOL.get() && heldItem.hasTag()){
            BlockPos controller = NBTUtil.readBlockPos(heldItem.getTag().getCompound("pos"));
            TileEntity e = worldIn.getTileEntity(controller);
            if(e instanceof ElevatorControllerTileEntitiy){
                if(!worldIn.isRemote()) {
                    ElevatorControllerTileEntitiy te = (ElevatorControllerTileEntitiy) e;
                    te.addPart(pos, player);
                    worldIn.notifyBlockUpdate(pos, state, state, 3);
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;
        }
        return super.onBlockActivated(state,worldIn,pos,player,handIn,hit);
    }
    //need to notify on destory or unbind.
}
