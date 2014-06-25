package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportBase;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.data.Logger;
import net.slimevoid.library.data.LoggerSlimevoidLib;

public class BlockTransportBase extends BlockBase {

    protected IIcon[][] iconList;

    public BlockTransportBase(int blockID) {
        super(Material.iron, BlockLib.BLOCK_MAX_TILES);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabTransport;
    }

    @Override
    public IIcon getIcon(int side, int metadata) {

        return this.iconList[metadata][side];

    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        iconList = new IIcon[BlockLib.BLOCK_MAX_TILES][6];
        iconList = BlockLib.registerIcons(iconRegister,
                                          iconList);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x,
                                      y,
                                      z) == BlockLib.BLOCK_DYNAMIC_MARK_ID ? false : super.shouldCheckWeakPower(world,
                                                                                                                x,
                                                                                                                y,
                                                                                                                z,
                                                                                                                side);
    }

    @Override
    public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
        IIcon output = this.iconList[iblockaccess.getBlockMetadata(i,
                                                                   j,
                                                                   k)][l];
        try {
            TileEntity tile = iblockaccess.getTileEntity(i,
                                                         j,
                                                         k);
            if (tile instanceof TileEntityTransportBase) {

                ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
                if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                    itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
                }

                if (itemstack != null && itemstack.getItem() != null) {
                    Block block = Block.getBlockFromItem(itemstack.getItem());
                    int damage = itemstack.getItemDamage();
                    output = block.getIcon(l,
                                           damage);
                }
            }
        } catch (Exception e) {
            LoggerSlimevoidLib.getInstance(LoggerSlimevoidLib.filterClassName(this.getClass().toString())).write(false,
                                                                                                                 "Failed to get Camo Item",
                                                                                                                 Logger.LogLevel.WARNING);
            LoggerSlimevoidLib.getInstance(LoggerSlimevoidLib.filterClassName(this.getClass().toString())).writeStackTrace(e);

        }

        return output;
    }

    @Override
    public IIcon[] registerBottomIcons(IIconRegister iconRegister) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIcon[] registerTopIcons(IIconRegister iconRegister) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIcon[] registerFrontIcons(IIconRegister iconRegister) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIcon[] registerSideIcons(IIconRegister iconRegister) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
    {


            TileEntity tile = world.getTileEntity(x,
                    y,
                    z);
            if (tile instanceof TileEntityTransportBase) {

                ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
                if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                    itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
                }

                if (itemstack != null && itemstack.getItem() != null) {
                    Block block = Block.getBlockFromItem(itemstack.getItem());
                    return block.isProvidingWeakPower(world,x,y,z,side);
                }
            }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
    {


            TileEntity tile = world.getTileEntity(x,
                    y,
                    z);
            if (tile instanceof TileEntityTransportBase) {

                ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
                if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                    itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
                }

                if (itemstack != null && itemstack.getItem() != null) {
                    Block block = Block.getBlockFromItem(itemstack.getItem());
                    return block.isProvidingStrongPower(world,x,y,z,side);
                }
            }

            return 0;
    }
}
