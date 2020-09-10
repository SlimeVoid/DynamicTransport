package net.slimevoid.dynamictransport.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.slimevoid.dynamictransport.common.property.UnlistedPropertyInteger;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.item.ModItems;
import net.slimevoid.dynamictransport.network.play.server.SPacketOpenFloorSelection;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockElevator extends BlockTransportPartBase {
    public static IUnlistedProperty<Integer> OVERLAY = new UnlistedPropertyInteger("overlay",0,63);

    public BlockElevator() {
        super(Material.IRON);
    }
    @Override
    protected BlockStateContainer.Builder fillStateContainer(BlockStateContainer.Builder builder) {
        return super.fillStateContainer(builder).add(OVERLAY);
    }
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityElevator) {
            TileEntityElevator e = (TileEntityElevator) tileEntity;
            extendedBlockState = extendedBlockState.withProperty(OVERLAY, e.getOverlay());
        }
        else
        {
            extendedBlockState = extendedBlockState.withProperty(OVERLAY, 0);
        }
        return extendedBlockState;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing hitFace, float hitx, float hity, float hitz) {

        ItemStack heldItem = player.getHeldItem(handIn);
        if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.getElevatorTool() && !heldItem.hasTagCompound()) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t instanceof TileEntityElevator) {
                if(!worldIn.isRemote)
                    ((TileEntityElevator) t).toggleOverLay(hitFace.getIndex());
                return true;
            }
        } else if (heldItem.isEmpty() || heldItem.getItem() != ModItems.getElevatorTool()) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t instanceof TileEntityElevator) {
                TileEntityElevator te = (TileEntityElevator) t;
                if ((te.getOverlay() & (int)Math.pow(2,hitFace.getIndex())) > 0) {
                    BlockPos controller = te.getController();
                    if(controller != null)
                    {
                        t = worldIn.getTileEntity(controller);
                        if(t instanceof TileEntityElevatorController){
                            if(!worldIn.isRemote)
                                DynamicTransportMod.CHANNEL.sendTo(new SPacketOpenFloorSelection(controller, ((TileEntityElevatorController)t).getBoundMarkerBlocks()),(EntityPlayerMP) player);
                            return true;
                        }
                    }
                }
            }
        }
        return super.onBlockActivated(worldIn,pos,state,player,handIn,hitFace,hitx,hity,hitz);
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityElevator();
    }
}
