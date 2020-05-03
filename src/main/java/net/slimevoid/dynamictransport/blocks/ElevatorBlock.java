package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.entities.MasterElevatorEntity;

public class ElevatorBlock extends BaseTransportPartBlock {


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote && player.getHeldItem(handIn).isEmpty() && !player.isCrouching()) {
            MasterElevatorEntity e = new MasterElevatorEntity(worldIn,
                    NonNullList.from(BlockPos.ZERO, pos, pos.west(),pos.south(),pos.south().west()),
                    pos.getY(),
                    30,
                    "");
            worldIn.addEntity(e);

            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state,worldIn,pos,player,handIn,hit);
    }
}
