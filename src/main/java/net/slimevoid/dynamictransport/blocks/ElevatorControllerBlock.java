package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_TOOL;

public class ElevatorControllerBlock extends Block {
    private static final IProperty<Direction> FACING = HorizontalBlock.HORIZONTAL_FACING;

    public ElevatorControllerBlock() {
        super(Block.Properties.create(Material.IRON));
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ElevatorControllerTileEntitiy();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    @Nonnull
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {
        ItemStack heldItem = player.getHeldItem(handIn);
        if(!heldItem.isEmpty() && heldItem.getItem() == ELEVATOR_TOOL.get()){
            CompoundNBT tag = new CompoundNBT();
            tag.put("pos", NBTUtil.writeBlockPos(pos));
            heldItem.setTag(tag);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
