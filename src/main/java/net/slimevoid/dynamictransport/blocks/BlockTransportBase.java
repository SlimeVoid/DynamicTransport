package net.slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportBase;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.tileentity.TileEntityBase;
import net.slimevoid.library.util.helpers.BlockHelper;

public class BlockTransportBase extends BlockBase {

    public BlockTransportBase() {
        super(Material.iron, BlockLib.BLOCK_MAX_TILES);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabTransport;
    }

    /**protected IIcon[][] iconList;
    private static IIcon[] iconOverlays;

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
    }**/

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return this.getMetaFromState(world.getBlockState(pos)) != BlockLib.BLOCK_DYNAMIC_MARK_ID && super.shouldCheckWeakPower(world,
                pos,
                side);
    }

    /**@Override
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
    }**/

    @Override
    public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {


        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityTransportBase) {

            ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
            if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
            }

            if (itemstack != null && itemstack.getItem() != null) {
                Block block = Block.getBlockFromItem(itemstack.getItem());
                return block.isProvidingWeakPower(world, pos, state, side);
            }
        }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityTransportBase) {

            ItemStack itemstack = ((TileEntityTransportBase) tile).getCamoItem();
            if (itemstack == null && tile instanceof TileEntityFloorMarker) {
                itemstack = ((TileEntityFloorMarker) tile).getCamoItem();
            }

            if (itemstack != null && itemstack.getItem() != null) {
                Block block = Block.getBlockFromItem(itemstack.getItem());
                return block.isProvidingStrongPower(world, pos, state, side);
            }
        }

        return 0;
    }

    @Override
    public int getRenderType() {
        return ConfigurationLib.ElevatorRenderId;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float xHit, float yHit, float zHit) {
        TileEntityBase tileentity = (TileEntityBase) BlockHelper.getTileEntity(world,
                pos,
                this.getTileMapData(state));
        if (tileentity != null) {
            if (tileentity instanceof TileEntityElevator) {
                return ((TileEntityElevator) tileentity).onBlockActivated(state, entityplayer, side, xHit, yHit, zHit);
            } else {
                return tileentity.onBlockActivated(state, entityplayer, side, zHit, zHit, zHit);
            }
        } else {
            return false;
        }
    }
    
    /**@SideOnly(Side.CLIENT)
    public static IIcon getIconSideOverlay() {
        return iconOverlays[0];
    }**/

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
        ItemStack heldItem = FMLClientHandler.instance().getClientPlayerEntity().getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemElevatorTool) {
            NBTTagCompound tags = heldItem.getTagCompound();
            if (tags != null && tags.hasKey("ComputerX")) {
                BlockPos possibleComputer = new BlockPos(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
                switch (this.getMetaFromState(world.getBlockState(pos))) {
                    case BlockLib.BLOCK_ELEVATOR_COMPUTER_ID:
                        if (possibleComputer.equals(pos)) {
                            return ConfigurationLib.ComputerMaintenanceHighlight;
                        }
                        break;
                    case BlockLib.BLOCK_ELEVATOR_ID:
                        TileEntityElevator elevator = (TileEntityElevator) BlockHelper.getTileEntity(world, pos, TileEntityElevator.class);
                        if (elevator != null && elevator.getParent().equals(possibleComputer)) {
                            return ConfigurationLib.ElevatorMaintenanceHighlight;
                        }
                        break;
                    case BlockLib.BLOCK_DYNAMIC_MARK_ID:
                        TileEntityFloorMarker marker = (TileEntityFloorMarker) BlockHelper.getTileEntity(world, pos, TileEntityFloorMarker.class);
                        if (marker != null && marker.getParentChunkCoords() != null && marker.getParentChunkCoords().equals(possibleComputer)) {
                            return ConfigurationLib.MarkerMaintenanceHighlight;
                        }
                        break;
                }
            }
        }
        return super.colorMultiplier(world, pos, renderPass);
    }
}

