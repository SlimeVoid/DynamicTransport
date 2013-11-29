package slimevoid.dynamictransport.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;

public class EntityElevator extends Entity {

	private static final int	blockID					= ConfigurationLib.blockTransportBaseID;
	private static final int	blockMeta				= BlockLib.BLOCK_ELEVATOR_ID;
	private byte				stillcount				= 0;
	private byte				waitToAccelerate		= 0;
	public int					dest;
	private float				destY;
	private boolean				atDestination;
	boolean						unUpdated;

	private float				elevatorSpeed			= 0.0F;
	private static final float	elevatorAccel			= 0.01F;
	private static final float	maxElevatorSpeed		= 0.4F;
	private static final float	minElevatorMovingSpeed	= 0.016F;
	public Set<Entity>			mountedEntities;

	private String				elevatorName			= "";
	private String				destFloorName			= "";
	private boolean				canBeHalted				= true;
	private boolean				enableMobilePower		= false;
	private ChunkCoordinates	computerPos				= null;

	public boolean				emerHalt				= false;
	public int					startStops				= 0;

	public int					tickcount				= 0;

	public EntityElevator		floor					= null;

	public EntityElevator		centerElevator			= null;
	public Set<EntityElevator>	conjoinedelevators		= new HashSet<EntityElevator>();
	public boolean				center					= false;
	private boolean				conjoinedHasBeenSet		= false;

	private boolean				isClient;

	private boolean				propertiesSet			= false;

	private boolean				slowingDown				= false;

	public EntityElevator(World world) {
		super(world);
		this.preventEntitySpawning = true;
		this.isImmuneToFire = true;
		this.entityCollisionReduction = 1.0F;
		this.ignoreFrustumCheck = true;
		setSize(0.98F,
				0.98F);

		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;

		atDestination = false;
		unUpdated = true;
		mountedEntities = new HashSet<Entity>();
		riddenByEntity = null;

		waitToAccelerate = 100;
		centerElevator = this;
		ridingEntity = null;
		conjoinedelevators.add(this);

		this.dataWatcher.addObject(	17,
									new Integer(0));
	}

	public EntityElevator(World world, double i, double j, double k) {
		this(world);
		prevPosX = i + 0.5F;
		prevPosY = j + 0.5F;
		prevPosZ = k + 0.5F;
		setPosition(prevPosX,
					prevPosY,
					prevPosZ);
		say((new StringBuilder()).append("Elevator created at ").append(i).append(", ").append(j).append(", ").append(k).toString());

		dest = (int) j;
		destY = (float) j + 0.5F;

		isClient = true;
		center = false;

		waitToAccelerate = 0;

		this.dataWatcher.updateObject(	17,
										0);
	}

	public void setProperties(int destination, String destinationName, boolean isCenter, boolean local, int meta, ChunkCoordinates computer, boolean haltable) {
		if (propertiesSet) {
			return;
		}

		dest = destination;
		destY = dest + 0.5F;
		destFloorName = destinationName;

		this.computerPos = computer;
		this.canBeHalted = haltable;

		isClient = local;
		center = isCenter;

		waitToAccelerate = 0;

		propertiesSet = true;

		say("Properties set! destination: " + destination + ", isClient: "
			+ isClient + ", center: " + center + ", metadata: " + meta);

		this.dataWatcher.updateObject(	17,
										meta);
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return true;
	}

	private void say(String s) {
		// DECore.say((new
		// StringBuilder()).append(" [ ").append(entityId).append(" ] ").append(s).toString());
	}

	// -------------------------------------------------------------------- //
	// ------------------ SERVER/CLIENT SENSITIVE CODE! ------------------- //

	public void setEmerHalt(boolean newhalt) {
		if (!this.canBeHalted && newhalt) {
			return;
		}
		if (!isClient) {
			return;
		}
		emerHalt = newhalt;

		if (emerHalt && !mountedEntities.isEmpty()) {
			ejectRiders();
		}
		if (emerHalt) {
			motionY = 0;
			elevatorSpeed = 0;
		}

		if (center) {
			Iterator<EntityElevator> iter = conjoinedelevators.iterator();
			while (iter.hasNext()) {
				EntityElevator curElevator = iter.next();
				if (curElevator != this && curElevator.emerHalt != emerHalt) {
					curElevator.setEmerHalt(emerHalt);
				}
			}
		} else if (centerElevator.emerHalt != emerHalt) {
			centerElevator.setEmerHalt(emerHalt);
		}
	}

	// ---------------- END SERVER/CLIENT SENSITIVE CODE ------------------ //
	// -------------------------------------------------------------------- //

	public void refreshRiders() {
		if (!center) {
			return;
		}
		mountedEntities.clear();

		Iterator<EntityElevator> elevators = conjoinedelevators.iterator();
		while (elevators.hasNext()) {
			EntityElevator curElevator = elevators.next();
			AxisAlignedBB boundBox = curElevator.getBoundingBox().expand(	0,
																			2.0,
																			0);
			boundBox.minY += 1.5;

			Set<Entity> potentialEntities = new HashSet<Entity>();
			potentialEntities.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	this,
																					boundBox));
			Iterator<Entity> iter = potentialEntities.iterator();
			while (iter.hasNext()) {
				Entity entity = iter.next();
				if (entity != null && !(entity instanceof EntityElevator)
					&& !mountedEntities.contains(entity)) {
					if (entity.ridingEntity == null) {
						mountedEntities.add(entity);
					}
				}
			}
		}
	}

	/*
	 * public boolean collideEntity(Entity entity) {
	 * say("Collision detected..."); say("Entity Position: " + entity.posY +
	 * ", Entity height: " + entity.height + ", Entity Offset: " +
	 * entity.yOffset + ", Entity speed: " + entity.motionY); if
	 * (mod_Elevator.killBelow && entity.posY < this.posY && entity instanceof
	 * EntityLiving) { EntityLiving living = (EntityLiving)entity;
	 * living.attackEntityFrom(DamageSource.inWall, 50);
	 * mod_Elevator.say("Damaging!"); return true; } return false; }
	 */

	@Override
	public void mountEntity(Entity entity) {
	}

	public void setConjoined(Set<EntityElevator> entitylist) {
		conjoinedelevators.addAll(entitylist);
		conjoinedHasBeenSet = true;
	}

	@Override
	public void updateRiderPosition() {
		// TODO :: Add ability to crush riders who don't have enough room above
		// their heads
		// TODO :: Should depend on amount of room and rider height
		if (this.isDead) {
			return;
		}
		if (!worldObj.isRemote && !mountedEntities.isEmpty()) {
			Iterator<Entity> iter = mountedEntities.iterator();
			while (iter.hasNext()) {
				updateRider(iter.next());
			}
			/*
			 * ElevatorPacketHandler.sendRiderUpdates( mountedEntities, (int)
			 * this.posX, (int) this.posY, (int) this.posZ);
			 */
		}
	}

	private void ejectRiders() {
		if (mountedEntities.isEmpty()) {
			return;
		}
		Iterator<Entity> iter = mountedEntities.iterator();
		while (iter.hasNext()) {
			Entity rider = iter.next();
			double pos = Math.abs(rider.posY - this.posY);
			if (pos < 1.0) {
				if (this.motionY > 0) rider.posY += 1F;
			}
			rider.motionY = 0.1F;
			updateRider(rider);
			rider.mountEntity(null);
			say("Ejected rider #" + rider.entityId);
		}
		/*
		 * ElevatorPacketHandler.sendRiderUpdates( mountedEntities, (int)
		 * this.posX, (int) this.posY, (int) this.posZ, true);
		 */
		mountedEntities.clear();
	}

	@Override
	public double getMountedYOffset() {
		return 0.5D;
	}

	public void updateRider(Entity rider) {
		if (rider == null) {
			return;
		}
		if (worldObj.isRemote) {
			return;
		}
		say("Difference: " + (rider.posY - this.posY));
		if (rider instanceof EntityLiving) {
			// if (rider instanceof EntityPlayer) {
			rider.posY = centerElevator.posY + getMountedYOffset()
							+ rider.yOffset;
			// }
			// elses {
			// / rider.setPosition(rider.posX, centerElevator.posY +
			// getMountedYOffset() + rider.getYOffset(), rider.posZ);
			// }
			rider.onGround = true;
			rider.fallDistance = 0.0F;
			rider.isCollidedVertically = true;
		} else if (!(rider instanceof EntityElevator)) {
			rider.posY = centerElevator.posY + getMountedYOffset()
							+ rider.yOffset;
			rider.onGround = false;
		}

		say((new StringBuilder()).append("Updating rider with id #").append(rider.entityId).append(" to ").append(rider.posY).toString());
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected boolean canTriggerWalking() {
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public void onUpdate() {

		if (worldObj.isRemote) {
			return;
		}

		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		tickcount++;
		if (tickcount > 45) {
			startStops--;
			if (startStops < 0) {
				startStops = 0;
			}
			tickcount = 0;
		}

		if (unUpdated) {
			if (worldObj.getBlockId(i,
									j,
									k) == blockID) {

				BlockTransportBase elevator = (BlockTransportBase) Block.blocksList[blockID];
				TileEntityElevator curTile = (TileEntityElevator) worldObj.getBlockTileEntity(	i,
																								j,
																								k);

				if (this.enableMobilePower) {
					worldObj.setBlock(	i,
										j,
										k,
										blockID,
										1,
										2);
				} else {
					worldObj.setBlock(	i,
										j,
										k,
										0);
				}
				worldObj.notifyBlocksOfNeighborChange(	i,
														j,
														k,
														blockID);
				worldObj.notifyBlocksOfNeighborChange(	i - 1,
														j,
														k,
														blockID);
				worldObj.notifyBlocksOfNeighborChange(	i + 1,
														j,
														k,
														blockID);
				worldObj.notifyBlocksOfNeighborChange(	i,
														j,
														k - 1,
														blockID);
				worldObj.notifyBlocksOfNeighborChange(	i,
														j,
														k + 1,
														blockID);

			}
			conjoinAllNeighbors();
			unUpdated = false;
		}

		// Place transient block
		if (!this.isDead && this.enableMobilePower) {
			int curX = i;
			int curY = j;
			int curZ = k;
			if (this.motionY > 0) {
				curX = (int) Math.ceil(posX - 0.5);
				curY = (int) Math.ceil(posY - 0.5);
				curZ = (int) Math.ceil(posZ - 0.5);
			} else {
				curX = (int) Math.floor(posX - 0.5);
				curY = (int) Math.floor(posY - 0.5);
				curZ = (int) Math.floor(posZ - 0.5);
			}
			int underId = worldObj.getBlockId(	curX,
												curY,
												curZ);

			if (underId == 0) {
				worldObj.setBlock(	i,
									j,
									k,
									blockID,
									1,
									2);
			}
		}

		if (!center) {
			say((new StringBuilder()).append("Speed: ").append(motionY).append(", posY: ").append(posY).append(", destY: ").append(destY).append(", center: "
																																					+ center).toString());
			if (centerElevator != null && !centerElevator.isDead) {

				this.setPosition(	this.posX,
									centerElevator.posY,
									this.posZ);
			} else if (!this.isDead) {
				this.killElevator();
			}
			return;
		}

		float range = 0.0F;

		if (!conjoinedHasBeenSet) {
			conjoinAllNeighbors();
		}

		if (emerHalt) {
			elevatorSpeed = 0;
		} else if (waitToAccelerate < 15) {
			if (waitToAccelerate < 10) {
				elevatorSpeed = 0;
			} else {
				elevatorSpeed = minElevatorMovingSpeed;
			}
			waitToAccelerate++;
			if (!conjoinedelevators.contains(this)) {
				conjoinedelevators.add(this);
			}
			say((new StringBuilder()).append("Waiting to accelerate").toString());
		} else {
			float tempSpeed = elevatorSpeed + elevatorAccel;
			if (tempSpeed > maxElevatorSpeed) {
				tempSpeed = maxElevatorSpeed;
			}
			// Calculate elevator range to break
			range = (tempSpeed * tempSpeed - minElevatorMovingSpeed
												* minElevatorMovingSpeed)
					/ (2 * elevatorAccel);
			if (!slowingDown
				&& MathHelper.abs((float) (destY - posY)) >= (range)) {
				// if current destination is further away than this range and <
				// max speed, continue to accelerate
				elevatorSpeed = tempSpeed;
			}
			// else start to slow down
			else {
				elevatorSpeed -= elevatorAccel;
				slowingDown = true;
			}
			if (elevatorSpeed > maxElevatorSpeed) {
				elevatorSpeed = maxElevatorSpeed;
			}
			if (elevatorSpeed < minElevatorMovingSpeed) {
				elevatorSpeed = minElevatorMovingSpeed;
			}
		}
		// check whether at the destination or not
		atDestination = onGround
						|| (MathHelper.abs((float) (destY - posY)) < elevatorSpeed);
		if (destY < 1 || destY > this.worldObj.getHeight()) {
			atDestination = true;
			say("Requested destination is too high or too low!");
			say((new StringBuilder()).append("Requested: ").append(destY).append(", max: ").append(this.worldObj.getHeight()).toString());
		}

		say((new StringBuilder()).append("Speed: ").append(motionY).append(", posY: ").append(posY).append(", destY: ").append(destY).append(", range: ").append(range).append(", center: "
																																												+ center).toString());

		refreshRiders();

		// if not there yet, update speed and location
		if (!atDestination) {
			motionY = (destY > posY) ? elevatorSpeed : -elevatorSpeed;
			// updateAllConjoined();
		} else if (atDestination) {
			killAllConjoined();
			return;
		}
		this.setPosition(	this.posX,
							this.posY + this.motionY,
							this.posZ);
		if (!worldObj.isRemote) {
			updateRiderPosition();
		}

		if (!emerHalt) {
			if (MathHelper.abs((float) motionY) < minElevatorMovingSpeed
				&& stillcount++ > 10) {
				killAllConjoined();
			} else {
				stillcount = 0;
			}
		}
		say((new StringBuilder()).append("End Entity Update").toString());
	}

	private void killAllConjoined() {
		Iterator<EntityElevator> iter = this.conjoinedelevators.iterator();
		while (iter.hasNext()) {
			EntityElevator curElevator = iter.next();
			curElevator.killElevator();
			/*
			 * if (curElevator.ceiling != null) {
			 * curElevator.ceiling.killElevator(); }
			 */
		}
	}

	public void killElevator() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		int curY = MathHelper.floor_double(posY);

		boolean blockPlaced = !worldObj.isRemote
								&& (worldObj.getBlockId(i,
														curY,
														k) == blockID || worldObj.canPlaceEntityOnSide(	blockID,
																										i,
																										curY,
																										k,
																										true,
																										1,
																										(Entity) null,
																										null)
																			&& worldObj.setBlock(	i,
																									curY,
																									k,
																									blockID,
																									this.blockMeta,
																									2));

		if (!worldObj.isRemote && !blockPlaced) {
			dropItem(	blockID,
						1);
		}

		if (!worldObj.isRemote) {
			if (this.destFloorName.trim() != "" && center) {

				Iterator<Entity> iter = mountedEntities.iterator();
				while (iter.hasNext()) {
					Entity curentity = iter.next();
					curentity.posY += 0.5;
					if (curentity instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) curentity;
						player.addChatMessage("Elevator Arrived at" + " "
												+ destFloorName);
					}
				}
			}

			setDead();
			ejectRiders();
			say((new StringBuilder()).append("Entity Dead").toString());
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i) {
		if (isDead) {
			return true;
		}

		setEmerHalt(!emerHalt);

		startStops++;
		if (startStops > 2) {
			killElevator();
		}
		return true;
	}

	public Set<EntityElevator> getNeighbors() {
		Set<EntityElevator> conjoineds = new HashSet<EntityElevator>();

		Set<Entity> neighbors = new HashSet<Entity>();
		neighbors.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	null,
																		getBoundingBox().expand(0.5,
																								0,
																								0.5)));
		Iterator<Entity> iter = neighbors.iterator();
		while (iter.hasNext()) {
			Entity curEntity = iter.next();
			if (curEntity instanceof EntityElevator) {
				conjoineds.add((EntityElevator) curEntity);
			}
		}
		return conjoineds;
	}

	public void conjoinAllNeighbors() {
		if (!center) {
			return;
		}
		conjoinedelevators.clear();
		Set<EntityElevator> notChecked = new HashSet<EntityElevator>();
		conjoinedelevators.addAll(getNeighbors());
		notChecked.addAll(conjoinedelevators);
		notChecked.remove(this);
		while (!notChecked.isEmpty()) {
			Iterator<EntityElevator> iter = notChecked.iterator();
			Set<EntityElevator> newElevators = new HashSet<EntityElevator>();
			while (iter.hasNext()) {
				EntityElevator curElevator = iter.next();
				newElevators.addAll(curElevator.getNeighbors());
			}
			notChecked.clear();
			newElevators.removeAll(conjoinedelevators);
			conjoinedelevators.addAll(newElevators);
			notChecked.addAll(newElevators);
		}
		Iterator<EntityElevator> iter = conjoinedelevators.iterator();
		while (iter.hasNext()) {
			EntityElevator curElevator = iter.next();
			if (curElevator.center) { // || isClient) {
				curElevator.setConjoined(conjoinedelevators);
				curElevator.centerElevator = curElevator;

			} else {
				curElevator.centerElevator = this;

			}
		}
		conjoinedHasBeenSet = true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger(	"destY",
									dest);
		nbttagcompound.setBoolean(	"emerHalt",
									emerHalt);
		nbttagcompound.setBoolean(	"isClient",
									isClient);
		nbttagcompound.setBoolean(	"isCenter",
									center);
		nbttagcompound.setInteger(	"metadata",
									dataWatcher.getWatchableObjectInt(17));

		// props.writeToNBT(nbttagcompound);

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		try {
			dest = nbttagcompound.getInteger("destY");
			dataWatcher.updateObject(	17,
										nbttagcompound.getInteger("metadata"));
		} catch (Exception e) {
			dest = nbttagcompound.getByte("destY");
		}
		emerHalt = nbttagcompound.getBoolean("emerHalt");
		isClient = nbttagcompound.getBoolean("isClient");
		center = nbttagcompound.getBoolean("isCenter");
		if (!conjoinedelevators.contains(this)) {
			conjoinedelevators.add(this);
		}
		destY = dest + 0.5F;
		setPosition(posX,
					posY,
					posZ);

		// props.readFromNBT(nbttagcompound);

	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.getBoundingBox();
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return AxisAlignedBB.getBoundingBox(posX - 0.5,
											posY - 0.5,
											posZ - 0.5,
											posX + 0.5,
											posY + 0.5,
											posZ + 0.5);
	}

	@Override
	public float getShadowSize() {
		return 0.0F;
	}

	public World getWorld() {
		return worldObj;
	}

}
