package net.slimevoid.dynamictransport.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.library.util.helpers.BlockHelper;
import net.slimevoid.library.util.helpers.EntityHelper;

public class EntityElevatorPart  extends Entity {
    public EntityMasterElevator entityElevatorObj;
    private HashSet<Entity> confirmedRiders;
    private int elevatorYOffset;

    public EntityElevatorPart(World par1World,EntityMasterElevator parent, double x,double y, double z) {
        this(par1World);
        this.prevPosX = x + 0.5F;
        this.prevPosY = y;
        this.prevPosZ = z + 0.5F;
        this.setPosition(prevPosX,
                prevPosY,
                prevPosZ);
        this.entityElevatorObj = parent;

    }

    public EntityElevatorPart(World par1World){
        super(par1World);
        this.confirmedRiders = new HashSet<Entity>();
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.entityCollisionReduction = 1.0F;
        this.ignoreFrustumCheck = true;
        this.setSize(0.5F,
                0.5F);

        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
    }

    @Override
    protected void entityInit() {
        this.getDataWatcher().addObjectByDataType(2,
                5);
        this.getDataWatcher().addObject(3,
                (short)0);
        this.getDataWatcher().addObject(4, (byte)0);
        this.getDataWatcher().addObject(5, 0);
        this.getDataWatcher().addObject(6, 0);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        this.setElevatorYOffset(nbttagcompound.getInteger("Offset"));
        this.setOverlay(nbttagcompound.getShort("overlay"));

        ItemStack Camo = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem"));
        if (Camo != null) {
            this.setCamoItem(Camo);
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("Offset", this.getElevatorYOffset());
        if (this.getCamoItem() != null) {
            NBTTagCompound itemNBTTagCompound = new NBTTagCompound();
            this.getCamoItem().writeToNBT(itemNBTTagCompound);

            nbttagcompound.setTag("CamoItem",
                    itemNBTTagCompound);
        }
        nbttagcompound.setShort("overlay", this.getOverlay());
    }

    public ItemStack getCamoItem() {
        return this.getDataWatcher().getWatchableObjectItemStack(2);
    }

    public void setCamoItem(ItemStack itemstack) {
        this.getDataWatcher().updateObject(2,
                itemstack);
    }

    public Short getOverlay(){
        return this.getDataWatcher().getWatchableObjectShort(3);
    }

    public void setOverlay(Short overlay){
        this.getDataWatcher().updateObject(3, overlay);
    }

    private byte getStillCount() {
        return this.getDataWatcher().getWatchableObjectByte(4);
    }

    private void setStillCount(byte b) {
        this.getDataWatcher().updateObject(4,b);
    }

    @Override
    public void onUpdate(){
        super.onUpdate();
        if(this.worldObj.isRemote) {
            updateRiderPosition(this.motionY);
            this.moveEntity(0,this.motionY,0);
        }
    }

    protected void removeElevatorBlock() {
    	BlockPos pos = EntityHelper.getFlooredPosition(this.posX, this.posY, this.posZ);
        if (Block.getStateId(this.worldObj.getBlockState(pos)) == BlockLib.BLOCK_ELEVATOR_ID) {
            TileEntityElevator tile = (TileEntityElevator) BlockHelper.getTileEntity(this.worldObj, pos, TileEntityElevator.class);
            if (tile != null) {
                if (tile.getCamoItem() != null) this.setCamoItem(tile.removeCamoItemWithoutDrop());
                this.setElevatorYOffset(tile.getYOffest());
                this.setOverlay(tile.getOverlay());
            }
            this.worldObj.setBlockToAir(pos);
        }
    }

    protected void setTransitBlocks() {
    	BlockPos pos = EntityHelper.getFlooredPosition(this.posX, this.posY, this.posZ);
        if (this.getCamoItem() != null) {
            Block camouflage = Block.getBlockFromItem(this.getCamoItem().getItem());
            int blockLightValue = camouflage.getLightValue();
            int blockWeakPower = camouflage.isProvidingWeakPower(this.worldObj, pos, this.worldObj.getBlockState(pos), EnumFacing.DOWN/*0*/) / 2; //half the redstone strength
            int yOffset = this.prevPosY < this.posY? -1 : 1;
                if (this.worldObj.isAirBlock(pos.add(0, yOffset, 0))) {
                    this.worldObj.setBlockState(pos.add(0, yOffset, 0), ConfigurationLib.blockPoweredLight[blockLightValue].getStateFromMeta(blockWeakPower), 3);
                }
        }
    }

    public void setDead(BlockPos parentComputer) {
    	BlockPos pos = EntityHelper.getFlooredPosition(this.posX, this.posY, this.posZ);

        boolean blockPlaced = !this.worldObj.isRemote
                && (this.worldObj.getBlockState(pos).getBlock() == ConfigurationLib.blockTransportBase
                || this.worldObj.canBlockBePlaced(ConfigurationLib.blockTransportBase, pos, true, EnumFacing.UP/*1*/, null, null)
                && this.worldObj.setBlockState(pos, ConfigurationLib.blockTransportBase.getStateFromMeta(BlockLib.BLOCK_ELEVATOR_ID), 3));

        if (!this.worldObj.isRemote) {

            if (blockPlaced) {
                TileEntityElevator tile = (TileEntityElevator) this.worldObj.getTileEntity(pos);
                if (tile != null) {
                    tile.setParentElevatorComputer(parentComputer);
                    if (this.getCamoItem() != null) {
                        tile.setCamoItem(this.getCamoItem());
                    }
                    tile.setYOffset(this.getElevatorYOffset());
                    tile.setOverlay(this.getOverlay());
                }
            } else {
                this.entityDropItem(new ItemStack(ConfigurationLib.blockTransportBase, 1, BlockLib.BLOCK_ELEVATOR_ID),
                        0.0F);
            }
        }
        this.updateRiders(true,0);


        this.setDead();
    }
//TODO: canmovecheck return bool to parent to initiate logic
    public void checkMotion(double velocity, double minSpeed) {
       if (!this.entityElevatorObj.getEmerHalt()) {
            if (MathHelper.abs((float) velocity) < minSpeed) {
                if (this.getStillCount() > 10) {
                    this.setDead();
                    this.setStillCount((byte) (this.getStillCount() + 1));
                }
            } else {
                this.setStillCount((byte) 0);
            }
        }
    }

    public void updateRiderPosition(double velocity) {
        this.updateRiders(false, velocity);
    }
    
    protected void updateRiders(boolean atDestination, double velocity) {
        if (this.isDead) {
            return;
        }
        this.updatePotentialRiders(velocity);
        if (atDestination) {
            this.unmountRiders();
            return;
        }
        this.updateConfirmedRiders(velocity);
    }

    protected void updatePotentialRiders(double velocity) {
        List<Entity> potentialRiders = new ArrayList<Entity>();
        //Only scan any entities that are above the elevators current position for initial scan
        AxisAlignedBB boundBox = this.getBoundingBox().offset(0,
                .5,
                0);
        //noinspection unchecked
        potentialRiders.addAll(this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                boundBox));
        for (Entity rider : potentialRiders) {
            if (!(rider instanceof EntityElevatorPart) && !(rider instanceof EntityMasterElevator) && !rider.isRiding()
                    && !this.confirmedRiders.contains(rider)) {

                //don't grab flying players
                if (rider instanceof EntityPlayer && ((EntityPlayer)rider).capabilities.isFlying){
                    continue;
                }

                double yPos = (this.posY + this.getMountedYOffset())
                        - rider.getBoundingBox().minY;

                rider.motionY = velocity < 0 ? velocity : Math.max(yPos,
                        rider.motionY);

                rider.isAirBorne= false;
                rider.onGround = true;

                rider.fallDistance = 0;

                this.confirmedRiders.add(rider);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return AxisAlignedBB.fromBounds(this.posX - 0.5,
                this.posY - 0.5,
                this.posZ - 0.5,
                this.posX + 0.5,
                this.posY + 0.5,
                this.posZ + 0.5);
    }

    protected boolean isRiding(Entity rider) {
        return rider != null
                && !rider.isRiding()
                && rider.getBoundingBox().maxX >= this.getBoundingBox().minX
                && rider.getBoundingBox().minX <= this.getBoundingBox().maxX
                && rider.getBoundingBox().maxZ >= this.getBoundingBox().minZ
                && rider.getBoundingBox().minZ <= this.getBoundingBox().maxZ
                && rider.getBoundingBox().minY <= (this.posY
                + this.getMountedYOffset() + .5);
    }

    protected void updateConfirmedRiders(double velocity) {
        if (!this.confirmedRiders.isEmpty()) {
            Iterator<Entity> entities = this.confirmedRiders.iterator();
            while (entities.hasNext()) {
                Entity rider = entities.next();
                if (isRiding(rider)) {
                    double yPos = (this.posY + this.getMountedYOffset())
                            - rider.getBoundingBox().minY;
                    double yDif = Math.abs(this.posY + this.getMountedYOffset()
                            - rider.getBoundingBox().minY);
                    if (yDif < 1.0) {
                        rider.motionY = velocity < 0 ? velocity : Math.max(yPos,
                                rider.motionY);
                    } else {
                        rider.moveEntity(0,
                                yPos,
                                0);
                        rider.motionY = velocity;
                    }
                    rider.isAirBorne = true;

                    rider.onGround = true;
                    rider.fallDistance = 0;

                } else {
                    entities.remove();
                }
            }
        }
    }

    protected void unmountRiders() {
        for ( Entity rider : this.confirmedRiders) {
            if (rider != null) {
                rider.getBoundingBox().offset(0,
                        this.getMountedYOffset(),
                        0);
                rider.posY = this.getBoundingBox().maxY
                        + this.getMountedYOffset() + rider.getYOffset();
                rider.isAirBorne = false;
                rider.onGround = true;
                rider.fallDistance = 0;
            }
        }
    }

    @Override
    public double getMountedYOffset() {
        return 0.50D;
    }


    public int getElevatorYOffset() {
        return elevatorYOffset;
    }

    public void setElevatorYOffset(int elevatorYOffset) {
        this.elevatorYOffset = elevatorYOffset;
    }

    public void setParentElevator(EntityMasterElevator entityMasterElevator) {
        this.entityElevatorObj=entityMasterElevator;
    }

    public void setArrived() {
        this.getDataWatcher().updateObject(6,1);
    }

    public Boolean hasArrived() {
      return  this.getDataWatcher().getWatchableObjectInt(6) == 1;
    }
}

    
