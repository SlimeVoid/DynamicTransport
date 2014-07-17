package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.minecraft.world.IBlockAccess;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.Explosion;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
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
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
    {
        return world.getBlockMetadata(x,y,z);
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity e) {
        return false;
    }

    @Override
    public boolean canCollideCheck(int par1, boolean par2) {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean canDropFromExplosion(Explosion par1Explosion) {
        return false;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>(); // Empty List
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
        return null;
    }

    @Override
    public boolean isAir(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        world.setBlockToAir(x, y, z);
    }

    @Override
    public void onBlockAdded(World world,int x,int y,int z){
        world.scheduleBlockUpdate(x, y, z, this, 10);
    }

    @Override
    public void registerBlockIcons(IIconRegister register){
        blockIcon = register.registerIcon(CoreLib.MOD_RESOURCES + ":emptyTexture");
    }

}
