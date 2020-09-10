package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.entity.EntityElevator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class TileEntityElevatorController extends TileEntity {
    private static final TextComponentTranslation PART_INVALID = new TextComponentTranslation("elevatorController.bind.part.invalid");
    private static final TextComponentTranslation ELEVATOR_SUCCESS = new TextComponentTranslation("elevatorController.bind.elevator.success");
    private static final TextComponentTranslation ELEVATOR_ALREADY = new TextComponentTranslation("elevatorController.bind.elevator.alreadyBound");
    private static final TextComponentTranslation ELEVATOR_ANOTHER = new TextComponentTranslation("elevatorController.bind.elevator.boundToOther");
    private static final TextComponentTranslation ELEVATOR_TRANSIT = new TextComponentTranslation("elevatorController.bind.elevator.inTransit");
    private static final TextComponentTranslation MARKER_SUCCESS = new TextComponentTranslation("elevatorController.bind.marker.success");
    private static final TextComponentTranslation MARKER_ALREADY = new TextComponentTranslation("elevatorController.bind.marker.alreadyBound");
    private static final TextComponentTranslation MARKER_ANOTHER = new TextComponentTranslation("elevatorController.bind.marker.boundToOther");

    private final List<BlockPos> boundMarkerBlocks   = new ArrayList<>();
    //Elevator blocks that are bound to this controller with Y set to the offset from current floor when bound
    private final List<BlockPos> boundElevatorBlocks = new ArrayList<>();
    //current position of the elevator not present when in transit
    private OptionalInt elevatorPos = OptionalInt.empty();
    //list of floors in order of request
    private final LinkedHashMap<Integer, String> floorSpool = new LinkedHashMap<>();
    private int rest = 0;

    @Nonnull
    public List<BlockPos> getBoundMarkerBlocks() {
        return boundMarkerBlocks;
    }

    public ITextComponent addPart(BlockPos part){
        TileEntityTransportPart partTile = (TileEntityTransportPart) getWorld().getTileEntity(part);
        if(partTile != null) {
            if (this.getWorld().getBlockState(part).getBlock() == ModBlocks.getElevator()) {
                return addElevator(partTile);
            } else if (this.getWorld().getBlockState(part).getBlock() == ModBlocks.getMarker()) {
                return addMarker(partTile);
            }
        }
        return PART_INVALID;
    }
    private ITextComponent addElevator(@Nonnull TileEntityTransportPart partTile) {
        if (!elevatorPos.isPresent()) {
            return ELEVATOR_TRANSIT;
        }

        if (partTile.getController() != null && !partTile.getController().equals(getPos())) {
            return ELEVATOR_ANOTHER;
        }

        BlockPos offsetPos = partTile.getPos().subtract(new BlockPos(this.getPos().getX(), this.elevatorPos.getAsInt(), this.getPos().getZ()));
        if (boundElevatorBlocks.contains(offsetPos)) {
            partTile.setComputer(getPos());
            return ELEVATOR_ALREADY;
        }
        if (notInRange(partTile.getPos(), true)) return new TextComponentTranslation("elevatorController.bind.elevator.outOfRange", 2);

        boundElevatorBlocks.add(offsetPos);
        partTile.setComputer(getPos());
        markDirty();
        return ELEVATOR_SUCCESS;
    }

    private ITextComponent addMarker(TileEntityTransportPart partTile) {
        if(partTile.getController() != null && !partTile.getController().equals(getPos())){
            return MARKER_ANOTHER;
        }
        if (boundMarkerBlocks.contains(partTile.getPos())) {
            partTile.setComputer(getPos());
            return MARKER_ALREADY;
        }
        if (notInRange(partTile.getPos(), false)) return new TextComponentTranslation("elevatorController.bind.marker.outOfRange", 2);

        boundMarkerBlocks.add(partTile.getPos());
        partTile.setComputer(getPos());
        markDirty();
        return MARKER_SUCCESS;
    }

    private boolean notInRange(BlockPos bindingBlock, boolean shouldCheckComputer) {
        Stream<BlockPos> considered = boundElevatorBlocks.stream().map(pos -> pos.add(this.getPos().getX(),this.elevatorPos.getAsInt(),this.getPos().getZ()));
        if(shouldCheckComputer)
            considered = Stream.concat(considered, Stream.of(this.pos));
        return considered.noneMatch((pos) ->
                MathHelper.sqrt(Math.pow((double) pos.getX() - (double) bindingBlock.getX(), 2)
                        + Math.pow((double) pos.getZ() - (double) bindingBlock.getZ(), 2)
                ) <= 2);
    }

    public boolean callElevator(int i, String floorName) {
        if (elevatorPos.isPresent()) {
            if (i != this.elevatorPos.getAsInt() && !boundElevatorBlocks.isEmpty()) {
                this.floorSpool.put(i, floorName);
            } else {
                return false;
            }
            this.doCallElevator(i, floorName);
         } else {
            if(!this.floorSpool.containsKey(i))
                this.floorSpool.put(i,floorName);
        }
        return true;
    }

    public Tuple<Integer,String> elevatorArrived(int destination) {
        this.floorSpool.remove(destination);
        if (!this.floorSpool.isEmpty()) {
            Map.Entry<Integer,String> next = this.floorSpool.entrySet().iterator().next();
            return new Tuple<>(next.getKey(),next.getValue());
        }else{
            this.elevatorPos = OptionalInt.of(destination);
            return null;
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if(elevatorPos.isPresent())
            compound.setInteger("elevatorPos", elevatorPos.getAsInt());
        compound.setInteger("rest",rest);

        if(!boundElevatorBlocks.isEmpty()) {
            NBTTagList elevators = new NBTTagList();
            for (BlockPos e : boundElevatorBlocks) {
                elevators.appendTag(NBTUtil.createPosTag(e));
            }
            compound.setTag("elevators",elevators);
        }
        if(!boundMarkerBlocks.isEmpty()) {
            NBTTagList markers = new NBTTagList();
            for (BlockPos e : boundMarkerBlocks) {
                markers.appendTag(NBTUtil.createPosTag(e));
            }
            compound.setTag("markers",markers);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("elevatorPos"))
            elevatorPos = OptionalInt.of(compound.getInteger("elevatorPos"));
        for(NBTBase item : compound.getTagList("elevators",10)){
            boundElevatorBlocks.add(NBTUtil.getPosFromTag((NBTTagCompound) item));
        }
        for(NBTBase item : compound.getTagList("markers",10)){
            boundMarkerBlocks.add(NBTUtil.getPosFromTag((NBTTagCompound) item));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    private void doCallElevator(int floorY, String floorName) {
        EntityElevator e = new EntityElevator(
                getWorld(),
                this.pos,
                boundElevatorBlocks,
                elevatorPos.getAsInt(),
                floorY,
                floorName);
        getWorld().spawnEntity(e);
        this.elevatorPos = OptionalInt.empty();
    }

    public void setElevatorPos(int y) {
        this.elevatorPos = OptionalInt.of(y);
    }

    public void SetboundMarkerBlocks(List<BlockPos> floors) {
        this.boundMarkerBlocks.clear();
        this.boundMarkerBlocks.addAll(floors);
    }
}
