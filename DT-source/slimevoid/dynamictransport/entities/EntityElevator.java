package slimevoid.dynamictransport.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;

public class EntityElevator extends Entity {
	// Constants
	private static final int	blockID					= ConfigurationLib.blockTransportBaseID;
	private static final int	blockMeta				= BlockLib.BLOCK_ELEVATOR_ID;
	private static final float	elevatorAccel			= 0.01F;
	private static final float	maxElevatorSpeed		= ConfigurationLib.elevatorMaxSpeed;
	private static final float	minElevatorMovingSpeed	= 0.016F;

	// server only
	private ChunkCoordinates	computerPos				= null;
	private String				elevatorName			= "";
	private String				destFloorName			= "";
	private boolean				canBeHalted				= true;
	private boolean				enableMobilePower		= false;

	// only needed for emerhalt but also used in kill all conjoined
	public Set<Integer>			conjoinedelevators		= new HashSet<Integer>();
	public Set<Integer>			confirmedRiders			= new HashSet<Integer>();

	// possible watcher
	private byte				waitToAccelerate		= 0;
	public int					startStops				= 0;
	public int					controlingElevatorID	= 0;
	public boolean				emerHalt				= false;
	public boolean				iscontrolerElevator		= false;

	// most likely fine
	private byte				stillCount				= 0;
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
		waitToAccelerate = 100;
	}

	public EntityElevator(World world, double i, double j, double k) {
		this(world);
		prevPosX = i + 0.5F;
		prevPosY = j + 0.5F;
		prevPosZ = k + 0.5F;
		setPosition(prevPosX,
					prevPosY,
					prevPosZ);

		iscontrolerElevator = false;

		waitToAccelerate = 0;

	}

	@Override
	protected void entityInit() {
		this.getDataWatcher().addObject(2,
										new Integer(-1));
	}

	@Override
	public void setDead() {
		int i = MathHelper.floor_double(posX);
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
																									3));

		if (!worldObj.isRemote) {
			if (blockPlaced) {
				TileEntityElevator tile = (TileEntityElevator) this.worldObj.getBlockTileEntity(i,
																								curY,
																								k);
				if (tile != null) {
					tile.setParentElevatorComputer(this.computerPos);
				}
			} else {
				dropItem(	blockID,
							1);
			}
		}
		this.updateRiderPosition();

		if (!worldObj.isRemote) {
			if (iscontrolerElevator) {

				MinecraftServer.getServer().getConfigurationManager().sendToAllNear(this.posX,
																					this.posY,
																					this.posZ,
																					4,
																					this.worldObj.provider.dimensionId,
																					new Packet3Chat(new ChatMessageComponent().addText("Elevator Arrived at"
																																		+ " "
																																		+ (destFloorName == null
																																			|| destFloorName.trim().isEmpty() ? this.getDataWatcher().getWatchableObjectInt(2) : this.destFloorName))));

			}

		}
		super.setDead();
	}

	@Override
	public void onUpdate() {
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(posY);
		int z = MathHelper.floor_double(posZ);

		// on first update remove blocks
		if (this.ticksExisted == 1 && !worldObj.isRemote) {
			removeElevatorBlock(x,
								y,
								z);
		}

		if (this.getDataWatcher().getWatchableObjectInt(2) == -1) return;

		// Place transient block
		if (!worldObj.isRemote && !this.isDead && this.enableMobilePower) {
			this.setTransitBlocks(	x,
									y,
									z);
		}
		if (this.velocityChanged) {
			this.velocityChanged = false;
			setEmerHalt(!emerHalt);

			startStops++;
			if (startStops > 2) {
				setDead();
			}
		}

		float destY = this.getDataWatcher().getWatchableObjectInt(2) + 0.5F;
		float elevatorSpeed = (float) Math.abs(this.motionY);
		if (emerHalt) {
			elevatorSpeed = 0;
		} else if (waitToAccelerate < 15) {
			if (waitToAccelerate < 10) {
				elevatorSpeed = 0;
			} else {
				elevatorSpeed = minElevatorMovingSpeed;
			}
			waitToAccelerate++;

		} else {
			float tempSpeed = elevatorSpeed + elevatorAccel;
			if (tempSpeed > maxElevatorSpeed) {
				tempSpeed = maxElevatorSpeed;
			}
			// Calculate elevator range to break

			if (!slowingDown
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
		boolean atDestination = onGround
								|| (MathHelper.abs((float) (destY - posY)) < elevatorSpeed);
		if (destY < 1 || destY > this.worldObj.getHeight()) {
			atDestination = true;
		}

		// if not there yet, update speed and location
		if (!atDestination) {
			motionY = (destY > posY) ? elevatorSpeed : -elevatorSpeed;
		} else if (atDestination) {
			killAllConjoined();
			return;
		}
		this.moveEntity(this.motionX,
						this.motionY,
						this.motionX);

		updateRiderPosition();

		if (!emerHalt) {
			if (MathHelper.abs((float) motionY) < minElevatorMovingSpeed
				&& stillCount++ > 10) {
				killAllConjoined();
			} else {
				stillCount = 0;
			}
		}

	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
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
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger(	"destY",
									this.getDataWatcher().getWatchableObjectInt(2));
		if (this.destFloorName != null && !this.destFloorName.trim().isEmpty()) nbttagcompound.setString(	"destName",
																											this.destFloorName);
		nbttagcompound.setBoolean(	"emerHalt",
									emerHalt);
		nbttagcompound.setBoolean(	"isCenter",
									iscontrolerElevator);
		nbttagcompound.setInteger(	"ComputerX",
									this.computerPos.posX);
		nbttagcompound.setInteger(	"ComputerY",
									this.computerPos.posY);
		nbttagcompound.setInteger(	"ComputerZ",
									this.computerPos.posZ);

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		this.getDataWatcher().updateObject(	2,
											nbttagcompound.getInteger("destY"));
		this.emerHalt = nbttagcompound.getBoolean("emerHalt");
		this.iscontrolerElevator = nbttagcompound.getBoolean("isCenter");
		this.computerPos = new ChunkCoordinates(nbttagcompound.getInteger("ComputerX"), nbttagcompound.getInteger("ComputerY"), nbttagcompound.getInteger("ComputerZ"));
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.getBoundingBox();
	}

	// this should be called by each elevator entity and not just the controller
	@Override
	public void updateRiderPosition() {
		if (this.isDead || this.motionY <= 0) {
			return;
		}

		Set<Entity> potentialEntities = new HashSet<Entity>();
		potentialEntities.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	this,
																				this.getBoundingBox().offset(	0,
																												.1,
																												0)));
		for (Entity entity : potentialEntities) {
			if (!(entity instanceof EntityElevator)
				&& !this.confirmedRiders.contains(entity.entityId)
				&& entity.boundingBox.minY <= this.getBoundingBox().maxY) {
				entity.motionY = Math.max(	this.posY
													+ this.getMountedYOffset()
													- entity.boundingBox.minY,
											entity.motionY);
				entity.onGround = true;
				entity.fallDistance = 0;
				this.confirmedRiders.add(entity.entityId);
			}
		}
		if (!confirmedRiders.isEmpty()) {
			Set<Integer> removedRiders = new HashSet<Integer>();
			for (Integer entityID : confirmedRiders) {
				Entity rider = this.worldObj.getEntityByID(entityID);
				if (rider != null) {
					if ((rider.boundingBox.maxX >= this.getBoundingBox().minX || rider.boundingBox.minX <= this.getBoundingBox().maxX)
						&& (rider.boundingBox.maxZ >= this.getBoundingBox().minZ || rider.boundingBox.minZ <= this.getBoundingBox().maxZ)
						&& rider.boundingBox.minY <= (this.posY
														+ this.getMountedYOffset() + .05)) {
						rider.motionY = Math.max(	this.posY
															+ this.getMountedYOffset()
															- rider.boundingBox.minY,
													rider.motionY);
						rider.onGround = true;
						rider.fallDistance = 0;
					} else {
						removedRiders.add(entityID);
					}
				}
			}
			if (!removedRiders.isEmpty()) {
				this.confirmedRiders.removeAll(removedRiders);
			}
		}
	}

	@Override
	public double getMountedYOffset() {
		return 0.55D;
	}

	public void setProperties(int destination, String destinationName, ChunkCoordinates computer, boolean haltable, int controlerID) {
		this.getDataWatcher().updateObject(	2,
											destination);
		destFloorName = destinationName;

		this.computerPos = computer;
		this.canBeHalted = haltable;

		iscontrolerElevator = (controlerID == this.entityId);

		waitToAccelerate = 0;

		if (!iscontrolerElevator) {
			this.controlingElevatorID = controlerID;
			this.getControler().conjoinedelevators.add(this.entityId);
		}
	}

	private void removeElevatorBlock(int x, int y, int z) {
		if (worldObj.getBlockId(x,
								y,
								z) == blockID
			&& worldObj.getBlockMetadata(	x,
											y,
											z) == this.blockMeta) {
			if (this.enableMobilePower) {
				worldObj.setBlock(	x,
									y,
									z,
									blockID,
									1,
									3);
			} else {
				worldObj.setBlockToAir(	x,
										y,
										z);
			}

		}

	}

	private void setTransitBlocks(int x, int y, int z) {

		if (this.motionY > 0) {
			x = (int) Math.ceil(posX - 0.5);
			y = (int) Math.ceil(posY - 0.5);
			z = (int) Math.ceil(posZ - 0.5);
		} else {
			x = (int) Math.floor(posX - 0.5);
			y = (int) Math.floor(posY - 0.5);
			z = (int) Math.floor(posZ - 0.5);
		}

		if (worldObj.isAirBlock(x,
								y,
								z)) {
			worldObj.setBlock(	x,
								y,
								z,
								blockID,
								1,
								3);
		}

	}

	// only function that abbsolutly needs to keep track of elevators
	public void setEmerHalt(boolean newhalt) {
		if (!this.canBeHalted && newhalt) {
			return;
		}
		emerHalt = newhalt;

		if (emerHalt) {
			motionY = 0;
		}

		if (this.getIsControlerElevator()) {

			Iterator<Integer> iter = conjoinedelevators.iterator();
			while (iter.hasNext()) {
				EntityElevator curElevator = (EntityElevator) this.worldObj.getEntityByID(iter.next());
				if (curElevator != this && curElevator.emerHalt != emerHalt) {
					curElevator.setEmerHalt(emerHalt);
				}
			}
		} else if (getControler() != null
					&& getControler().emerHalt != emerHalt) {
			getControler().setEmerHalt(emerHalt);
		}
	}

	private void killAllConjoined() {
		Iterator<Integer> iter = this.conjoinedelevators.iterator();
		while (iter.hasNext()) {
			EntityElevator curElevator = (EntityElevator) this.worldObj.getEntityByID(iter.next());
			if (curElevator != null) curElevator.setDead();
		}
		this.setDead();
		if (iscontrolerElevator) {
			TileEntityElevatorComputer comTile = this.getParentElevatorComputer();
			if (comTile != null) {

				comTile.elevatorArrived(MathHelper.floor_double(this.posY),
										iscontrolerElevator);
			}

		}
	}

	// Used to get isControler on both client and server
	private boolean getIsControlerElevator() {
		return this.iscontrolerElevator
				|| (this.controlingElevatorID == this.entityId);
	}

	private EntityElevator getControler() {
		return ((EntityElevator) this.worldObj.getEntityByID(this.controlingElevatorID));
	}

	// used to get access to the elevators computer
	public TileEntityElevatorComputer getParentElevatorComputer() {
		TileEntity tile = this.computerPos == null ? null : this.worldObj.getBlockTileEntity(	this.computerPos.posX,
																								this.computerPos.posY,
																								this.computerPos.posZ);
		if (tile == null) {
		} else if (!(tile instanceof TileEntityElevatorComputer)) {
			tile = null;
		}

		return (TileEntityElevatorComputer) tile;
	}

}
