package net.slimevoid.dynamictransport.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.entity.EntityElevator;
import net.slimevoid.dynamictransport.entity.EntityTransportPart;

import java.util.Random;

public class ModBlocks {
    private static Block controller;
    private static Block marker;
    private static Block elevator;
    private static Block[] transientBlocks;
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    public static final MaterialTransparent materialTransparent = new MaterialTransparent(MapColor.AIR);
    public static void init(){
        controller = new BlockElevatorController().setCreativeTab(CreativeTabs.TRANSPORTATION).setRegistryName("controller").setUnlocalizedName("dynamictransport.controller");
        marker = new BlockMarker().setCreativeTab(CreativeTabs.TRANSPORTATION).setRegistryName("marker").setUnlocalizedName("dynamictransport.marker");
        elevator = new BlockElevator().setCreativeTab(CreativeTabs.TRANSPORTATION).setRegistryName("elevator").setUnlocalizedName("dynamictransport.elevator");
        transientBlocks = new Block[16];
        for(int j = 0; j<16;j++) {
            int finalJ = j;
            transientBlocks[j] = new BlockAir(){
                @Override
                public int getLightValue(IBlockState state) {
                    return finalJ;
                }

                @Override
                protected BlockStateContainer createBlockState() {
                    return new BlockStateContainer(this, POWER);
                }


                @Override
                public int getMetaFromState(IBlockState state) {
                    return state.getValue(POWER);
                }

                @Override
                public IBlockState getStateFromMeta(int meta) {
                    return super.getStateFromMeta(meta).withProperty(POWER, meta);
                }

                @Override
                public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
                    return blockState.getValue(POWER);
                }

                @Override
                public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
                    if(state.getBlock() == this){
                        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 7);
                    }
                }


                @Override
                public Material getMaterial(IBlockState state) {
                    return materialTransparent;
                }

                @Override
                public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
                    return true;
                }
            }.setRegistryName("powered_light" + j);
        }

    }
    public static Block getController() {
        return controller;
    }
    public static Block getMarker() {
        return marker;
    }
    public static Block getElevator() {
        return elevator;
    }
    public static Block[] getTransientBlocks() {
        return transientBlocks;
    }
}
