package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.util.helpers.ChatHelper;

public class TileEntityElevator extends TileEntityTransportBase {

    private BlockPos		 connectionPos;
    private int              yOffset = 0;
    private int              maxY    = -1;
    private int              minY    = -1;
    private short            overlay = 0;

    public int getMaxY(boolean reCalculate) {
        if (reCalculate || maxY == -1) {
            for (int y = this.pos.getY(); y < this.worldObj.getActualHeight(); y++) {
                if (!this.worldObj.isAirBlock(this.pos.add(0, 1, 0))
                    || y == this.worldObj.getActualHeight()) {
                    this.maxY = y;
                    break;
                }
            }
        }

        return maxY;
    }

    public int getMinY(boolean reCalculate) {
        if (reCalculate || minY == -1) {
            for (int y = this.pos.getY(); y >= 0; y--) {
                if (!this.worldObj.isAirBlock(this.pos.add(0, -1, 0)) || y == 0) {
                    this.minY = y;
                    break;
                }
            }
        }

        return minY;
    }

    @Override
    public boolean onBlockActivated(IBlockState blockState, EntityPlayer entityplayer, EnumFacing side, float xHit, float yHit, float zHit) {
        if (this.getWorld().isRemote) {
            return true;
        }
        short digitLoc = (short) Math.pow(2, side.getIndex());
        ItemStack heldItem = entityplayer.getHeldItem();
        if (heldItem != null
            && heldItem.getItem() == ConfigurationLib.itemElevatorTool) {

            if (heldItem.hasTagCompound()
                && entityplayer.getHeldItem().getTagCompound() != null) {
                NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
                if (tags != null && tags.hasKey("ComputerX")) {
                    BlockPos possibleComputer = new BlockPos(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
                    if (entityplayer.isSneaking()) {
                        if (possibleComputer.equals(this.connectionPos)) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorBlock.unbound");// "Block Unbound"
                            this.getConnection().RemoveElevatorBlock(pos.add(0, -this.pos.getY() + this.getYOffest(), 0));
                            this.RemoveComputer(possibleComputer);
                            return true;
                        } else if (this.connectionPos != null) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorBlock.boundToOtherComputer");// "Block Bound to Another Elevator"
                        }
                    } else {
                        if (this.getConnectionPos() == null) {
                            setParent(possibleComputer,
                                    entityplayer);
                        } else if (possibleComputer.equals(this.getConnectionPos())) {

                            if ((this.overlay & digitLoc) == digitLoc){
                                this.overlay = (short)((this.overlay & ~digitLoc) & 127);
                            }else{
                                this.overlay = (short)(this.overlay | digitLoc);
                            }
                            this.getWorld().markBlockForUpdate(this.pos);
                            return true;
                        }
                    }
                }
            } else {
                FMLCommonHandler.instance().getFMLLogger().warn("There was an error processing this Transport Component at ["
                                                                + this.pos.toString()
                                                                + "]");
            }
        }

        if (!this.isInMaintenanceMode() && (this.overlay & digitLoc) == digitLoc){
            entityplayer.openGui(DynamicTransportMod.instance,
                    GuiLib.GUIID_FloorSelection,
                    this.worldObj,
                    this.pos.getX(),
                    this.pos.getY(),
                    this.pos.getZ());
            return true;
        }
        return super.onBlockActivated(blockState, entityplayer, side, xHit, yHit, zHit);
    }

    public void setParent(BlockPos pos) {
        TileEntityTransportComputer comTile = connectionPos == null ? null : (TileEntityTransportComputer) this.worldObj.getTileEntity(this.connectionPos);
        if (comTile == null) this.connectionPos = null;
        IBlockState state = this.worldObj.getBlockState(pos);
        if (state.getBlock() == ConfigurationLib.blockTransportBase
            && Block.getStateId(state) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {
            this.connectionPos = pos;
        }

    }

    public void setParent(BlockPos pos, EntityPlayer entityplayer) {
        TileEntityTransportComputer comTile = connectionPos == null ? null : (TileEntityTransportComputer) this.worldObj.getTileEntity(this.connectionPos);
        if (comTile == null) this.connectionPos = null;
        IBlockState state = this.worldObj.getBlockState(pos);

        if (state.getBlock() == ConfigurationLib.blockTransportBase
            && Block.getStateId(state) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {

            comTile = (TileEntityTransportComputer) this.worldObj.getTileEntity(pos);
            if (comTile.addElevator(this.pos,
                                    entityplayer)) this.connectionPos = pos;

            this.yOffset = this.pos.getY() - (pos.getY() + 1);
        } else {
            ItemStack heldItem = entityplayer.getHeldItem();
            NBTTagCompound tags = new NBTTagCompound();
            ChatHelper.addMessageToPlayer(entityplayer,
                                          "slimevoid.DT.elevatorBlock.bindMissingElevator");
            heldItem.setTagCompound(tags);
        }
        this.updateBlock();
    }

    @Override
    public boolean removeBlockByPlayer(EntityPlayer player, BlockBase blockBase, boolean willHarvest) {
        TileEntityTransportComputer comTile = connectionPos == null ? null : (TileEntityTransportComputer) this.worldObj.getTileEntity(this.connectionPos);
        if (comTile != null) {
            ((TileEntityTransportComputer) comTile).RemoveElevatorBlock(this.pos.add(0, -this.pos.getY() + this.getYOffest(), 0));
        }
        return super.removeBlockByPlayer(player,
                                         blockBase,
                                         willHarvest);
    }

    public BlockPos getConnectionPos() {
        return this.connectionPos;
    }

    public TileEntityTransportComputer getConnection() {
        TileEntity tile = connectionPos == null ? null : this.worldObj.getTileEntity(this.connectionPos);
        if (tile == null) {
            connectionPos = null;
        } else if (!(tile instanceof TileEntityTransportComputer)) {
            tile = null;
            connectionPos = null;
        }

        return (TileEntityTransportComputer) tile;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        if (connectionPos != null) {
            nbttagcompound.setInteger("ParentElevatorComputerX",
                                      connectionPos.getX());
            nbttagcompound.setInteger("ParentElevatorComputerY",
                                      connectionPos.getY());
            nbttagcompound.setInteger("ParentElevatorComputerZ",
                                      connectionPos.getZ());
            nbttagcompound.setInteger("yOffset",
                                      this.yOffset);
            nbttagcompound.setShort("overLay",
                    this.overlay);
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.connectionPos = new BlockPos(nbttagcompound.getInteger("ParentElevatorComputerX"), nbttagcompound.getInteger("ParentElevatorComputerY"), nbttagcompound.getInteger("ParentElevatorComputerZ"));
        this.yOffset = nbttagcompound.getInteger("yOffset");
        this.overlay = nbttagcompound.getShort("overLay");
    }

    public void RemoveComputer(BlockPos chunkCoordinates) {
        this.connectionPos = null;
        this.updateBlock();
    }

    @Override
    public int getExtendedBlockID() {
        return BlockLib.BLOCK_ELEVATOR_ID;
    }

    @Override
    protected boolean isInMaintenanceMode() {
        return this.getConnection() == null
               || this.getConnection().isInMaintenanceMode();
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;

    }

    public int getYOffest() {
        return this.yOffset;
    }

    @Override
    public String getInvName() {
        return BlockLib.BLOCK_ELEVATOR;
    }

    public short getOverlay() {
        return overlay;
    }

    public void setOverlay(Short overlay) {
        this.overlay = overlay;
    }
}