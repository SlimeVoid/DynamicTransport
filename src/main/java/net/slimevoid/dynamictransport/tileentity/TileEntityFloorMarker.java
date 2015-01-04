package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.ForgeDirection;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.util.helpers.ChatHelper;

public class TileEntityFloorMarker extends TileEntityTransportBase {

    private ChunkCoordinates parentTransportBase;
    private String           floorName;
    private boolean          Powered = false;
    private int              yOffset = -2;
    public boolean atFloor = false;

    public ChunkCoordinates getParentChunkCoords() {
        return this.parentTransportBase;
    }

    public TileEntityElevatorComputer getParentElevatorComputer() {
        TileEntity tile = parentTransportBase == null ? null : this.worldObj.getTileEntity(this.parentTransportBase.posX,
                                                                                           this.parentTransportBase.posY,
                                                                                           this.parentTransportBase.posZ);
        if (tile == null) {
            parentTransportBase = null;
        } else if (!(tile instanceof TileEntityElevatorComputer)) {
            tile = null;
            parentTransportBase = null;
        }

        return (TileEntityElevatorComputer) tile;
    }

    @Override
    public boolean isSideSolid(BlockBase blockBase, ForgeDirection side) {
        return true;
    }

    @Override
    public void onBlockNeighborChange(Block block) {
        boolean flag = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord,
                                                                     this.yCoord,
                                                                     this.zCoord);

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
            String msg = comTile.callElevator(this.yCoord + this.yOffset,
                                              this.floorName);
            if (!this.worldObj.isRemote) {
                ChatHelper.sendChatMessageToAllNear(this.getWorldObj(),
                                                    this.xCoord,
                                                    this.yCoord,
                                                    this.zCoord,
                                                    4,
                                                    msg);
            }
        }
    }

    @Override
    public boolean onBlockActivated(EntityPlayer entityplayer) {
        if (this.getWorldObj().isRemote) {
            return true;
        }
        ItemStack heldItem = entityplayer.getHeldItem();
        if (heldItem != null
            && heldItem.getItem() == ConfigurationLib.itemElevatorTool) {
            if (heldItem.hasTagCompound()
                && entityplayer.getHeldItem().getTagCompound() != null) {
                NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
                if (tags != null && tags.hasKey("ComputerX")) {
                    ChunkCoordinates possibleComputer = new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
                    if (entityplayer.isSneaking()) {
                        if (possibleComputer.equals(this.parentTransportBase)) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.dynamicMarker.unbound");// "Block Unbound"
                            this.getParentElevatorComputer().removeMarkerBlock(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
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
                                                 this.xCoord,
                                                 this.yCoord,
                                                 this.zCoord);
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
                                 this.xCoord,
                                 this.yCoord,
                                 this.zCoord);
            return true;
        }

        return super.onBlockActivated(entityplayer);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        if (parentTransportBase != null) {
            nbttagcompound.setInteger("ParentTransportComputerX",
                                      parentTransportBase.posX);
            nbttagcompound.setInteger("ParentTransportComputerY",
                                      parentTransportBase.posY);
            nbttagcompound.setInteger("ParentTransportComputerZ",
                                      parentTransportBase.posZ);
        }
        if (this.floorName != null && !this.floorName.isEmpty()) nbttagcompound.setString("FloorName",
                                                                                          floorName);
        nbttagcompound.setInteger("yOffset",
                                  yOffset);
        nbttagcompound.setBoolean("Powered",
                                  this.Powered);
        nbttagcompound.setBoolean("atFloor",
                                  this.atFloor);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.parentTransportBase = new ChunkCoordinates(nbttagcompound.getInteger("ParentTransportComputerX"), nbttagcompound.getInteger("ParentTransportComputerY"), nbttagcompound.getInteger("ParentTransportComputerZ"));

        this.floorName = nbttagcompound.getString("FloorName");

        this.yOffset = nbttagcompound.getInteger("yOffset");
        this.Powered = nbttagcompound.getBoolean("Powered");
        this.atFloor = nbttagcompound.getBoolean("atFloor");
    }

    public void removeParent() {
        this.parentTransportBase = null;
        this.updateBlock();
    }

    public void setParentComputer(ChunkCoordinates ComputerLocation, EntityPlayer entityplayer) {
        TileEntityElevatorComputer comTile = getParentElevatorComputer();

        if (this.worldObj.getBlock(ComputerLocation.posX,
                                   ComputerLocation.posY,
                                   ComputerLocation.posZ) == ConfigurationLib.blockTransportBase
            && this.worldObj.getBlockMetadata(ComputerLocation.posX,
                                              ComputerLocation.posY,
                                              ComputerLocation.posZ) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {

            comTile = (TileEntityElevatorComputer) this.worldObj.getTileEntity(ComputerLocation.posX,
                                                                               ComputerLocation.posY,
                                                                               ComputerLocation.posZ);
            if (comTile.addFloorMarker(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord),
                                       entityplayer)) {
                this.parentTransportBase = ComputerLocation;
                this.onInventoryChanged();
                this.getWorldObj().markBlockForUpdate(this.xCoord,
                                                      this.yCoord,
                                                      this.zCoord);
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
        return this.yCoord + this.yOffset;
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
        this.yOffset = floorY - this.yCoord;
        this.onInventoryChanged();
        this.markDirty();
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 2, nbttagcompound);
    }

    public void setActive(boolean flag) {
        this.atFloor = flag;
    }
}
