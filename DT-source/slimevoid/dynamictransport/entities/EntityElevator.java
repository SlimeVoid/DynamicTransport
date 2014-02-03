package slimevoid.dynamictransport.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoidlib.util.helpers.BlockHelper;
import cpw.mods.fml.common.network.PacketDispatcher;

public class EntityElevator extends Entity {
    // Constants
    protected static final float elevatorAccel          = 0.01F;
    protected static final float minElevatorMovingSpeed = 0.016F;

    // server only
    protected ChunkCoordinates   computerPos            = null;
    protected String             elevatorName           = "";
    protected String             destFloorName          = "";
    protected boolean            canBeHalted            = true;
    protected boolean            enableMobilePower      = false;

    // only needed for emerhalt but also used in kill all conjoined
    protected Set<Integer>       conjoinedelevators     = new HashSet<Integer>();
    protected Set<Integer>       confirmedRiders        = new HashSet<Integer>();

    // possible watcher
    protected byte               waitToAccelerate       = 0;
    protected int                startStops             = 0;
    protected int                notifierElevatorID     = 0;
    protected boolean            emerHalt               = false;
    protected boolean            isNotifierElevator     = false;

    // most likely fine
    protected byte               stillCount             = 0;
    protected boolean            slowingDown            = false;

    public EntityElevator(World world) {
        super(world);
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.entityCollisionReduction = 1.0F;
        this.ignoreFrustumCheck = true;
        this.setSize(0.98F,
                     0.98F);

        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.waitToAccelerate = 100;
    }

    public EntityElevator(World world, double i, double j, double k) {
        this(world);
        this.prevPosX = i + 0.5F;
        this.prevPosY = j + 0.5F;
        this.prevPosZ = k + 0.5F;
        this.setPosition(prevPosX,
                         prevPosY,
                         prevPosZ);

        this.isNotifierElevator = false;

        this.waitToAccelerate = 0;
        this.updatePotentialRiders();
    }

    public int getDestinationY() {
        return this.getDataWatcher().getWatchableObjectInt(2);
    }

    public float getMaximumSpeed() {
        return this.getDataWatcher().getWatchableObjectFloat(3);
    }

    public ItemStack getCamoItem() {
        return this.getDataWatcher().getWatchableObjectItemStack(4);
    }

    protected void setDestinationY(int destinationY) {
        this.getDataWatcher().updateObject(2,
                                           destinationY);
    }

    protected void setMaximumSpeed(float speed) {
        this.getDataWatcher().updateObject(3,
                                           speed);
    }

    protected void setCamoItem(ItemStack itemstack) {
        this.getDataWatcher().updateObject(4,
                                           itemstack);
    }

    @Override
    protected void entityInit() {
        this.getDataWatcher().addObject(2,
                                        new Integer(-1));
        this.getDataWatcher().addObject(3,
                                        0f);
        this.getDataWatcher().addObjectByDataType(4,
                                                  5);
    }

    @Override
    public void setDead() {
        int x = MathHelper.floor_double(this.posX);
        int z = MathHelper.floor_double(this.posZ);
        int y = MathHelper.floor_double(this.posY);

        boolean blockPlaced = !this.worldObj.isRemote
                              && (this.worldObj.getBlockId(x,
                                                           y,
                                                           z) == ConfigurationLib.blockTransportBaseID || this.worldObj.canPlaceEntityOnSide(ConfigurationLib.blockTransportBaseID,
                                                                                                                                             x,
                                                                                                                                             y,
                                                                                                                                             z,
                                                                                                                                             true,
                                                                                                                                             1,
                                                                                                                                             (Entity) null,
                                                                                                                                             null)
                                                                                                          && this.worldObj.setBlock(x,
                                                                                                                                    y,
                                                                                                                                    z,
                                                                                                                                    ConfigurationLib.blockTransportBaseID,
                                                                                                                                    BlockLib.BLOCK_ELEVATOR_ID,
                                                                                                                                    3));

        if (!this.worldObj.isRemote) {
            if (blockPlaced) {
                TileEntityElevator tile = (TileEntityElevator) this.worldObj.getBlockTileEntity(x,
                                                                                                y,
                                                                                                z);
                if (tile != null) {
                    tile.setParentElevatorComputer(this.computerPos);
                    if (this.getCamoItem() != null) {
                        tile.setCamoItem(this.getCamoItem());
                    }
                }
            } else {
                this.entityDropItem(new ItemStack(ConfigurationLib.blockTransportBaseID, 1, BlockLib.BLOCK_ELEVATOR_ID),
                                    0.0F);
            }
        }
        this.updateRiders(true);

        if (!this.worldObj.isRemote) {
            if (this.isNotifierElevator) {
                ChatMessageComponent message;
                if (this.elevatorName != null
                    && !this.elevatorName.trim().equals("")) {
                    message = ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.entityElevator.arriveWithName",
                                                                                          this.elevatorName,
                                                                                          this.destFloorName);
                } else {
                    message = ChatMessageComponent.createFromTranslationWithSubstitutions("slimevoid.DT.entityElevator.arrive",
                                                                                          this.destFloorName);
                }
                PacketDispatcher.sendPacketToAllAround(this.posX,
                                                       this.posY,
                                                       this.posZ,
                                                       4,
                                                       this.worldObj.provider.dimensionId,
                                                       new Packet3Chat(message));

            }

        }
        super.setDead();
    }

    @Override
    public void onUpdate() {
        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.posY);
        int z = MathHelper.floor_double(this.posZ);

        // on first update remove blocks
        if (!this.worldObj.isRemote && this.ticksExisted == 1) {
            this.removeElevatorBlock(x,
                                     y,
                                     z);
        }

        if (this.getDestinationY() == -1) {
            return;
        }

        // Place transient block
        if (!this.worldObj.isRemote && !this.isDead && this.enableMobilePower) {
            this.setTransitBlocks(x,
                                  y,
                                  z);
        }

        this.updateLightLevel(x,
                              y,
                              z);

        this.moveElevator();
        this.updateRiderPosition();

        if (!this.emerHalt) {
            if (MathHelper.abs((float) this.motionY) < minElevatorMovingSpeed) {
                if (this.stillCount++ > 10) {
                    // should notify computer that this block is invalid
                    // that way the computer doesn't think it still has this
                    // block when it goes into maintenance
                    this.killAllConjoined();
                }
            } else {
                this.stillCount = 0;
            }
        }
    }

    protected void moveElevator() {
        if (this.velocityChanged) {
            this.velocityChanged = false;
            setEmerHalt(!this.emerHalt);

            this.startStops++;
            if (this.startStops > 2) {
                setDead();
            }
        }

        float destY = this.getDestinationY() + 0.5F;
        float elevatorSpeed = (float) Math.abs(this.motionY);
        if (this.emerHalt) {
            elevatorSpeed = 0;
        } else if (this.waitToAccelerate < 15) {
            if (this.waitToAccelerate < 10) {
                elevatorSpeed = 0;
            } else {
                elevatorSpeed = minElevatorMovingSpeed;
            }
            this.waitToAccelerate++;

        } else {
            float maxElevatorSpeed = this.getMaximumSpeed();
            float tempSpeed = elevatorSpeed + elevatorAccel;
            if (tempSpeed > maxElevatorSpeed) {
                tempSpeed = maxElevatorSpeed;
            }
            // Calculate elevator range to break

            if (!this.slowingDown
                && MathHelper.abs((float) (destY - posY)) >= (tempSpeed
                                                              * tempSpeed - minElevatorMovingSpeed
                                                                            * minElevatorMovingSpeed)
                                                             / (2 * elevatorAccel)) {
                // if current destination is further away than this range and <
                // max speed, continue to accelerate
                elevatorSpeed = tempSpeed;
            }
            // else start to slow down
            else {
                elevatorSpeed -= elevatorAccel;
                this.slowingDown = true;
            }
            if (elevatorSpeed > maxElevatorSpeed) {
                elevatorSpeed = maxElevatorSpeed;
            }
            if (elevatorSpeed < minElevatorMovingSpeed) {
                elevatorSpeed = minElevatorMovingSpeed;
            }
        }
        // check whether at the destination or not
        boolean atDestination = this.onGround
                                || (MathHelper.abs((float) (destY - this.posY)) < elevatorSpeed);
        if (destY < 1 || destY > this.worldObj.getHeight()) {
            atDestination = true;
        }

        // if not there yet, update speed and location
        if (!atDestination) {
            this.motionY = (destY > this.posY) ? elevatorSpeed : -elevatorSpeed;
        } else if (atDestination) {
            this.killAllConjoined();
            return;
        }
        this.moveEntity(this.motionX,
                        this.motionY,
                        this.motionX);
    }

    protected void updateLightLevel(int x, int y, int z) {
        if (this.getCamoItem() != null) {
            int blockLightValue = Block.lightValue[((ItemBlock) this.getCamoItem().getItem()).getBlockID()];
            if (blockLightValue > this.worldObj.getSavedLightValue(EnumSkyBlock.Block,
                                                                   x,
                                                                   y,
                                                                   z)) {
                this.worldObj.setLightValue(EnumSkyBlock.Block,
                                            x,
                                            y,
                                            z,
                                            blockLightValue);
            }
            if (this.prevPosY < this.posY) {
                this.worldObj.updateLightByType(EnumSkyBlock.Block,
                                                x,
                                                y - 1,
                                                z);
            } else {
                this.worldObj.updateLightByType(EnumSkyBlock.Block,
                                                x,
                                                y + 1,
                                                z);
            }
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.posX - 0.5,
                                            this.posY - 0.5,
                                            this.posZ - 0.5,
                                            this.posX + 0.5,
                                            this.posY + 0.5,
                                            this.posZ + 0.5);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("destY",
                                  this.getDestinationY());
        if (this.destFloorName != null && !this.destFloorName.trim().isEmpty()) {
            nbttagcompound.setString("destName",
                                     this.destFloorName);
        }
        nbttagcompound.setBoolean("emerHalt",
                                  this.emerHalt);
        nbttagcompound.setBoolean("isCenter",
                                  isNotifierElevator);
        nbttagcompound.setInteger("ComputerX",
                                  this.computerPos.posX);
        nbttagcompound.setInteger("ComputerY",
                                  this.computerPos.posY);
        nbttagcompound.setInteger("ComputerZ",
                                  this.computerPos.posZ);
        nbttagcompound.setFloat("TopSpeed",
                                this.getMaximumSpeed());

        if (this.getCamoItem() != null) {
            NBTTagCompound itemNBTTagCompound = new NBTTagCompound();
            this.getCamoItem().writeToNBT(itemNBTTagCompound);

            nbttagcompound.setTag("CamoItem",
                                  itemNBTTagCompound);
        }

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        this.setDestinationY(nbttagcompound.getInteger("destY"));
        this.setMaximumSpeed(nbttagcompound.getFloat("TopSpeed"));
        this.emerHalt = nbttagcompound.getBoolean("emerHalt");
        this.destFloorName = nbttagcompound.getString("destName");
        this.isNotifierElevator = nbttagcompound.getBoolean("isCenter");
        this.computerPos = new ChunkCoordinates(nbttagcompound.getInteger("ComputerX"), nbttagcompound.getInteger("ComputerY"), nbttagcompound.getInteger("ComputerZ"));
        if (ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem")) != null) {
            this.setCamoItem(ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem")));
        }
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity.getBoundingBox();
    }

    protected void unmountRiders() {
        if (!this.confirmedRiders.isEmpty()) {
            for (Integer entityID : this.confirmedRiders) {
                Entity rider = this.worldObj.getEntityByID(entityID);
                if (rider != null) {
                    rider.boundingBox.offset(0,
                                             this.getMountedYOffset(),
                                             0);
                    rider.posY = this.getBoundingBox().maxY
                                 + this.getMountedYOffset() + rider.yOffset;
                    rider.isAirBorne = false;
                    rider.onGround = true;
                    rider.fallDistance = 0;
                }
            }
            this.confirmedRiders.clear();
        }
    }

    protected void updatePotentialRiders() {
        Set<Entity> potentialRiders = new HashSet<Entity>();
        AxisAlignedBB boundBox = this.getBoundingBox().offset(0,
                                                              1,
                                                              0).expand(0,
                                                                        1.0,
                                                                        0);
        potentialRiders.addAll(this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                                                                                  boundBox));
        for (Entity rider : potentialRiders) {
            if (!(rider instanceof EntityElevator)
                && !this.confirmedRiders.contains(rider.entityId)) {
                double yPos = (this.posY + this.getMountedYOffset())
                              - rider.boundingBox.minY;
                rider.motionY = this.motionY < 0 ? this.motionY : Math.max(yPos,
                                                                           rider.motionY);
                rider.isAirBorne = true;
                rider.onGround = true;
                rider.fallDistance = 0;
                this.confirmedRiders.add(rider.entityId);
            }
        }
    }

    protected boolean isRiding(Entity rider) {
        return rider != null
               && rider.boundingBox.maxX >= this.getBoundingBox().minX
               && rider.boundingBox.minX <= this.getBoundingBox().maxX
               && rider.boundingBox.maxZ >= this.getBoundingBox().minZ
               && rider.boundingBox.minZ <= this.getBoundingBox().maxZ
               && rider.boundingBox.minY <= (this.posY
                                             + this.getMountedYOffset() + 2.0);
    }

    protected void updateConfirmedRiders() {
        if (!this.confirmedRiders.isEmpty()) {
            Set<Integer> removedRiders = new HashSet<Integer>();
            for (Integer entityID : this.confirmedRiders) {
                Entity rider = this.worldObj.getEntityByID(entityID);

                if (isRiding(rider)) {
                    double yPos = (this.posY + this.getMountedYOffset())
                                  - rider.boundingBox.minY;
                    double yDif = Math.abs(this.posY + this.getMountedYOffset()
                                           - rider.boundingBox.minY);
                    if (yDif < 1.0) {
                        rider.motionY = this.motionY < 0 ? this.motionY : Math.max(yPos,
                                                                                   rider.motionY);
                    } else {
                        rider.moveEntity(0,
                                         yPos,
                                         0);
                        rider.motionY = this.motionY;
                    }
                    rider.isAirBorne = true;
                    rider.onGround = true;
                    rider.fallDistance = 0;
                } else {
                    removedRiders.add(entityID);
                }
            }

            if (!removedRiders.isEmpty()) {
                this.confirmedRiders.removeAll(removedRiders);
            }
        }
    }

    protected void updateRiders(boolean atDestination) {
        if (this.isDead) {
            return;
        }
        this.updatePotentialRiders();
        if (atDestination) {
            this.unmountRiders();
            return;
        }
        this.updateConfirmedRiders();
    }

    // this should be called by each elevator entity and not just the controller
    @Override
    public void updateRiderPosition() {
        this.updateRiders(false);
    }

    @Override
    public double getMountedYOffset() {
        return 0.50D;
    }

    public void setProperties(int destination, String destinationName, float elevatorTopSpeed, ChunkCoordinates computer, boolean haltable, int notifierID, boolean mobilePower) {
        this.setDestinationY(destination);
        this.destFloorName = destinationName != null
                             && destinationName.trim() != "" ? destinationName : String.valueOf(destination);

        this.computerPos = computer;
        this.canBeHalted = haltable;
        this.enableMobilePower = mobilePower;

        this.isNotifierElevator = (notifierID == this.entityId);

        this.setMaximumSpeed(elevatorTopSpeed);

        this.waitToAccelerate = 0;

        if (!this.isNotifierElevator) {
            this.notifierElevatorID = notifierID;
            this.getNotifier().conjoinedelevators.add(this.entityId);
        }
    }

    protected void removeElevatorBlock(int x, int y, int z) {
        if (this.worldObj.getBlockId(x,
                                     y,
                                     z) == ConfigurationLib.blockTransportBaseID
            && this.worldObj.getBlockMetadata(x,
                                              y,
                                              z) == BlockLib.BLOCK_ELEVATOR_ID) {
            TileEntityElevator tile = (TileEntityElevator) BlockHelper.getTileEntity(this.worldObj,
                                                                                     x,
                                                                                     y,
                                                                                     z,
                                                                                     TileEntityElevator.class);
            if (tile != null) {
                if (tile.getCamoItem() != null) {
                    this.setCamoItem(tile.removeCamoItemWithoutDrop());
                }

            }

            if (this.enableMobilePower) {
                this.worldObj.setBlock(x,
                                       y,
                                       z,
                                       ConfigurationLib.blockTransportBaseID,
                                       1,
                                       BlockLib.BLOCK_TRANSIT_ID);
            } else {
                this.worldObj.setBlockToAir(x,
                                            y,
                                            z);
            }

        }

    }

    protected void setTransitBlocks(int x, int y, int z) {

        if (this.motionY > 0) {
            x = (int) Math.ceil(this.posX - 0.5);
            y = (int) Math.ceil(this.posY - 0.5);
            z = (int) Math.ceil(this.posZ - 0.5);
        } else {
            x = (int) Math.floor(this.posX - 0.5);
            y = (int) Math.floor(this.posY - 0.5);
            z = (int) Math.floor(this.posZ - 0.5);
        }

        if (this.worldObj.isAirBlock(x,
                                     y,
                                     z)) {
            this.worldObj.setBlock(x,
                                   y,
                                   z,
                                   ConfigurationLib.blockTransportBaseID,
                                   1,
                                   BlockLib.BLOCK_TRANSIT_ID);
        }

    }

    // only function that absolutely needs to keep track of elevators
    protected void setEmerHalt(boolean newhalt) {
        if (!this.canBeHalted && newhalt) {
            return;
        }
        this.emerHalt = newhalt;

        if (this.emerHalt) {
            this.motionY = 0;
        }

        if (this.isNotifier()) {

            Iterator<Integer> iter = this.conjoinedelevators.iterator();
            while (iter.hasNext()) {
                EntityElevator curElevator = (EntityElevator) this.worldObj.getEntityByID(iter.next());
                if (curElevator != this
                    && curElevator.emerHalt != this.emerHalt) {
                    curElevator.setEmerHalt(this.emerHalt);
                }
            }
        } else if (this.getNotifier() != null
                   && this.getNotifier().emerHalt != this.emerHalt) {
            this.getNotifier().setEmerHalt(this.emerHalt);
        }
    }

    protected void killAllConjoined() {
        Iterator<Integer> iter = this.conjoinedelevators.iterator();
        while (iter.hasNext()) {
            EntityElevator curElevator = (EntityElevator) this.worldObj.getEntityByID(iter.next());
            if (curElevator != null) curElevator.setDead();
        }
        this.setDead();
        if (this.isNotifierElevator
            && MathHelper.floor_double(this.posY) == this.getDestinationY()) {
            TileEntityElevatorComputer computer = this.getParentElevatorComputer();
            if (computer != null) {
                computer.elevatorArrived(MathHelper.floor_double(this.posY),
                                         this.isNotifierElevator);
            }

        }
    }

    // Used to get isControler on both client and server
    protected boolean isNotifier() {
        return this.isNotifierElevator
               || (this.notifierElevatorID == this.entityId);
    }

    protected EntityElevator getNotifier() {
        return ((EntityElevator) this.worldObj.getEntityByID(this.notifierElevatorID));
    }

    // used to get access to the elevators computer
    protected TileEntityElevatorComputer getParentElevatorComputer() {
        TileEntityElevatorComputer computer = null;
        if (this.computerPos != null) {
            computer = (TileEntityElevatorComputer) BlockHelper.getTileEntity(this.worldObj,
                                                                              this.computerPos.posX,
                                                                              this.computerPos.posY,
                                                                              this.computerPos.posZ,
                                                                              TileEntityElevatorComputer.class);
        }

        return computer;
    }

}
