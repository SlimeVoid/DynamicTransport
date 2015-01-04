package net.slimevoid.dynamictransport.blocks;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportBase;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.data.Logger;
import net.slimevoid.library.data.LoggerSlimevoidLib;
import net.slimevoid.library.tileentity.TileEntityBase;
import net.slimevoid.library.util.helpers.BlockHelper;

public class BlockTransportBase extends BlockBase {

    protected IIcon[][] iconList;
    private static IIcon[] iconOverlays;

    public BlockTransportBase() {
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
        iconOverlays = new IIcon[1];
        iconOverlays = BlockLib.registerIconOverLays(iconRegister, iconOverlays);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x,
                y,
                z) != BlockLib.BLOCK_DYNAMIC_MARK_ID && super.shouldCheckWeakPower(world,
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
        return null;
    }

    @Override
    public IIcon[] registerTopIcons(IIconRegister iconRegister) {
        return null;
    }

    @Override
    public IIcon[] registerFrontIcons(IIconRegister iconRegister) {
        return null;
    }

    @Override
    public IIcon[] registerSideIcons(IIconRegister iconRegister) {
        return null;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {


        TileEntity tile = world.getTileEntity(x,
                y,
                z);
        if (tile instanceof TileEntityFloorMarker) {
            if (((TileEntityFloorMarker) tile).atFloor) {
                return 3;
            }
        }
        if (tile instanceof TileEntityTransportBase) {

            ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
            if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
            }

            if (itemstack != null && itemstack.getItem() != null) {
                Block block = Block.getBlockFromItem(itemstack.getItem());
                return block.isProvidingWeakPower(world, x, y, z, side);
            }
        }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {


        TileEntity tile = world.getTileEntity(x,
                y,
                z);
        if (tile instanceof TileEntityFloorMarker) {
            if (((TileEntityFloorMarker) tile).atFloor) {
                return 3;
            }
        }
        if (tile instanceof TileEntityTransportBase) {

            ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
            if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
            }

            if (itemstack != null && itemstack.getItem() != null) {
                Block block = Block.getBlockFromItem(itemstack.getItem());
                return block.isProvidingStrongPower(world, x, y, z, side);
            }
        }

        return 0;
    }

    @Override
    public int getRenderType() {
        return ConfigurationLib.ElevatorRenderId;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float xHit, float yHit, float zHit) {
        int metadata = world.getBlockMetadata(x,
                y,
                z);
        TileEntityBase tileentity = (TileEntityBase) BlockHelper.getTileEntity(world,
                x,
                y,
                z,
                this.getTileMapData(metadata));
        if (tileentity != null) {
            if (tileentity instanceof TileEntityElevator) {
                return ((TileEntityElevator) tileentity).onBlockActivated(entityplayer, side, xHit, yHit, zHit);
            } else {
                return tileentity.onBlockActivated(entityplayer);
            }
        } else {
            return false;
        }
    }
    @SideOnly(Side.CLIENT)
    public static IIcon getIconSideOverlay() {
        return iconOverlays[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        ItemStack heldItem = FMLClientHandler.instance().getClientPlayerEntity().getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemElevatorTool) {
            NBTTagCompound tags = heldItem.getTagCompound();
            if (tags != null && tags.hasKey("ComputerX")) {
                ChunkCoordinates possibleComputer = new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
                switch (world.getBlockMetadata(x, y, z)) {
                    case BlockLib.BLOCK_ELEVATOR_COMPUTER_ID:
                        if (possibleComputer.equals(new ChunkCoordinates(x, y, z))) {
                            return ConfigurationLib.ComputerMaintenanceHighlight;
                        }
                        break;
                    case BlockLib.BLOCK_ELEVATOR_ID:
                        TileEntityElevator elevator = (TileEntityElevator) BlockHelper.getTileEntity(world, x, y, z, TileEntityElevator.class);
                        if (elevator != null && elevator.getParent().equals(possibleComputer)) {
                            return ConfigurationLib.ElevatorMaintenanceHighlight;
                        }
                        break;
                    case BlockLib.BLOCK_DYNAMIC_MARK_ID:
                        TileEntityFloorMarker marker = (TileEntityFloorMarker) BlockHelper.getTileEntity(world, x, y, z, TileEntityFloorMarker.class);
                        if (marker != null && marker.getParentChunkCoords() != null && marker.getParentChunkCoords().equals(possibleComputer)) {
                            return ConfigurationLib.MarkerMaintenanceHighlight;
                        }
                        break;
                }
            }
        }
        return super.colorMultiplier(world,x,y,z);
    }
}

