package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.util.helpers.ChatHelper;

public class TileEntityFloorMarker extends TileEntityTransportBase {

    private BlockPos parentTransportBase;
    private String           floorName;
    private boolean          Powered = false;
    private int              yOffset = -2;

    public BlockPos getParentChunkCoords() {
        return this.parentTransportBase;
    }

    public TileEntityElevatorComputer getParentElevatorComputer() {
        TileEntity tile = parentTransportBase == null ? null : this.worldObj.getTileEntity(this.parentTransportBase);
        if (tile == null) {
            parentTransportBase = null;
        } else if (!(tile instanceof TileEntityElevatorComputer)) {
            tile = null;
            parentTransportBase = null;
        }

        return (TileEntityElevatorComputer) tile;
    }

    @Override
    public boolean isSideSolid(BlockBase blockBase, EnumFacing side) {
        return true;
    }

    @Override
    public void onNeighborChange(BlockPos pos) {
        boolean flag = this.worldObj.isBlockIndirectlyGettingPowered(this.pos) > 0;

        if (!this.Powered) {
            if (flag) {
                this.Powered = true;
                this.callElevator();
            }
        } else if (!flag) {
            this.Powered = false;
        }
    }

    private void callElevator() {

        TileEntityElevatorComputer comTile = this.getParentElevatorComputer();
        if (comTile != null) {
            String msg = comTile.callElevator(this.pos.getY() + this.yOffset,
                                              this.floorName);
            if (!this.worldObj.isRemote) {
                ChatHelper.sendChatMessageToAllNear(this.getWorld(),
                                                    this.pos.getX(),
                                                    this.pos.getY(),
                                                    this.pos.getZ(),
                                                    4,
                                                    msg);
            }
        }
    }

    @Override
    public boolean onBlockActivated(IBlockState blockState, EntityPlayer entityplayer, EnumFacing side, float xHit, float yHit, float zHit) {
        if (this.getWorld().isRemote) {
            return true;
        }
        ItemStack heldItem = entityplayer.getHeldItem();
        if (heldItem != null
            && heldItem.getItem() == ConfigurationLib.itemElevatorTool) {
            if (heldItem.hasTagCompound()
                && entityplayer.getHeldItem().getTagCompound() != null) {
                NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
                if (tags != null && tags.hasKey("ComputerX")) {
                    BlockPos possibleComputer = new BlockPos(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
                    if (entityplayer.isSneaking()) {
                        if (possibleComputer.equals(this.parentTransportBase)) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.dynamicMarker.unbound");// "Block Unbound"
                            this.getParentElevatorComputer().removeMarkerBlock(this.pos);
                            this.removeParent();
                            return true;
                        } else if (this.parentTransportBase != null) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.dynamicMarker.boundToOtherComputer");// "Block Bound to Another Elevator"
                        }
                    } else {
                        if (this.parentTransportBase == null) {
                            setParentComputer(possibleComputer,
                                              entityplayer);
                        } else if (possibleComputer.equals(this.parentTransportBase)) {
                            // open option GUI
                            entityplayer.openGui(DynamicTransportMod.instance,
                                                 GuiLib.GUIID_FLOOR_MARKER,
                                                 this.worldObj,
                                                 this.pos.getX(),
                                                 this.pos.getY(),
                                                 this.pos.getZ());
                            return true;
                        }
                    }
                }
            }
        }
        if (!this.isInMaintenanceMode()) {
            // show floor selection
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

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        if (parentTransportBase != null) {
            nbttagcompound.setInteger("ParentTransportComputerX",
                                      parentTransportBase.getX());
            nbttagcompound.setInteger("ParentTransportComputerY",
                                      parentTransportBase.getY());
            nbttagcompound.setInteger("ParentTransportComputerZ",
                                      parentTransportBase.getZ());
        }
        if (this.floorName != null && !this.floorName.isEmpty()) nbttagcompound.setString("FloorName",
                                                                                          floorName);
        nbttagcompound.setInteger("yOffset",
                                  yOffset);
        nbttagcompound.setBoolean("Powered",
                                  this.Powered);

    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.parentTransportBase = new BlockPos(nbttagcompound.getInteger("ParentTransportComputerX"), nbttagcompound.getInteger("ParentTransportComputerY"), nbttagcompound.getInteger("ParentTransportComputerZ"));

        this.floorName = nbttagcompound.getString("FloorName");

        this.yOffset = nbttagcompound.getInteger("yOffset");
        this.Powered = nbttagcompound.getBoolean("Powered");
    }

    public void removeParent() {
        this.parentTransportBase = null;
        this.updateBlock();
    }

    public void setParentComputer(BlockPos ComputerLocation, EntityPlayer entityplayer) {
        TileEntityElevatorComputer comTile = getParentElevatorComputer();
        IBlockState state = this.worldObj.getBlockState(ComputerLocation);
        if (state.getBlock() == ConfigurationLib.blockTransportBase
            && Block.getStateId(state) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {

            comTile = (TileEntityElevatorComputer) this.worldObj.getTileEntity(ComputerLocation);
            if (comTile.addFloorMarker(this.pos,
                                       entityplayer)) {
                this.parentTransportBase = ComputerLocation;
                this.onInventoryChanged();
                this.getWorld().markBlockForUpdate(this.pos);
            }

        } else {
            ItemStack heldItem = entityplayer.getHeldItem();
            NBTTagCompound tags = new NBTTagCompound();
            ChatHelper.addMessageToPlayer(entityplayer,
                                          "slimevoid.DT.dynamicMarker.bindMissingElevator");
            heldItem.setTagCompound(tags);
        }
        this.updateBlock();
    }

    @Override
    public int getExtendedBlockID() {
        return BlockLib.BLOCK_DYNAMIC_MARK_ID;
    }

    public int getFloorY() {
        return this.pos.getY() + this.yOffset;
    }

    public String getFloorName() {
        return this.floorName;
    }

    @Override
    protected boolean isInMaintenanceMode() {
        return this.getParentChunkCoords() == null
               || this.getParentElevatorComputer() == null
               || this.getParentElevatorComputer().isInMaintenanceMode();
    }

    @Override
    public String getInvName() {
        return BlockLib.BLOCK_DYNAMIC_MARK;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
        this.onInventoryChanged();
        this.markDirty();
    }

    public void setFloorY(int floorY) {
        this.yOffset = floorY - this.pos.getY();
        this.onInventoryChanged();
        this.markDirty();
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 2, nbttagcompound);
    }
}
