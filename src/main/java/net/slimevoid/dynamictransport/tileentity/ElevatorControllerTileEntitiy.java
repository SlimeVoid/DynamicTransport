package net.slimevoid.dynamictransport.tileentity;

import com.google.common.collect.Streams;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.RegistryHandler;
import net.slimevoid.dynamictransport.entities.ElevatorEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static net.slimevoid.dynamictransport.core.RegistryHandler.*;

public class ElevatorControllerTileEntitiy extends TileEntity {
    private static TranslationTextComponent PART_INVALID = new TranslationTextComponent("elevatorController.bind.part.invalid");
    private static TranslationTextComponent ELEVATOR_SUCCESS = new TranslationTextComponent("elevatorController.bind.elevator.success");
    private static TranslationTextComponent ELEVATOR_ALREADY = new TranslationTextComponent("elevatorController.bind.elevator.alreadyBound");
    private static TranslationTextComponent ELEVATOR_ANOTHER = new TranslationTextComponent("elevatorController.bind.elevator.boundToOther");
    private static TranslationTextComponent ELEVATOR_TRANSIT = new TranslationTextComponent("elevatorController.bind.elevator.inTransit");
    private static TranslationTextComponent MARKER_SUCCESS = new TranslationTextComponent("elevatorController.bind.marker.success");
    private static TranslationTextComponent MARKER_ALREADY = new TranslationTextComponent("elevatorController.bind.marker.alreadyBound");
    private static TranslationTextComponent MARKER_ANOTHER = new TranslationTextComponent("elevatorController.bind.marker.boundToOther");

    private List<BlockPos> boundMarkerBlocks   = new ArrayList<>();
    //Elevator blocks that are bound to this controller with Y set to the offset from current floor when bound
    private List<BlockPos> boundElevatorBlocks = new ArrayList<>();
    //current position of the elevator not present when in transit
    private OptionalInt elevatorPos = OptionalInt.empty();
    //list of floors in order of request
    private LinkedHashMap<Integer, String> floorSpool = new LinkedHashMap<>();
    //link to entity when in transit
    //private int elevatorEntity;

    public ElevatorControllerTileEntitiy() {
        super(RegistryHandler.ELEVATOR_CONTROLLER_TILE_ENTITIY.get());
    }

    @Override
    public void setWorldAndPos(World p_226984_1_, BlockPos p_226984_2_) {
        super.setWorldAndPos(p_226984_1_, p_226984_2_);
        this.elevatorPos = OptionalInt.of(this.pos.getY() + 1);
    }

    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        //when pos is set the elevator is right above the controller
        this.elevatorPos = OptionalInt.of(this.getPos().getY() + 1);
    }

    public void addPart(BlockPos part, PlayerEntity entityplayer){
        if (this.getWorld().getBlockState(part).getBlock() == ELEVATOR_BLOCK.get()) {
            addElevator(part,entityplayer);
        }else if(this.getWorld().getBlockState(part).getBlock() == MARKER_BLOCK.get()){
            addMarker(part,entityplayer);
        }else{
            entityplayer.sendStatusMessage(PART_INVALID, true);
        }

    }
    private void addElevator(BlockPos part, PlayerEntity entityplayer) {
        if (!elevatorPos.isPresent()) {
            entityplayer.sendStatusMessage(ELEVATOR_TRANSIT, true);
            return;
        }
        TransportPartTileEntity partTile = (TransportPartTileEntity)world.getTileEntity(part);
        if(partTile.getController() != null && !partTile.getController().equals(getPos())){
            entityplayer.sendStatusMessage(ELEVATOR_ANOTHER, true);
            return;
        }
        BlockPos offsetPos = part.down(this.elevatorPos.getAsInt());
        if (boundElevatorBlocks.contains(offsetPos)) {
            entityplayer.sendStatusMessage(ELEVATOR_ALREADY, true);
            partTile.setComputer(getPos());
            return;
        }
        if (notInRange(part, true)) {
            entityplayer.sendStatusMessage(new TranslationTextComponent("elevatorController.bind.elevator.outOfRange", 2), true);
            return;
        }
        boundElevatorBlocks.add(offsetPos);
        partTile.setComputer(getPos());
        entityplayer.sendStatusMessage(ELEVATOR_SUCCESS, true);
        markDirty();
    }

    private void addMarker(BlockPos marker, PlayerEntity entityplayer) {
        TransportPartTileEntity partTile = (TransportPartTileEntity)world.getTileEntity(marker);
        if(partTile.getController() != null && !partTile.getController().equals(getPos())){
            entityplayer.sendStatusMessage(MARKER_ANOTHER, true);
            return;
        }
        if (boundMarkerBlocks.contains(marker)) {
            entityplayer.sendStatusMessage(MARKER_ALREADY, true);
            partTile.setComputer(getPos());
            return;
        }
        if (notInRange(marker, false)) {
            entityplayer.sendStatusMessage(new TranslationTextComponent("elevatorController.bind.marker.outOfRange", 2), true);
            return;
        }
        boundMarkerBlocks.add(marker);
        partTile.setComputer(getPos());
        entityplayer.sendStatusMessage(MARKER_SUCCESS, true);
        markDirty();
    }

    private boolean notInRange(BlockPos bindingBlock, boolean shouldCheckComputer) {
        Stream<BlockPos> considered = boundElevatorBlocks.stream();
        if(shouldCheckComputer)
            considered = Stream.concat(considered, Stream.of(this.pos));
        return considered.noneMatch((pos) ->
                MathHelper.sqrt(Math.pow((double) pos.getX() - (double) bindingBlock.getX(), 2)
                        + Math.pow((double) pos.getZ() - (double) bindingBlock.getZ(), 2)
                ) <= 2);
    }

    private void callElevator(int i, String floorName) {
        if (elevatorPos.isPresent()) {
            if (i != this.elevatorPos.getAsInt()) {
                this.floorSpool.put(i, floorName);
            } else {
                return;
            }
            this.doCallElevator(i, floorName);
        }else {
            this.floorSpool.put(i,floorName);
        }
    }

    public void elevatorArrived(int destination) {
        this.elevatorPos = OptionalInt.of(destination);
        this.floorSpool.remove(destination);
        Integer nextFloor = this.floorSpool.keySet().iterator().next();
        doCallElevator(nextFloor, this.floorSpool.get(nextFloor));
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        if(elevatorPos.isPresent())
            compound.putInt("elevatorPos", elevatorPos.getAsInt());

        if(!boundElevatorBlocks.isEmpty()) {
            ListNBT elevators = new ListNBT();
            for (BlockPos e : boundElevatorBlocks) {
                elevators.add(NBTUtil.writeBlockPos(e));
            }
            compound.put("elevators",elevators);
        }
        if(!boundMarkerBlocks.isEmpty()) {
            ListNBT markers = new ListNBT();
            for (BlockPos e : boundMarkerBlocks) {
                markers.add(NBTUtil.writeBlockPos(e));
            }
            compound.put("markers",markers);
        }
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        if (compound.contains("elevatorPos"))
            elevatorPos = OptionalInt.of(compound.getInt("elevatorPos"));
        for(INBT item : compound.getList("elevators",10)){
            boundElevatorBlocks.add(NBTUtil.readBlockPos((CompoundNBT) item));
        }
        for(INBT item : compound.getList("markers",10)){
            boundMarkerBlocks.add(NBTUtil.readBlockPos((CompoundNBT) item));
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        return new SUpdateTileEntityPacket(getPos(), 1, write(nbtTag));
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        write(updateTag);
        return updateTag;
    }

    private void doCallElevator(int floorY, String floorName) {
        ElevatorEntity e = new ElevatorEntity(getWorld(), boundElevatorBlocks, elevatorPos.getAsInt(), floorY, floorName);
        getWorld().addEntity(e);
        this.elevatorPos = OptionalInt.empty();
    }
    @Nonnull
    public Stream<BlockPos> getParts() {
        return Streams.concat(boundElevatorBlocks.stream().filter(p-> elevatorPos.isPresent()).map(p->p.up(elevatorPos.getAsInt())), boundMarkerBlocks.stream());
    }
}
