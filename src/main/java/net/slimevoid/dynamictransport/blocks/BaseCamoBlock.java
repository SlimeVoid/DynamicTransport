package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.CamoTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.OptionalInt;

import static net.minecraft.state.properties.BlockStateProperties.LEVEL_0_15;
import static net.slimevoid.dynamictransport.core.DynamicTransport.CAMO;

public abstract class BaseCamoBlock extends Block {
    BaseCamoBlock() {
        super(Block.Properties.create(Material.IRON));
        setDefaultState(getDefaultState().with(CAMO,true));
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return (getLightValue(state, worldIn, pos) == 0) ? super.getAmbientOcclusionLightValue(state, worldIn, pos):1.0f;
    }

    @Override
    public int getLightValue(BlockState state) {
        return state.get(LEVEL_0_15);
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader worldIn, BlockPos pos, Direction side) {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof CamoTileEntity) {
            CamoTileEntity e = (CamoTileEntity) tileEntity;
            int sideIndex = side.getOpposite().getIndex();
            BlockState camo = e.getBlockStates().get(sideIndex);
            if (!(camo.getBlock() instanceof BaseCamoBlock))
            return camo.getStrongPower(worldIn,pos,side);
        }
        return super.getStrongPower(blockState, worldIn, pos, side);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader worldIn, BlockPos pos, Direction side) {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof CamoTileEntity) {
            CamoTileEntity e = (CamoTileEntity) tileEntity;
            int sideIndex = side.getOpposite().getIndex();
            BlockState camo = e.getBlockStates().get(sideIndex);
            if (!(camo.getBlock() instanceof BaseCamoBlock))
                return camo.getWeakPower(worldIn,pos,side);
        }
        return super.getWeakPower(blockState, worldIn, pos, side);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> container) {
        container.add(CAMO).add(LEVEL_0_15);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
/*
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CamoTileEntity();
    }
*/
    @Override
    @Nonnull
    public ActionResultType onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit) {

        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof CamoTileEntity) {
            CamoTileEntity e = (CamoTileEntity) tileEntity;
            int sideIndex = hit.getFace().getIndex();
            ItemStack originalCamo = e.getCamoSide(sideIndex);
            ItemStack newCamo = null;
            if (player.isCrouching()) {
                newCamo = ItemStack.EMPTY;
            } else {
                ItemStack heldStack = player.getHeldItem(handIn);
                if (heldStack != ItemStack.EMPTY) {
                    Block b = getBlockFromItem(heldStack.getItem());
                    if (!(b instanceof BaseCamoBlock) && b != getBlockFromItem(originalCamo.getItem()) && b.getDefaultState().isOpaqueCube(worldIn, pos))
                        newCamo = heldStack.split(1);
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
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }
}
