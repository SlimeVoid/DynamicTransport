package net.slimevoid.dynamictransport.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.slimevoid.dynamictransport.blocks.BaseCamoBlock;
import net.slimevoid.dynamictransport.tileentity.CamoTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraft.state.properties.BlockStateProperties.LEVEL_0_15;
import static net.slimevoid.dynamictransport.core.DynamicTransport.CAMO;
import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_BLOCK;
import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_ENTITY;

public class ElevatorEntity extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(ElevatorEntity.class, DataSerializers.BLOCK_POS);
    private NonNullList<ItemStack> camoSides = NonNullList.withSize(6, ItemStack.EMPTY); //server only
    private NonNullList<BlockState> clientCamo = null; //client only

    public ElevatorEntity(EntityType<? extends ElevatorEntity> p_i50218_1_, World p_i50218_2_) {
        super(p_i50218_1_, p_i50218_2_);
    }

    public void Initialize(BlockPos part){
        double x = (double)part.getX() + 0.5D;
        double y = (double)part.getY();
        double z = (double)part.getZ() + 0.5D;
        this.camoSides = ((CamoTileEntity)this.world.getTileEntity(part)).getCamoSides();
        this.setPosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
        this.setMotion(Vec3d.ZERO);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.setOrigin(new BlockPos(this));
    }

    private void setOrigin(BlockPos blockPos) {
        this.dataManager.set(ORIGIN, blockPos);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(ORIGIN, BlockPos.ZERO);
    }

    @Override
    public void tick() {
        if(firstUpdate){
            BlockPos pos = new BlockPos(this);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            world.notifyNeighbors(pos, Blocks.AIR);
        }
        super.tick();
        updateRiders(this.getMotion().getY());

    }

    public void arrived(){
        BlockPos bPos = new BlockPos(this);
        this.remove();
        if (this.world.setBlockState(bPos, ELEVATOR_BLOCK.get().getDefaultState(), 3)) {
            if(!this.world.isRemote){
                TileEntity e = this.world.getTileEntity(bPos);
                if(e instanceof  CamoTileEntity){
                    ((CamoTileEntity)e).setCamoSides(this.camoSides);
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

    private static final Predicate<Entity> PossibleRider = (entity) ->
        !(entity instanceof ElevatorEntity) && !entity.isPassenger() && (!(entity instanceof PlayerEntity) || !entity.isSpectator());


    private void updateRiders(double velocity) {
        for (Entity rider : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(0,0,0), PossibleRider)) {
            double yPos = this.getBoundingBox().maxY - rider.getBoundingBox().minY;
            Vec3d riderMotion = rider.getMotion();
            rider.setMotion(riderMotion.getX(), velocity < 0 ? velocity : Math.max(yPos,riderMotion.getY()), riderMotion.getZ());
            rider.isAirBorne= false;
            rider.onGround = true;
            rider.fallDistance = 0;
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        ItemStackHelper.saveAllItems(compound, this.camoSides);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        camoSides.replaceAll((a)->ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound,this.camoSides);
    }


    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        for(BlockState state : getBlockStates()){
            buffer.writeInt(Block.getStateId(state));
        }
    }

    @Override
    @Nonnull
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {

        this.clientCamo = NonNullList.withSize(6, ELEVATOR_BLOCK.get().getDefaultState().with(CAMO, false));
        for(int i=0; i< 6;++i){
            this.clientCamo.set(i,Block.getStateById(additionalData.readInt()));
        }
    }

    private List<BlockState> getBlockStates(){
        return camoSides.stream().map( s -> s.isEmpty()?
                ELEVATOR_BLOCK.get().getDefaultState().with(CAMO, false):
                ((BlockItem)s.getItem()).getBlock().getDefaultState()).collect(Collectors.toList());
    }

    public List<BlockState> getClientBlockStates(){
        return this.clientCamo;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.removed;
    }

    @OnlyIn(Dist.CLIENT)
    public World getWorldObj() {
        return this.world;
    }

    public BlockPos getOrigin() {
        return this.dataManager.get(ORIGIN);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getBoundingBox();
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        //noop
    }
    @Override
    public void updateRidden(){
    }

    private void updateLight() {
        BlockPos pos = new BlockPos(this);
        OptionalInt light = Arrays.stream(Direction.values()).mapToInt((side)-> {
            int i = side.getOpposite().getIndex();
            BlockState camo = getBlockStates().get(i);
            if (!(camo.getBlock() instanceof BaseCamoBlock))
                return camo.getLightValue(world, pos);
            return 0;
        }).max();
        int newLight = 0;
        if(light.isPresent())
            newLight = light.getAsInt();

        BlockState state = world.getBlockState(pos);
        if(newLight != state.get(LEVEL_0_15)) {
            //need the extra processing here to update lighting
            world.setBlockState(pos, state.with(LEVEL_0_15, newLight));
        }
    }
}
