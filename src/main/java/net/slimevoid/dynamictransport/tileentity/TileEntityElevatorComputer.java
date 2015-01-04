package net.slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.entities.EntityMasterElevator;
import net.slimevoid.library.blocks.BlockBase;
import net.slimevoid.library.util.helpers.BlockHelper;
import net.slimevoid.library.util.helpers.ChatHelper;

@SuppressWarnings("MalformedFormatString")
public class TileEntityElevatorComputer extends TileEntityTransportBase {

    public enum ElevatorMode {
        Maintenance,
        TransitUp,
        TransitDown,
        Available
    }

    // Persistent Data
    private String                         elevatorName;
    private List<BlockPos>         boundMarkerBlocks   = new ArrayList<BlockPos>();
    private List<BlockPos>         boundElevatorBlocks = new ArrayList<BlockPos>();
    private LinkedHashMap<Integer, String> floorSpool          = new LinkedHashMap<Integer, String>();
    private ElevatorMode                   mode                = ElevatorMode.Available;
    private String                         curTechnicianName;
    private int                            elevatorPos;
    public boolean                         pendingMaintenance  = false;
    private float                          elevatorSpeed       = ConfigurationLib.elevatorMaxSpeed;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean                        isHaltAble = false;

    public boolean addElevator(BlockPos elevator, EntityPlayer entityplayer) {
        if (this.mode == ElevatorMode.Maintenance
            && this.curTechnicianName != null
            && this.curTechnicianName.equals(entityplayer.getGameProfile().getName())) {
            if (!boundElevatorBlocks.contains(elevator.add(0, elevator.getY()
                    - (this.pos.getY() + 1), 0))) {

                if (isInRange(elevator,
                              true)) {
                	IBlockState state = this.worldObj.getBlockState(elevator);
                    if (state.getBlock() == ConfigurationLib.blockTransportBase
                        && Block.getStateId(state) == BlockLib.BLOCK_ELEVATOR_ID) {
                        this.boundElevatorBlocks.add(elevator.add(0, elevator.getY() - (this.pos.getY() + 1), 0));
                        if (this.elevatorName != null
                            && !this.elevatorName.isEmpty()) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorcomputer.bindElevatorSuccessWithName",
                                                          this.elevatorName);
                        } else {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorcomputer.bindElevatorSuccess");
                        }
                        this.updateBlock();
                        return true;
                    } else {
                        if (this.elevatorName != null
                            && !this.elevatorName.isEmpty()) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorcomputer.bindInvalidElevatorWithName",
                                                          this.elevatorName);
                        } else {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          "slimevoid.DT.elevatorcomputer.bindInvalidElevator");
                        }
                    }

                } else {
                    if (this.elevatorName != null
                        && !this.elevatorName.isEmpty()) {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.bindElevatorOutOfRangeWithName",
                                                      this.elevatorName,
                                                      ConfigurationLib.MaxBindingRange);
                    } else {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.bindElevatorOutOfRange",
                                                      ConfigurationLib.MaxBindingRange);
                    }
                }
            } else {
                if (this.elevatorName != null && !this.elevatorName.isEmpty()) {
                    ChatHelper.addMessageToPlayer(entityplayer,
                                                  "slimevoid.DT.elevatorcomputer.bindElevatorAlreadyBoundWithName",
                                                  this.elevatorName);
                } else {
                    ChatHelper.addMessageToPlayer(entityplayer,
                                                  "slimevoid.DT.elevatorcomputer.bindElevatorAlreadyBound");
                }
                this.updateBlock();
                return true;
            }
        } else {
            if (this.elevatorName != null && !this.elevatorName.isEmpty()) {
                ChatHelper.addMessageToPlayer(entityplayer,
                                              "slimevoid.DT.elevatorcomputer.bindNoLongerTechWithName",
                                              this.elevatorName);
            } else {
                ChatHelper.addMessageToPlayer(entityplayer,
                                              "slimevoid.DT.elevatorcomputer.bindNoLongerTech",
                                              this.pos);
            }
            ItemStack heldItem = entityplayer.getHeldItem();
            NBTTagCompound tags = new NBTTagCompound();
            heldItem.setTagCompound(tags);
        }

        return false;
    }

    public boolean addFloorMarker(BlockPos markerBlock, EntityPlayer entityplayer) {
        if (this.mode == ElevatorMode.Maintenance
            && this.curTechnicianName != null
            && this.curTechnicianName.equals(entityplayer.getGameProfile().getName())) {
            if (!this.boundMarkerBlocks.contains(markerBlock)) {
                if (boundElevatorBlocks.size() != 0) {
                    if (isInRange(markerBlock,
                                  false)) {
                    	IBlockState state  = this.worldObj.getBlockState(markerBlock);
                        if (state.getBlock() == ConfigurationLib.blockTransportBase
                            && Block.getStateId(state) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
                            this.boundMarkerBlocks.add(markerBlock);
                            if (this.elevatorName != null
                                && !this.elevatorName.isEmpty()) {
                                ChatHelper.addMessageToPlayer(entityplayer,
                                                              String.format("Block Successfully Bound to Elevator: %0$s.",
                                                                            this.elevatorName));
                            } else {
                                ChatHelper.addMessageToPlayer(entityplayer,
                                                              "Block Successfully Bound to Elevator");
                            }
                            this.updateBlock();
                            return true;
                        } else {
                            if (this.elevatorName != null
                                && !this.elevatorName.isEmpty()) {
                                ChatHelper.addMessageToPlayer(entityplayer,
                                                              String.format("Block Can Not be Bound to Elevator: %0$s. Block Does Not Seem To Be a Floor Marker",
                                                                            this.elevatorName));
                            } else {
                                ChatHelper.addMessageToPlayer(entityplayer,
                                                              "Block Can Not be Bound to Elevator. Block Does Not Seem To Be a Floor Marker");
                            }
                        }
                    } else {
                        if (this.elevatorName != null
                            && !this.elevatorName.isEmpty()) {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set Withing %1$s Meters of an Elevator Block",
                                                                        this.elevatorName,
                                                                        ConfigurationLib.MaxBindingRange));
                        } else {
                            ChatHelper.addMessageToPlayer(entityplayer,
                                                          String.format("Block Can Not be Bound to Elevator. Block Must be set Withing %0$s Meters of an Elevator Block",
                                                                        ConfigurationLib.MaxBindingRange));
                        }
                    }
                } else {
                    if (this.elevatorName != null
                        && !this.elevatorName.isEmpty()) {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "Block Can Not be Bound to Elevator: %0$s. Must Bind at Least One Elevator Block");
                    } else {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "Block Can Not be Bound to Elevator. Must Bind at Least One Elevator Block");
                    }
                }
            } else {
                if (this.elevatorName != null && !this.elevatorName.isEmpty()) {
                    ChatHelper.addMessageToPlayer(entityplayer,
                                                  String.format("Block Already Bound to Elevator: %0$s",
                                                                this.elevatorName));
                } else {
                    ChatHelper.addMessageToPlayer(entityplayer,
                                                  "Block Already Bound to Elevator");
                }
                this.updateBlock();
                return true;
            }
        } else {
            if (this.elevatorName != null && !this.elevatorName.isEmpty()) {
                ChatHelper.addMessageToPlayer(entityplayer,
                                              String.format("You are no longer the Technician for the Elevator %0$s",
                                                            this.elevatorName));
            } else {
                ChatHelper.addMessageToPlayer(entityplayer,
                                              String.format("You are no longer the Technician for the Elevator at %0$s, %1$s ,%2$s",
                                                            this.pos));
            }
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(IBlockState blockState, EntityPlayer entityplayer, EnumFacing side, float xHit, float yHit, float zHit) {
        if (this.worldObj.isRemote) {
            return true;
        }
        ItemStack heldItem = entityplayer.getHeldItem();
        if (heldItem != null
            && heldItem.getItem() == ConfigurationLib.itemElevatorTool) {
            if (entityplayer.isSneaking()) {
                NBTTagCompound tags = new NBTTagCompound();
                if (this.curTechnicianName != null
                    && this.curTechnicianName.equals(entityplayer.getGameProfile().getName())) {
                    this.curTechnicianName = "";
                    this.mode = ElevatorMode.Available;
                    if (this.elevatorName != null
                        && !this.elevatorName.isEmpty()) {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.mantCompleteWithName",
                                                      this.elevatorName);
                    } else {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.mantComplete");
                    }
                } else if (this.curTechnicianName == null
                           || this.curTechnicianName.isEmpty()) {
                    // TODO:Ensure if the tools is bound to another elevator
                    // that we either inform the previous elevator that
                    // Maintenance is over or keep the tool from binding
                    tags.setInteger("ComputerX",
                                    this.pos.getX());
                    tags.setInteger("ComputerY",
                                    this.pos.getY());
                    tags.setInteger("ComputerZ",
                                    this.pos.getZ());
                    this.curTechnicianName = entityplayer.getGameProfile().getName();
                    if (this.elevatorName != null
                        && !this.elevatorName.isEmpty()) {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.enterMantWithName",
                                                      this.elevatorName);
                    } else {
                        ChatHelper.addMessageToPlayer(entityplayer,
                                                      "slimevoid.DT.elevatorcomputer.enterMant");
                    }
                    // Move Elevator for Maintenance
                    if (this.boundElevatorBlocks.size() == 0) {
                        this.floorSpool.clear();
                        this.elevatorPos = this.pos.getY() + 1;
                        this.mode = ElevatorMode.Maintenance;
                    } else if (this.elevatorPos > this.pos.getY() + 1) {
                        this.callElevator(this.pos.getY() + 1,
                                          true,
                                          "");
                    } else {
                        this.floorSpool.clear();
                        this.mode = ElevatorMode.Maintenance;
                    }

                }
                heldItem.setTagCompound(tags);
                this.updateBlock();
            }
        }
        return false;
    }

    @Override
    public boolean removeBlockByPlayer(EntityPlayer player, BlockBase blockBase, boolean willHarvest) {
        for (BlockPos boundElevator : this.boundElevatorBlocks) {
            TileEntityElevator eleTile = (TileEntityElevator) BlockHelper.getTileEntity(this.getWorld(),
            												 							boundElevator.add(0, this.elevatorPos, 0),
							                                                            TileEntityElevator.class);
            if (eleTile != null) {
                 eleTile.RemoveComputer(this.pos);
            }
        }
        for (BlockPos boundFloorMarker : this.boundMarkerBlocks) {
            TileEntityFloorMarker markerTile = (TileEntityFloorMarker) BlockHelper.getTileEntity(this.getWorld(),
            																					 boundFloorMarker,
            																					 TileEntityFloorMarker.class);
            if (markerTile != null) {
                 markerTile.removeParent();
            }
        }
        return super.removeBlockByPlayer(player,
                                         blockBase,
                                         willHarvest);
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
                if (this.elevatorName != null && !this.elevatorName.isEmpty()) {
                    sendMessageFromAllFloors("slimevoid.DT.elevatorcomputer.enterMantWithName",
                                             this.elevatorName);
                } else {
                    this.sendMessageFromAllFloors("slimevoid.DT.elevatorcomputer.enterMant");// "Elevator Going into Mantinance Mode"
                }
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
                sendMessageFromAllFloors("slimevoid.DT.elevatorcomputer.alreadyMant");
            }
            return "Elevator in Maintenance Mode please Try Again Later";
        } else if (this.mode == ElevatorMode.TransitUp || this.mode == ElevatorMode.TransitDown) {
            if (forMaintenance) {
                this.pendingMaintenance = true;
                sendMessageFromAllFloors("slimevoid.DT.elevatorcomputer.mantQueued");
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

    private void sendMessageFromAllFloors(String message, Object... args) {
        for (BlockPos marker : this.boundMarkerBlocks) {
            ChatHelper.sendChatMessageToAllNear(this.getWorld(),
                                                marker.getX(),
                                                marker.getY(),
                                                marker.getZ(),
                                                4,
                                                message,
                                                args);
        }
        if (!this.worldObj.isRemote) {
            ChatHelper.sendChatMessageToAllNear(this.getWorld(),
                                                this.pos.getX(),
                                                this.pos.getY(),
                                                this.pos.getZ(),
                                                4,
                                                message,
                                                args);
        }
    }

    private void doCallElevator(int i, String floorname) {
        this.mode = i > this.elevatorPos ? ElevatorMode.TransitUp : ElevatorMode.TransitDown;


        EntityMasterElevator curElevator = new EntityMasterElevator(this.getWorld(),
        															this.pos.getX(),
        															this.elevatorPos,
        															this.pos.getZ());
        worldObj.spawnEntityInWorld(curElevator);
        curElevator.setProperties(i,
                floorname,
                this.elevatorSpeed,
                this.pos,
                this.isHaltAble,
                this.boundElevatorBlocks
        );


    }

    private boolean validElevatorBlock(BlockPos pos) {
        TileEntity tile = this.worldObj.getTileEntity(pos);
        if (tile != null && tile instanceof TileEntityElevator) {
            if (((TileEntityElevator) tile).getParentElevatorComputer() != null) {
                return ((TileEntityElevator) tile).getParent().equals(this.pos);

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
            BoundMarkerBlocksX[i] = boundMarkerBlocks.get(i).getX();
            BoundMarkerBlocksY[i] = boundMarkerBlocks.get(i).getY();
            BoundMarkerBlocksZ[i] = boundMarkerBlocks.get(i).getZ();
        }
        int BoundElevatorBlocksX[] = new int[boundElevatorBlocks.size()];
        int BoundElevatorBlocksY[] = new int[boundElevatorBlocks.size()];
        int BoundElevatorBlocksZ[] = new int[boundElevatorBlocks.size()];
        for (int i = 0; i < boundElevatorBlocks.size(); i++) {
            BoundElevatorBlocksX[i] = boundElevatorBlocks.get(i).getX();
            BoundElevatorBlocksY[i] = boundElevatorBlocks.get(i).getY();
            BoundElevatorBlocksZ[i] = boundElevatorBlocks.get(i).getZ();
        }

        if (elevatorName != null && !elevatorName.isEmpty()) nbttagcompound.setString("ElevatorName",
                                                                                      this.elevatorName);
        nbttagcompound.setIntArray("BoundMarkerBlocksX",
                                   BoundMarkerBlocksX);
        nbttagcompound.setIntArray("BoundMarkerBlocksY",
                                   BoundMarkerBlocksY);
        nbttagcompound.setIntArray("BoundMarkerBlocksZ",
                                   BoundMarkerBlocksZ);
        nbttagcompound.setIntArray("BoundElevatorBlocksX",
                                   BoundElevatorBlocksX);
        nbttagcompound.setIntArray("BoundElevatorBlocksY",
                BoundElevatorBlocksY);
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
        int BoundElevatorBlocksY[] = nbttagcompound.getIntArray("BoundElevatorBlocksY");
        int BoundElevatorBlocksZ[] = nbttagcompound.getIntArray("BoundElevatorBlocksZ");
        int tempSpool[] = nbttagcompound.getIntArray("FloorSpool");
        boundMarkerBlocks.clear();
        boundElevatorBlocks.clear();
        this.floorSpool.clear();
        for (int i = 0; i < BoundMarkerBlocksX.length; i++) {
            boundMarkerBlocks.add(new BlockPos(BoundMarkerBlocksX[i], BoundMarkerBlocksY[i], BoundMarkerBlocksZ[i]));
        }
        for (int i = 0; i < BoundElevatorBlocksY.length; i++) {
            boundElevatorBlocks.add(new BlockPos(BoundElevatorBlocksX[i], BoundElevatorBlocksY[i], BoundElevatorBlocksZ[i]));
        }
        for (int i = 0; i < tempSpool.length; i++) {
            this.floorSpool.put(tempSpool[i],
                                nbttagcompound.getString("FloorSpoolNames_" + i));
        }

        this.mode = ElevatorMode.values()[nbttagcompound.getInteger("Mode")];

        this.elevatorPos = nbttagcompound.getInteger("ElevPos");

        this.curTechnicianName = nbttagcompound.getString("CurTechnicianName");

        this.elevatorName = nbttagcompound.getString("ElevatorName");

    }

    @SuppressWarnings("UnusedDeclaration")
    public String getElevatorName() {
        return this.elevatorName;
    }

    public void RemoveElevatorBlock(BlockPos elevatorPosition) {
        if (this.boundElevatorBlocks.contains(elevatorPosition)) {
            this.boundElevatorBlocks.remove(this.boundElevatorBlocks.indexOf(elevatorPosition));
            ListIterator<BlockPos> itr = this.boundMarkerBlocks.listIterator();
            while ( itr.hasNext()) {
            	BlockPos boundMarker = itr.next();
                boolean valid = false;
                for (BlockPos boundElevators : this.boundElevatorBlocks) {
                	double powerX = Math.pow((double) boundElevators.getX() - (double) boundMarker.getX(), 2);
                	double powerZ = Math.pow((double) boundElevators.getZ() - (double) boundMarker.getZ(), 2);
                    if (MathHelper.sqrt_double(powerX + powerZ) <= ConfigurationLib.MaxBindingRange) {
                    	IBlockState state = this.worldObj.getBlockState(boundMarker);
                        if (state.getBlock() == ConfigurationLib.blockTransportBase
                            && Block.getStateId(state) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
                            valid = true;
                            break;
                        }
                    }
                }
                if (!valid) {
                	TileEntityFloorMarker tile = (TileEntityFloorMarker) BlockHelper.getTileEntity(this.worldObj,
                																				   boundMarker,
                																				   TileEntityFloorMarker.class);
                	if (tile != null){
                		tile.removeParent();
                	}
                	itr.remove();
                }
            }
           
            this.updateBlock();
        }

    }

    public void elevatorArrived(int destination) {
        this.elevatorPos = destination;
        this.floorSpool.remove(destination);
        if (this.pendingMaintenance) {
            if (this.elevatorPos == (this.pos.getY() + 1)) {
                this.pendingMaintenance = false;
                this.mode = ElevatorMode.Maintenance;

            } else {
                this.floorSpool.clear();
                doCallElevator(this.pos.getY() + 1,
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

    @SuppressWarnings("UnusedDeclaration")
    public int getElevatorPos() {
        return this.elevatorPos;
    }

    public ElevatorMode getElevatorMode() {
        return this.mode;
    }

    @Override
    public int getExtendedBlockID() {
        return BlockLib.BLOCK_ELEVATOR_COMPUTER_ID;
    }

    public boolean isInRange(BlockPos bindingBlock, boolean shouldCheckComputer) {
        if (boundElevatorBlocks != null && boundElevatorBlocks.size() > 0) {
            for (BlockPos boundBlock : boundElevatorBlocks) {
            	double powerX = Math.pow((double) boundBlock.getX() - (double) bindingBlock.getX(), 2);
            	double powerZ = Math.pow((double) boundBlock.getZ() - (double) bindingBlock.getZ(), 2);
                if (MathHelper.sqrt_double(powerX + powerZ) <= ConfigurationLib.MaxBindingRange) {
                    return true;
                }
            }
        }
        double powerX = Math.pow((double) this.pos.getX() - (double) bindingBlock.getX(), 2);
        double powerZ = Math.pow((double) this.pos.getZ() - (double) bindingBlock.getZ(), 2);
        return shouldCheckComputer && MathHelper.sqrt_double(powerX + powerZ) <= ConfigurationLib.MaxBindingRange;
    }

    public SortedMap<Integer, ArrayList<String>> getFloorList() {
        SortedMap<Integer, ArrayList<String>> floors = new TreeMap<Integer, ArrayList<String>>();
        if (this.boundMarkerBlocks != null && this.boundMarkerBlocks.size() > 0) {
            for (BlockPos boundBlock : boundMarkerBlocks) {
                TileEntity tile = this.worldObj.getTileEntity(boundBlock);
                if (tile != null && tile instanceof TileEntityFloorMarker) {
                    int floorY = ((TileEntityFloorMarker) tile).getFloorY();
                    String floorName = ((TileEntityFloorMarker) tile).getFloorName();
                    if (!floors.containsKey(floorY)) {
                        floors.put(floorY,
                                   new ArrayList<String>());
                    }
                    if (floorName != null && !floorName.trim().equals("")) {
                        floors.get(floorY).add(floorName);
                    }
                }
            }
        }

        return floors;
    }

    public void removeMarkerBlock(BlockPos elevatorPosition) {
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
