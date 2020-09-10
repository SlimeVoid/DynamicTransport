package net.slimevoid.dynamictransport.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.item.ModItems;
import net.slimevoid.dynamictransport.network.ModGuiHandler;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMarker extends BlockTransportPartBase {
    public BlockMarker() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockRedstoneComparator.POWERED, false));
    }

    @Override
    protected BlockStateContainer.Builder fillStateContainer(BlockStateContainer.Builder container) {
        return super.fillStateContainer(container.add(BlockRedstoneComparator.POWERED));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@Nonnull IBlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if (!worldIn.isRemote) {
            boolean flag = worldIn.isBlockPowered(pos);
            if (state.getValue(BlockRedstoneComparator.POWERED) != flag) {
                if (flag) {
                    TileEntity e = worldIn.getTileEntity(pos);
                    if (e instanceof TileEntityMarker) {
                        TileEntityMarker me = (TileEntityMarker)e;
                        BlockPos controllerPos = me.getController();
                        if (controllerPos != null) {
                            TileEntity c = worldIn.getTileEntity(controllerPos);
                            if (c instanceof TileEntityElevatorController) {
                                if(((TileEntityElevatorController) c).callElevator(me.getDestination(), me.getFloorName()) && worldIn.getMinecraftServer() != null)
                                    worldIn.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 4, worldIn.provider.getDimension(),
                                            new SPacketChat(new TextComponentTranslation("elevator.called"), ChatType.GAME_INFO));
                            }
                        }
                    }
                }
                worldIn.setBlockState(pos, state.withProperty(BlockRedstoneComparator.POWERED, flag));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing hitFace, float hitx, float hity, float hitz) {

        if(worldIn.isRemote) {
            ItemStack heldItem = player.getHeldItem(handIn);
            if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.getElevatorTool() && !heldItem.hasTagCompound()) {
                player.openGui(DynamicTransportMod.MOD_ID, ModGuiHandler.MARKER_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return super.onBlockActivated(worldIn,pos,state,player,handIn,hitFace,hitx,hity,hitz);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityMarker();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(BlockRedstoneComparator.POWERED,(meta & 1) > 0);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(BlockRedstoneComparator.POWERED) ? 1:0;
    }
}
