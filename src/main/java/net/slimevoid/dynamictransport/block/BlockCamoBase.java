package net.slimevoid.dynamictransport.block;

import net.slimevoid.dynamictransport.common.property.UnlistedPropertyList;
import net.slimevoid.dynamictransport.tileentity.TileEntityCamo;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;

public abstract class BlockCamoBase extends Block {
    public static final PropertyBool CAMO = PropertyBool.create("camo");
    public static final UnlistedPropertyList<IBlockState> CAMO_STATE = new UnlistedPropertyList<>("camo_state");
    public BlockCamoBase(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockCamoBase(Material blockMaterialIn) {
        super(blockMaterialIn);
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCamo) {
            TileEntityCamo e = (TileEntityCamo) tileEntity;
            int sideIndex = facing.getIndex();
            ItemStack originalCamo = e.getCamoSide(sideIndex);
            ItemStack newCamo = null;
            if (playerIn.isSneaking()) {
                newCamo = ItemStack.EMPTY;
            } else {
                ItemStack heldStack = playerIn.getHeldItem(hand);
                if (heldStack != ItemStack.EMPTY) {
                    Block b = getBlockFromItem(heldStack.getItem());
                    if (!(b instanceof BlockCamoBase) && b != getBlockFromItem(originalCamo.getItem()) && b.getDefaultState().isOpaqueCube())
                        newCamo = heldStack.splitStack(1);
                }
            }
            if (newCamo != null) {
                if (!worldIn.isRemote) {
                    e.setCamoSide(sideIndex, newCamo, pos);
                    if (originalCamo != ItemStack.EMPTY) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), originalCamo);
                    }

                    e.markDirty();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return this.fillStateContainer(new BlockStateContainer.Builder(this)).build();
    }

    protected BlockStateContainer.Builder fillStateContainer(BlockStateContainer.Builder builder){
        return builder.add(CAMO).add(CAMO_STATE);
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, IBlockAccess world,@Nonnull BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof  TileEntityCamo) {
            return ((TileEntityCamo) te).getLightValue();
        } else {
            return super.getLightValue(state,world,pos);
        }
    }


    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(CAMO, true);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(@Nonnull IBlockState state)
    {
        return 0;
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state,@Nonnull BlockRenderLayer layer) {
        return true;
    }

    @Override
    @Nonnull
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world,@Nonnull BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCamo) {
            TileEntityCamo e = (TileEntityCamo) tileEntity;
            state = extendedBlockState.withProperty(CAMO_STATE, e.getBlockStates());
        }
        return state;
    }
}
