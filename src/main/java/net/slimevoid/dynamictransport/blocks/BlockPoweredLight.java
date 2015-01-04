package net.slimevoid.dynamictransport.blocks;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.CoreLib;

/**
 * Created by Allen on 7/17/2014.
 */
public class BlockPoweredLight extends Block {

    public BlockPoweredLight(int light) {
        super(Material.fire);
        this.lightValue = light;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return this.getMetaFromState(state);
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity e) {
        return false;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean par2) {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canDropFromExplosion(Explosion par1Explosion) {
        return false;
    }

    @Override
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<ItemStack>(); // Empty List
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public boolean isAir(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.setBlockToAir(pos);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state){
        world.scheduleUpdate(pos, this, 10);
    }

    /**@Override
    public void registerBlockIcons(IIconRegister register){
        blockIcon = register.registerIcon(CoreLib.MOD_RESOURCES + ":emptyTexture");
    }**/

}
