package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.entities.ElevatorEntity;
import net.slimevoid.dynamictransport.entities.MasterElevatorEntity;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

import static net.slimevoid.dynamictransport.core.RegistryHandler.*;

public class ElevatorBlock extends BaseTransportPartBlock {


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote && player.getHeldItem(handIn).isEmpty()) {
            MasterElevatorEntity e = MASTER_ELEVATOR_ENTITY.get().create(worldIn);
            e.Initialize(NonNullList.from(BlockPos.ZERO, pos, pos.west(),pos.south(),pos.south().west()),pos.getY(),30,"");
            worldIn.addEntity(e);
        }
        return super.onBlockActivated(state,worldIn,pos,player,handIn,hit);
    }
}
