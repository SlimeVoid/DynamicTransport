package com.slimevoid.dynamictransport.blocks;

import com.slimevoid.dynamictransport.core.lib.BlockLib;
import com.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import com.slimevoid.dynamictransport.tileentity.TileEntityTransportBase;
import com.slimevoid.library.blocks.BlockBase;
import com.slimevoid.library.data.Logger;
import com.slimevoid.library.data.LoggerSlimevoidLib;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTransportBase extends BlockBase {

    protected Icon[][] iconList;

    public BlockTransportBase(int blockID) {
        super(blockID, Material.iron, BlockLib.BLOCK_MAX_TILES);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabTransport;
    }

    @Override
    public Icon getIcon(int side, int metadata) {

        return this.iconList[metadata][side];

    }

    @Override
    public void registerIcons(IconRegister iconRegister) {
        iconList = new Icon[BlockLib.BLOCK_MAX_TILES][6];
        iconList = BlockLib.registerIcons(iconRegister,
                                          iconList);
    }

    @Override
    public boolean shouldCheckWeakPower(World world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x,
                                      y,
                                      z) == BlockLib.BLOCK_DYNAMIC_MARK_ID ? false : super.shouldCheckWeakPower(world,
                                                                                                                x,
                                                                                                                y,
                                                                                                                z,
                                                                                                                side);
    }

    @Override
    public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
        Icon output = this.iconList[iblockaccess.getBlockMetadata(i,
                                                                  j,
                                                                  k)][l];
        try {
            TileEntity tile = iblockaccess.getBlockTileEntity(i,
                                                              j,
                                                              k);
            if (tile instanceof TileEntityTransportBase) {

                ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
                if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                    itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
                }

                if (itemstack != null) {
                    int blockID = ((ItemBlock) itemstack.getItem()).getBlockID();
                    int damage = itemstack.getItemDamage();
                    output = Block.blocksList[blockID].getIcon(l,
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

}
