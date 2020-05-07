package net.slimevoid.dynamictransport.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.slimevoid.dynamictransport.tileentity.CamoTileEntity;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import static net.slimevoid.dynamictransport.core.DynamicTransport.CAMO;
import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_BLOCK;
import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_ENTITY;

public class ElevatorEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final float maxSpeed = 0.2F;
    private static final float minSpeed = 0.016F;
    private static final float accel = 0.01F;
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(ElevatorEntity.class, DataSerializers.BLOCK_POS);

    private int dest;
    private BlockPos controller;
    private boolean slowingDown;
    private String floorName = null;
    private Entity obstructed = null;
    private List<TransportPartEntity> parts = new ArrayList<>();

    public ElevatorEntity(EntityType<? extends ElevatorEntity> p_i50218_1_, World p_i50218_2_) {
        super(p_i50218_1_, p_i50218_2_);
        this.noClip = true;
    }


    public ElevatorEntity(World worldIn,BlockPos controller, List<BlockPos> parts, int elevatorPos, int floorY, String floorName){
        this(ELEVATOR_ENTITY.get(),worldIn);
        dest = floorY;
        this.controller = controller;
        this.floorName = floorName;
        BlockPos center = new BlockPos(controller.getX(),elevatorPos,controller.getZ());
        double x = (double)center.getX() + 0.5D;
        double y = (double)center.getY();
        double z = (double)center.getZ() + 0.5D;
        this.setPosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.setOrigin(center);
        for(BlockPos pos: parts){
            TransportPartEntity part = new TransportPartEntity(this,pos);
            part.setPosition(this.getPosX() + pos.getX(),this.getPosY() + pos.getY(),this.getPosZ() + pos.getZ());
            BlockState t = this.world.getBlockState(new BlockPos(part));
            part.setItems(((CamoTileEntity)this.world.getTileEntity(new BlockPos(part))).getCamoSides());
            this.parts.add(part);
        }
    }

    @Override
    protected void registerData() {
        this.dataManager.register(ORIGIN, BlockPos.ZERO);
    }
    private void setOrigin(BlockPos blockPos) {
        this.dataManager.set(ORIGIN, blockPos);
    }

    public BlockPos getOrigin() {
        return this.dataManager.get(ORIGIN);
    }

    @Override
    public void tick() {
        if (!world.isRemote() && isUnObstructed()) {
            if(firstUpdate){
                for(TransportPartEntity part: parts) {
                    BlockPos pos = new BlockPos(this).add(part.getOffset());
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    world.notifyNeighbors(pos, Blocks.AIR);
                }
            }
            CalcVelocity();
            // check whether at the destination or not
            if ((MathHelper.abs((float) (dest - this.getPosY())) < MathHelper.abs((float)this.getMotion().getY()))) {
                Arrived();

                return;
            }
        }
        MoveParts();
        updateRiders(this.getMotion().getY());

        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.baseTick();
    }

    private void Arrived() {
        this.remove();
        for (TransportPartEntity part : this.parts) {
            BlockPos bPos = new BlockPos(this.getPosX(),dest,this.getPosZ()).add(part.getOffset());
            if (this.world.setBlockState(bPos, ELEVATOR_BLOCK.get().getDefaultState(), 3)) {
                if(!this.world.isRemote){
                    TileEntity e = this.world.getTileEntity(bPos);
                    if(e instanceof CamoTileEntity){
                        ((CamoTileEntity)e).setCamoSides(part.getItems());
                    }
                }
                //disembark
                for (Entity rider : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), PossibleRider)) {
                    double yTarget = bPos.getY() + 1;
                    if((rider.getBoundingBox().minY)< yTarget){
                        rider.move(MoverType.SHULKER, new Vec3d(0,yTarget - rider.getBoundingBox().minY,0));
                    }
                }
            }
        }
        ((ElevatorControllerTileEntitiy)world.getTileEntity(controller)).elevatorArrived(dest);
        world.getServer().getPlayerList().sendToAllNearExcept(null, getPosX(),getPosY(),getPosZ(),4,world.getDimension().getType(),
                new SChatPacket( new TranslationTextComponent("entityElevator.arrive", floorName), ChatType.GAME_INFO));
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        dest = compound.getInt("dest");
        slowingDown = compound.getBoolean("slowingDown");
        for(INBT p: compound.getList("parts",10)){
            TransportPartEntity part = new TransportPartEntity(this,NBTUtil.readBlockPos((CompoundNBT) p));
            BlockPos offset = part.getOffset();
            part.setPosition(this.getPosX() + offset.getX(),this.getPosY() + offset.getY(),this.getPosZ() + offset.getZ());
            ItemStackHelper.loadAllItems((CompoundNBT) p, part.getItems());
            this.parts.add(part);
        }
        if(this.parts.size() == 0){
            this.remove();
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        compound.putInt("dest",dest);
        compound.putBoolean("slowingDown", slowingDown);
        ListNBT parts = new ListNBT();
        for(TransportPartEntity p: this.parts){
            CompoundNBT part = NBTUtil.writeBlockPos(p.getOffset());
            ItemStackHelper.saveAllItems(part, p.getItems());
            parts.add(part);
        }
        compound.put("parts",parts);
    }

    @Override
    @Nonnull
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void MoveParts() {
        ListIterator<TransportPartEntity> parts = this.parts.listIterator();
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
                part.setPosition(part.prevPosX, part.prevPosY, part.prevPosZ);
            }
            while (parts.hasNext()) { //replay
                Entity part = parts.next();

                //reset velocity
                part.setMotion(0, 0, 0);
                if(part != obstructed )
                //move all parts the same amount as obstructed
                part.move(MoverType.SELF, new Vec3d(0, obstructed.getPosY() - obstructed.prevPosY, 0));
            }
        }else{
            this.move(MoverType.SELF, this.getMotion());
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


    //====================================================================================================================================================
    //These stubs may unlock the ability to have the elevator have a single renderer and multiple invisible hitboxes just like how the ender dragon works
    //====================================================================================================================================================
    @Override
    public void onAddedToWorld() {
        //add entities to entityids
        if(!world.isRemote())
        {
            for(Entity e : this.parts) {
                ((ServerWorld)world).entitiesById.put(e.getEntityId(), e);
            }
        }
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        if (!world.isRemote()) {
            //remove parts since we don't track capabilities on them we can just scrap them.
            for (Entity e : this.parts) {
                e.remove(false);
            }
        }
        super.onRemovedFromWorld();
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(this.parts.size());
        for(TransportPartEntity part: this.parts){
            buffer.writeBlockPos(part.getOffset());
            for(BlockState i:getBlockStates(part.getItems())){
                buffer.writeInt(Block.getStateId(i));
            }
        }
    }

    private Iterable<? extends BlockState> getBlockStates(NonNullList<ItemStack> camoSides) {
        return camoSides.stream().map( s -> s.isEmpty()?
                ELEVATOR_BLOCK.get().getDefaultState().with(CAMO, false):
                ((BlockItem)s.getItem()).getBlock().getDefaultState())::iterator;
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        //first read length as int
        int j = additionalData.readInt();
        for(int i=0; i< j;i++){
            TransportPartEntity part = new TransportPartEntity(this,additionalData.readBlockPos());
            part.setEntityId(i + this.getEntityId());
            BlockPos offset = part.getOffset();
            part.setPosition(this.getPosX() + offset.getX(),this.getPosY() + offset.getY(),this.getPosZ() + offset.getZ());
            this.parts.add(part);

            ArrayList<BlockState> clientCamo = new ArrayList<>();
            for(int k = 0; k<6;k++) {
                clientCamo.add(Block.getStateById(additionalData.readInt()));
            }
            part.setClientCamo(clientCamo);
        }
    }


    private static final Predicate<Entity> PossibleRider = (entity) ->
            !(entity instanceof TransportPartEntity) && !entity.isPassenger() && (!(entity instanceof PlayerEntity) || !entity.isSpectator());

    private void updateRiders(double velocity) {
        for(Entity part: this.parts) {
            for (Entity rider : this.world.getEntitiesInAABBexcluding(this, part.getBoundingBox().expand(0, 0, 0), PossibleRider)) {
                double yPos = this.getBoundingBox().maxY - rider.getBoundingBox().minY;
                Vec3d riderMotion = rider.getMotion();
                rider.setMotion(riderMotion.getX(), velocity < 0 ? velocity : Math.max(yPos, riderMotion.getY()), riderMotion.getZ());
                rider.isAirBorne = false;
                rider.onGround = true;
                rider.fallDistance = 0;
            }
        }
    }

    public Iterable<? extends TransportPartEntity> getParts() {
        return this.parts;
    }
}
