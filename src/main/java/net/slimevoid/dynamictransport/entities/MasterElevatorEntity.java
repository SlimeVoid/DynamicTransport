package net.slimevoid.dynamictransport.entities;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_ENTITY;

public class MasterElevatorEntity extends ElevatorEntity {
    //may be better to move this to the block controller
    private static final float maxSpeed = 0.2F;
    private static final float minSpeed = 0.016F;
    private static final float accel = 0.01F;

    private int dest;
    private boolean slowingDown;
    //private String floorName = null;
    private Entity obstructed = null;
    public MasterElevatorEntity(EntityType<? extends MasterElevatorEntity> p_i50218_1_, World p_i50218_2_) {
        super(p_i50218_1_, p_i50218_2_);
    }


    public void Initialize(List<BlockPos> parts, int elevatorPos, int floorY, String floorName){
        dest = elevatorPos - parts.get(0).getY() + floorY;
        //this.floorName = floorName;
        super.Initialize(parts.get(0));
        parts.stream().skip(1).forEach((pos) -> {
            ElevatorEntity part = ELEVATOR_ENTITY.get().create(world);
            part.Initialize(pos);
            part.startRiding(this,true);
            world.addEntity(part);
        });
    }

    @Override
    public void tick() {
        if (!world.isRemote() && isUnObstructed()) {
            CalcVelocity();
            // check whether at the destination or not
            if (new BlockPos(this).getY() == dest) { //use velocity to detect
                for (Entity part : getParts()) {
                    if(part instanceof  ElevatorEntity)
                        ((ElevatorEntity)part).arrived();
                }
                return;
            }
        }
        MoveParts();

        super.tick();
        /*for(Entity part: getPassengers()){
            if(part != this){
                part.tick();
            }
        }*/
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        dest = compound.getInt("dest");
        slowingDown = compound.getBoolean("slowingDown");
        super.readAdditional(compound);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("dest",dest);
        compound.putBoolean("slowingDown", slowingDown);
        super.writeAdditional(compound);
    }
    private List<Entity> getParts(){
        return Stream.concat(Stream.of(this), getPassengers().stream()).collect(Collectors.toList());
    }
    private void MoveParts() {
        ListIterator<Entity> parts = getParts().listIterator();
        while(parts.hasNext()){
            Entity part = parts.next();
            part.setMotion(this.getMotion());
            part.move(MoverType.SELF, part.getMotion());
            if (part.collidedVertically) {
                obstructed = part;
                break;
            }
        }
        if(obstructed != null) {
            while (parts.hasPrevious()) { //rewind all movements
                Entity part = parts.previous();
                part.setPosition(part.prevPosX, prevPosY, prevPosZ);
            }
            while (parts.hasNext()) { //replay
                Entity part = parts.next();

                //reset velocity
                part.setMotion(0, 0, 0);
                if(part != obstructed )
                //move all parts the same amount as obstructed
                part.move(MoverType.SELF, new Vec3d(0, obstructed.getPosY() - obstructed.prevPosY, 0));
            }
        }
    }

    private void CalcVelocity() {
        float elevatorSpeed = MathHelper.abs((float) this.getMotion().getY());
        if (this.ticksExisted == 10) {
            this.setMotion(0, getDestinationY() > this.getPosY() ? minSpeed: -minSpeed, 0);
        }
        if (this.ticksExisted >= 15) {
            float currentAccel;
            if (!this.slowingDown
                    && MathHelper.abs(getDestinationY() - (float) getPosY()) >= (((elevatorSpeed * elevatorSpeed) - (minSpeed * minSpeed)) / (2f * accel))) {
                currentAccel = Math.min(maxSpeed - elevatorSpeed, accel);
            } else {
                this.slowingDown = true;
                currentAccel = Math.max(minSpeed - elevatorSpeed, -accel);
            }
            this.setMotion(this.getMotion().add(0, (getDestinationY() > this.getPosY() ? currentAccel : -currentAccel), 0));
        }
    }

    private float getDestinationY() {
        return dest + 0.5f;
    }

    private boolean isUnObstructed() {
        if(obstructed != null){
            Vec3d minVelocity = new Vec3d(0, getDestinationY() > this.getPosY() ? minSpeed: -minSpeed,0);
            obstructed.move(MoverType.SELF, minVelocity);
            if(!obstructed.collidedVertically){
                obstructed.move(MoverType.SELF, minVelocity.inverse());
                obstructed = null;
            }
        }
        return obstructed == null;
    }
    @Override
    public void updatePassenger(Entity passenger) {
        //passenger.move(MoverType.SELF, passenger.getMotion());
        //passenger.setPosition( this.getPosX(), this.getPosY() + this.getMountedYOffset() + passenger.getYOffset(), this.getPosZ());
        //super.updatePassenger(passenger);
    }
}
