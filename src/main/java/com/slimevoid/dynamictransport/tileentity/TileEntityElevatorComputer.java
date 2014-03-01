package com.slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.slimevoid.dynamictransport.core.lib.BlockLib;
import com.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import com.slimevoid.dynamictransport.entities.EntityElevator;
import com.slimevoid.dynamictransport.util.XZCoords;
import com.slimevoid.library.blocks.BlockBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

public class TileEntityElevatorComputer extends TileEntityTransportBase {

    public enum ElevatorMode {
        Maintenance,
        Transit,
        Available
    }

    // Persistent Data
    private String                         elevatorName;
    private List<ChunkCoordinates>         boundMarkerBlocks   = new ArrayList<ChunkCoordinates>();
    private List<XZCoords>                 boundElevatorBlocks = new ArrayList<XZCoords>();
    private LinkedHashMap<Integer, String> floorSpool          = new LinkedHashMap<Integer, String>();
    private ElevatorMode                   mode                = ElevatorMode.Available;
    private String                         curTechnicianName;
    private int                            elevatorPos;
    public boolean                         pendingMaintenance  = false;
    private float                          elevatorSpeed       = ConfigurationLib.elevatorMaxSpeed;
    private boolean                        isHaltable;
    private boolean                        mobilePower;

    public boolean addElevator(ChunkCoordinates elevator, EntityPlayer entityplayer) {
        if (this.mode == ElevatorMode.Maintenance
            && this.curTechnicianName != null
            && this.curTechnicianName.equals(entityplayer.username)) {
            if (elevator.posY - 1 == this.yCoord) {
                if (!boundElevatorBlocks.contains(new XZCoords(elevator.posX, elevator.posZ))) {

                    if (isInRange(elevator,
                                  true)) {
                        if (this.worldObj.getBlockId(elevator.posX,
                                                     elevator.posY,
                                                     elevator.posZ) == ConfigurationLib.blockTransportBase.blockID
                            && this.worldObj.getBlockMetadata(elevator.posX,
                                                              elevator.posY,
                                                              elevator.posZ) == BlockLib.BLOCK_ELEVATOR_ID) {
                            this.boundElevatorBlocks.add(new XZCoords(elevator.posX, elevator.posZ));
                            entityplayer.sendChatToPlayer(this.elevatorName != null
                                                          && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorSuccessWithName",
                                                                                                                                                        this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.bindElevatorSuccess"));
                            this.updateBlock();
                            return true;
                        } else {
                            entityplayer.sendChatToPlayer(this.elevatorName != null
                                                          && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindInvalidElevatorWithName",
                                                                                                                                                        this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.bindInvalidElevator"));
                        }

                    } else {

                        entityplayer.sendChatToPlayer(this.elevatorName != null
                                                      && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorOutOfRangeWithName",
                                                                                                                                                    this.elevatorName,
                                                                                                                                                    ConfigurationLib.MaxBindingRange) : ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorOutOfRange",
                                                                                                                                                                                                                                                    ConfigurationLib.MaxBindingRange));

                    }
                } else {
                    entityplayer.sendChatToPlayer(this.elevatorName != null
                                                  && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorAlreadyBoundWithName",
                                                                                                                                                this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.bindElevatorAlreadyBound"));
                    this.updateBlock();
                    return true;
                }
            } else {
                entityplayer.sendChatToPlayer(this.elevatorName != null
                                              && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorElevationInvalidWithName",
                                                                                                                                            this.elevatorName,
                                                                                                                                            this.yCoord + 1) : ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindElevatorElevationInvalid",
                                                                                                                                                                                                                           this.yCoord + 1));

            }
        } else {
            entityplayer.sendChatToPlayer(this.elevatorName != null
                                          && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindNoLongerTechWithName",
                                                                                                                                        this.elevatorName) : ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.bindNoLongerTech",
                                                                                                                                                                                                                         this.xCoord,
                                                                                                                                                                                                                         this.yCoord,
                                                                                                                                                                                                                         this.zCoord));

            ItemStack heldItem = entityplayer.getHeldItem();
            NBTTagCompound tags = new NBTTagCompound();
            heldItem.setTagCompound(tags);
        }

        return false;
    }

    public boolean addFloorMarker(ChunkCoordinates markerBlock, EntityPlayer entityplayer) {
        if (this.mode == ElevatorMode.Maintenance
            && this.curTechnicianName != null
            && this.curTechnicianName.equals(entityplayer.username)) {
            if (!this.boundMarkerBlocks.contains(markerBlock)) {
                if (boundElevatorBlocks.size() != 0) {
                    if (isInRange(markerBlock,
                                  false)) {
                        if (this.worldObj.getBlockId(markerBlock.posX,
                                                     markerBlock.posY,
                                                     markerBlock.posZ) == ConfigurationLib.blockTransportBase.blockID
                            && this.worldObj.getBlockMetadata(markerBlock.posX,
                                                              markerBlock.posY,
                                                              markerBlock.posZ) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
                            this.boundMarkerBlocks.add(markerBlock);
                            entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                                             && !this.elevatorName.isEmpty() ? String.format("Block Succesfully Bound to Elevator: %0$s.",
                                                                                                                                             this.elevatorName) : "Block Succesfully Bound to Elevator"));
                            this.updateBlock();
                            return true;
                        } else {
                            entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                                             && !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Does Not Seem To Be a Floor Marker",
                                                                                                                                             this.elevatorName) : "Block Can Not be Bound to Elevator. Block Does Not Seem To Be a Floor Marker"));
                        }
                    } else {
                        entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                                         && !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set Withing %1$s Meters of an Elevator Block",
                                                                                                                                         this.elevatorName,
                                                                                                                                         ConfigurationLib.MaxBindingRange) : String.format("Block Can Not be Bound to Elevator. Block Must be set Withing %0$s Meters of an Elevator Block",
                                                                                                                                                                                           ConfigurationLib.MaxBindingRange)));
                    }
                } else {
                    entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                                     && !this.elevatorName.isEmpty() ? "Block Can Not be Bound to Elevator: %0$s. Must Bind at Least One Elevator Block" : "Block Can Not be Bound to Elevator. Must Bind at Least One Elevator Block"));
                }
            } else {
                entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                                 && !this.elevatorName.isEmpty() ? String.format("Block Already Bound to Elevator: %0$s",
                                                                                                                                 this.elevatorName) : "Block Already Bound to Elevator"));
                this.updateBlock();
                return true;
            }
        } else {
            entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
                                                                             && !this.elevatorName.isEmpty() ? String.format("You are no longer the Technition for the Elevator %0$s",
                                                                                                                             this.elevatorName) : String.format("You are no longer the Technition for the Elevator at %0$s, %1$s ,%2$s",
                                                                                                                                                                this.xCoord,
                                                                                                                                                                this.yCoord,
                                                                                                                                                                this.zCoord)));
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(EntityPlayer entityplayer) {
        if (this.worldObj.isRemote) {
            return true;
        }
        ItemStack heldItem = entityplayer.getHeldItem();
        if (heldItem != null
            && heldItem.itemID == ConfigurationLib.itemElevatorTool.itemID) {
            if (entityplayer.isSneaking()) {
                NBTTagCompound tags = new NBTTagCompound();
                if (this.curTechnicianName != null
                    && this.curTechnicianName.equals(entityplayer.username)) {
                    this.curTechnicianName = "";
                    this.mode = ElevatorMode.Available;
                    entityplayer.sendChatToPlayer(this.elevatorName != null
                                                  && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.mantCompleteWithName",
                                                                                                                                                this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.mantComplete"));
                } else if (this.curTechnicianName == null
                           || this.curTechnicianName.isEmpty()) {
                    // TODO:Ensure if the tools is bound to another elevator
                    // that we either inform the previous elevator that
                    // Maintenance is over or keep the tool from binding
                    tags.setInteger("ComputerX",
                                    this.xCoord);
                    tags.setInteger("ComputerY",
                                    this.yCoord);
                    tags.setInteger("ComputerZ",
                                    this.zCoord);
                    this.curTechnicianName = entityplayer.username;
                    entityplayer.sendChatToPlayer(this.elevatorName != null
                                                  && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.enterMantWithName",
                                                                                                                                                this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.enterMant"));
                    // Move Elevator for Maintenance
                    if (this.boundElevatorBlocks.size() == 0) {
                        this.floorSpool.clear();
                        this.elevatorPos = this.yCoord + 1;
                        this.mode = ElevatorMode.Maintenance;
                    } else if (this.elevatorPos > this.yCoord + 1) {
                        this.callElevator(this.yCoord + 1,
                                          true,
                                          "");
                    } else {
                        this.floorSpool.clear();
                        this.mode = ElevatorMode.Maintenance;
                    }

                }
                heldItem.setTagCompound(tags);
                this.updateBlock();
            } else {
                // open GUI
            }
        }
        return false;
    }

    @Override
    public boolean removeBlockByPlayer(EntityPlayer player, BlockBase blockBase) {
        for (XZCoords boundElevator : this.boundElevatorBlocks) {
            TileEntity eleTile = this.worldObj.getBlockTileEntity(boundElevator.x,
                                                                  this.elevatorPos,
                                                                  boundElevator.z);
            if (eleTile != null) {
                ((TileEntityElevator) eleTile).RemoveComputer(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
            }
        }
        for (ChunkCoordinates boundFloorMarker : this.boundMarkerBlocks) {
            TileEntity markerTile = this.worldObj.getBlockTileEntity(boundFloorMarker.posX,
                                                                     boundFloorMarker.posY,
                                                                     boundFloorMarker.posZ);
            if (markerTile != null) {
                ((TileEntityFloorMarker) markerTile).removeParent();
            }
        }
        return super.removeBlockByPlayer(player,
                                         blockBase);
    }

    public String callElevator(int i, String Floorname) {
        return this.callElevator(i,
                                 false,
                                 Floorname);
    }

    private String callElevator(int i, boolean forMaintenance, String floorname) {

        if (this.mode == ElevatorMode.Available) {
            if (forMaintenance) {
                this.floorSpool.clear();
                this.pendingMaintenance = true;
                sendMessageFromAllFloors(this.elevatorName != null
                                         && !this.elevatorName.isEmpty() ? ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.elevatorcomputer.enterMantWithName",
                                                                                                                                       this.elevatorName) : ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.enterMant"));// "Elevator Going into Mantinance Mode"

            } else {
                if (i != this.elevatorPos) {
                    this.floorSpool.put(i,
                                        floorname);
                } else {
                    return "Elevator Already At Floor "
                           + (floorname == null || floorname.trim().isEmpty() ? i : floorname);
                }
            }
            this.doCallElevator(i,
                                floorname);
            return "Elevator Called to Floor "
                   + (floorname == null || floorname.trim().isEmpty() ? i : floorname);

        } else if (this.mode == ElevatorMode.Maintenance) {
            if (forMaintenance) {
                sendMessageFromAllFloors(ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.alreadyMant"));
            }
            return "Elevator in Maintenance Mode please Try Again Later";
        } else if (this.mode == ElevatorMode.Transit) {
            if (forMaintenance) {
                this.pendingMaintenance = true;
                sendMessageFromAllFloors(ChatMessageComponent.createFromTranslationKey("slimevoid.DT.elevatorcomputer.mantQueued"));
                return "Maintenance Mode Request Queued";
            } else {
                this.floorSpool.put(i,
                                    floorname);
                return "Elevator Called to Floor "
                       + (floorname == null || floorname.trim().isEmpty() ? i : floorname);
            }

        }
        return "WTF you should never see me";

    }

    private void sendMessageFromAllFloors(ChatMessageComponent chatMessageComponent) {
        for (ChunkCoordinates marker : this.boundMarkerBlocks) {
            if (!this.worldObj.isRemote) MinecraftServer.getServer().getConfigurationManager().sendToAllNear(marker.posX,
                                                                                                             marker.posY,
                                                                                                             marker.posZ,
                                                                                                             4,
                                                                                                             this.worldObj.provider.dimensionId,
                                                                                                             new Packet3Chat(chatMessageComponent));
        }
        if (!this.worldObj.isRemote) MinecraftServer.getServer().getConfigurationManager().sendToAllNear(this.xCoord,
                                                                                                         this.yCoord,
                                                                                                         this.zCoord,
                                                                                                         4,
                                                                                                         this.worldObj.provider.dimensionId,
                                                                                                         new Packet3Chat(chatMessageComponent));

    }

    private void doCallElevator(int i, String floorname) {
        // call elevator now
        int centerElevator = -1;
        List<XZCoords> invalidElevators = new ArrayList<XZCoords>();
        boolean first = true;
        this.mode = ElevatorMode.Transit;
        for (XZCoords pos : this.boundElevatorBlocks) {
            if (validElevatorBlock(pos.x,
                                   this.elevatorPos,
                                   pos.z)) {

                EntityElevator curElevator = new EntityElevator(worldObj, pos.x, this.elevatorPos, pos.z);
                if (first) centerElevator = curElevator.entityId;
                curElevator.setProperties(i,
                                          floorname,
                                          this.elevatorSpeed,
                                          new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord + 0),
                                          this.isHaltable,
                                          centerElevator,
                                          this.mobilePower);
                if (first) first = false; // isClient;
                worldObj.spawnEntityInWorld(curElevator);
            } else {
                invalidElevators.add(pos);
            }
        }
        this.boundElevatorBlocks.removeAll(invalidElevators);

    }

    private boolean validElevatorBlock(int x, int y, int z) {
        TileEntity tile = this.worldObj.getBlockTileEntity(x,
                                                           y,
                                                           z + 0);
        if (tile != null && tile instanceof TileEntityElevator) {
            if (((TileEntityElevator) tile).getParentElevatorComputer() != null) {
                ChunkCoordinates thisCoords = new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord);
                return ((TileEntityElevator) tile).getParent().equals(thisCoords);

            }
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        // convert lists into arrays
        int BoundMarkerBlocksX[] = new int[boundMarkerBlocks.size()];
        int BoundMarkerBlocksY[] = new int[boundMarkerBlocks.size()];
        int BoundMarkerBlocksZ[] = new int[boundMarkerBlocks.size()];
        for (int i = 0; i < boundMarkerBlocks.size(); i++) {
            BoundMarkerBlocksX[i] = boundMarkerBlocks.get(i).posX;
            BoundMarkerBlocksY[i] = boundMarkerBlocks.get(i).posY;
            BoundMarkerBlocksZ[i] = boundMarkerBlocks.get(i).posZ;
        }
        int BoundElevatorBlocksX[] = new int[boundElevatorBlocks.size()];
        int BoundElevatorBlocksZ[] = new int[boundElevatorBlocks.size()];
        for (int i = 0; i < boundElevatorBlocks.size(); i++) {
            BoundElevatorBlocksX[i] = boundElevatorBlocks.get(i).x;
            BoundElevatorBlocksZ[i] = boundElevatorBlocks.get(i).z;
        }

        if (elevatorName != null && !elevatorName.isEmpty()) nbttagcompound.setString("ElevatorName",
                                                                                      elevatorName);
        nbttagcompound.setIntArray("BoundMarkerBlocksX",
                                   BoundMarkerBlocksX);
        nbttagcompound.setIntArray("BoundMarkerBlocksY",
                                   BoundMarkerBlocksY);
        nbttagcompound.setIntArray("BoundMarkerBlocksZ",
                                   BoundMarkerBlocksZ);
        nbttagcompound.setIntArray("BoundElevatorBlocksX",
                                   BoundElevatorBlocksX);
        nbttagcompound.setIntArray("BoundElevatorBlocksZ",
                                   BoundElevatorBlocksZ);

        int index = 0;
        int tempSpool[] = new int[floorSpool.size()];
        for (Entry<Integer, String> floorName : this.floorSpool.entrySet()) {
            if (floorName.getValue() != null && !floorName.getValue().isEmpty()) nbttagcompound.setString("FloorSpoolNames_"
                                                                                                                  + index,
                                                                                                          floorName.getValue());
            tempSpool[index] = floorName.getKey();
        }
        nbttagcompound.setIntArray("FloorSpool",
                                   tempSpool);

        nbttagcompound.setInteger("Mode",
                                  mode.ordinal());
        nbttagcompound.setInteger("ElevPos",
                                  this.elevatorPos);
        if (curTechnicianName != null && !curTechnicianName.isEmpty()) nbttagcompound.setString("CurTechnicianName",
                                                                                                curTechnicianName);

    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        int BoundMarkerBlocksX[] = nbttagcompound.getIntArray("BoundMarkerBlocksX");
        int BoundMarkerBlocksY[] = nbttagcompound.getIntArray("BoundMarkerBlocksY");
        int BoundMarkerBlocksZ[] = nbttagcompound.getIntArray("BoundMarkerBlocksZ");
        int BoundElevatorBlocksX[] = nbttagcompound.getIntArray("BoundElevatorBlocksX");
        int BoundElevatorBlocksZ[] = nbttagcompound.getIntArray("BoundElevatorBlocksZ");
        int tempSpool[] = nbttagcompound.getIntArray("FloorSpool");
        boundMarkerBlocks.clear();
        boundElevatorBlocks.clear();
        this.floorSpool.clear();
        for (int i = 0; i < BoundMarkerBlocksX.length; i++) {
            boundMarkerBlocks.add(new ChunkCoordinates(BoundMarkerBlocksX[i], BoundMarkerBlocksY[i], BoundMarkerBlocksZ[i]));
        }
        for (int i = 0; i < BoundElevatorBlocksX.length; i++) {
            boundElevatorBlocks.add(new XZCoords(BoundElevatorBlocksX[i], BoundElevatorBlocksZ[i]));
        }
        for (int i = 0; i < tempSpool.length; i++) {
            this.floorSpool.put(tempSpool[i],
                                nbttagcompound.getString("FloorSpoolNames_" + i));
        }

        this.mode = ElevatorMode.values()[nbttagcompound.getInteger("Mode")];

        this.elevatorPos = nbttagcompound.getInteger("ElevPos");

        this.curTechnicianName = nbttagcompound.getString("CurTechnicianName");

        elevatorName = nbttagcompound.getString("ElevatorName");

    }

    public String getElevatorName() {
        // TODO Auto-generated method stub
        return this.elevatorName;
    }

    public void RemoveElevatorBlock(XZCoords elevatorPosition) {
        if (this.boundElevatorBlocks.contains(elevatorPosition)) {
            this.boundElevatorBlocks.remove(this.boundElevatorBlocks.indexOf(elevatorPosition));
            List<ChunkCoordinates> invalidBoundMarkerBlocks = new ArrayList<ChunkCoordinates>();
            for (ChunkCoordinates boundMarker : this.boundMarkerBlocks) {
                boolean valid = false;
                for (XZCoords boundElevators : this.boundElevatorBlocks) {
                    if (MathHelper.sqrt_double(Math.pow((double) boundElevators.x
                                                                - (double) boundMarker.posX,
                                                        2)
                                               + Math.pow((double) boundElevators.z
                                                                  - (double) boundMarker.posZ,
                                                          2)) <= ConfigurationLib.MaxBindingRange) {
                        if (this.worldObj.getBlockId(boundMarker.posX,
                                                     boundMarker.posY,
                                                     boundMarker.posZ) == ConfigurationLib.blockTransportBase.blockID
                            && this.worldObj.getBlockMetadata(boundMarker.posX,
                                                              boundMarker.posY,
                                                              boundMarker.posZ) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
                            valid = true;
                        }
                    }
                }
                if (!valid) invalidBoundMarkerBlocks.add(boundMarker);
            }
            for (ChunkCoordinates invalidMarker : invalidBoundMarkerBlocks) {
                if (this.boundMarkerBlocks.contains(invalidMarker)) {
                    this.boundMarkerBlocks.remove(this.boundMarkerBlocks.indexOf(invalidMarker));
                }
            }
            this.updateBlock();
        }

    }

    public void elevatorArrived(int dest, boolean center) {
        this.elevatorPos = dest;
        this.floorSpool.remove(dest);
        if (this.pendingMaintenance) {
            if (this.elevatorPos == (this.yCoord + 1)) {
                this.pendingMaintenance = false;
                this.mode = ElevatorMode.Maintenance;

            } else {
                this.floorSpool.clear();
                doCallElevator(this.yCoord + 1,
                               "");
            }
        } else {
            this.mode = ElevatorMode.Available;
            if (!this.floorSpool.isEmpty()) {
                Integer nextFloor = this.floorSpool.keySet().iterator().next();
                doCallElevator(nextFloor,
                               this.floorSpool.get(nextFloor));
            }
        }

    }

    public int getElevatorPos() {
        // TODO Auto-generated method stub
        return this.elevatorPos;
    }

    public ElevatorMode getElevatorMode() {
        // TODO Auto-generated method stub
        return this.mode;
    }

    @Override
    public int getExtendedBlockID() {
        return BlockLib.BLOCK_ELEVATOR_COMPUTER_ID;
    }

    public boolean isInRange(ChunkCoordinates bindingBlock, boolean shouldCheckComputer) {
        if (boundElevatorBlocks != null && boundElevatorBlocks.size() > 0) {
            for (XZCoords boundBlock : boundElevatorBlocks) {
                if (MathHelper.sqrt_double(Math.pow((double) boundBlock.x
                                                            - (double) bindingBlock.posX,
                                                    2)
                                           + Math.pow((double) boundBlock.z
                                                              - (double) bindingBlock.posZ,
                                                      2)) <= ConfigurationLib.MaxBindingRange) {
                    return true;
                }
            }
        }
        if (shouldCheckComputer) {
            return MathHelper.sqrt_double(Math.pow((double) this.xCoord
                                                           - (double) bindingBlock.posX,
                                                   2)
                                          + Math.pow((double) this.zCoord
                                                             - (double) bindingBlock.posZ,
                                                     2)) <= ConfigurationLib.MaxBindingRange;
        }
        return false;
    }

    public SortedMap<Integer, ArrayList<String>> getFloorList() {
        SortedMap<Integer, ArrayList<String>> floors = new TreeMap<Integer, ArrayList<String>>();
        if (this.boundMarkerBlocks != null && this.boundMarkerBlocks.size() > 0) {
            for (ChunkCoordinates boundBlock : boundMarkerBlocks) {
                TileEntity tile = this.worldObj.getBlockTileEntity(boundBlock.posX,
                                                                   boundBlock.posY,
                                                                   boundBlock.posZ);
                if (tile != null && tile instanceof TileEntityFloorMarker) {
                    int floorY = ((TileEntityFloorMarker) tile).getFloorY();
                    String floorName = ((TileEntityFloorMarker) tile).getFloorName();
                    if (!floors.containsKey(floorY)) {
                        floors.put(floorY,
                                   new ArrayList<String>());
                    }
                    if (floorName != null && floorName.trim() != "") {
                        floors.get(floorY).add(floorName);
                    }
                }
            }
        }

        return floors;
    }

    public void removeMarkerBlock(ChunkCoordinates elevatorPosition) {
        if (this.boundMarkerBlocks.contains(elevatorPosition)) {
            this.boundMarkerBlocks.remove(this.boundMarkerBlocks.indexOf(elevatorPosition));
            this.updateBlock();

        }

    }

    @Override
    protected boolean isInMaintenanceMode() {
        return this.getElevatorMode() == TileEntityElevatorComputer.ElevatorMode.Maintenance;
    }

    @Override
    public String getInvName() {
        return BlockLib.BLOCK_ELEVATOR_COMPUTER;
    }
}
