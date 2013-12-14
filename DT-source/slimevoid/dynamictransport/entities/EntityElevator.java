package slimevoid.dynamictransport.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.network.packet.PacketEntityMotionUpdate;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import cpw.mods.fml.common.network.PacketDispatcher;

public class EntityElevator extends Entity {

	private static final int	blockID					= ConfigurationLib.blockTransportBaseID;
	private static final int	blockMeta				= BlockLib.BLOCK_ELEVATOR_ID;
	private byte				stillcount				= 0;
	private byte				waitToAccelerate		= 0;
	public int					dest;

	private float				elevatorSpeed			= 0.0F;
	private static final float	elevatorAccel			= 0.01F;
	private static final float	maxElevatorSpeed		= 0.4F;
	private static final float	minElevatorMovingSpeed	= 0.016F;

	private String				elevatorName			= "";
	private String				destFloorName			= "";
	private boolean				canBeHalted				= true;
	private boolean				enableMobilePower		= false;
	private ChunkCoordinates	computerPos				= null;

	public boolean				emerHalt				= false;
	public int					startStops				= 0;

	public int					tickcount				= 0;

	public EntityElevator		controlingElevator		= null;
	public Set<EntityElevator>	conjoinedelevators		= new HashSet<EntityElevator>();
	public boolean				iscontrolerElevator		= false;

	private boolean				isClient;

	private boolean				slowingDown				= false;
	private double				tmpControlerZ;
	private double				tmpControlerX;

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

		riddenByEntity = null;

		waitToAccelerate = 100;
		controlingElevator = this;
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

		dest = (int) j;

		isClient = true;
		iscontrolerElevator = false;

		waitToAccelerate = 0;

		this.dataWatcher.updateObject(	17,
										0);
	}

	public void setProperties(int destination, String destinationName, boolean isCenter, boolean local, int meta, ChunkCoordinates computer, boolean haltable, EntityElevator elevatorCenter) {

		dest = destination;
		destFloorName = destinationName;

		this.computerPos = computer;
		this.canBeHalted = haltable;

		isClient = local;
		iscontrolerElevator = isCenter;

		waitToAccelerate = 0;

		this.dataWatcher.updateObject(	17,
										meta);

		if (!iscontrolerElevator) {
			this.controlingElevator = elevatorCenter;
			controlingElevator.conjoinedelevators.add(this);
		}
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return true;
	}

	public void setEmerHalt(boolean newhalt) {
		if (!this.canBeHalted && newhalt) {
			return;
		}
		if (!isClient) {
			return;
		}
		emerHalt = newhalt;

		if (emerHalt) {
			motionY = 0;
			elevatorSpeed = 0;
		}

		if (iscontrolerElevator) {

			Iterator<EntityElevator> iter = conjoinedelevators.iterator();
			while (iter.hasNext()) {
				EntityElevator curElevator = iter.next();
				if (curElevator != this && curElevator.emerHalt != emerHalt) {
					curElevator.setEmerHalt(emerHalt);
				}
			}
		} else if (controlingElevator.emerHalt != emerHalt) {
			controlingElevator.setEmerHalt(emerHalt);
		}
	}

	@Override
	public void mountEntity(Entity entity) {

	}

	@Override
	public void updateRiderPosition() {
		if (this.isDead || !iscontrolerElevator) {
			return;
		}
		Iterator<EntityElevator> elevators = conjoinedelevators.iterator();
		while (elevators.hasNext()) {
			EntityElevator curElevator = elevators.next();
			AxisAlignedBB boundBox = curElevator.getBoundingBox();
			boundBox.minY += .5;
			boundBox.maxY += .2;
			Set<Entity> potentialEntities = new HashSet<Entity>();
			potentialEntities.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	this,
																					boundBox));
			Iterator<Entity> iter = potentialEntities.iterator();
			while (iter.hasNext()) {
				Entity entity = iter.next();
				Set<Entity> checkedEntities = new HashSet<Entity>();
				if (entity != null && !(entity instanceof EntityElevator)
					&& !checkedEntities.contains(entity)) {
					if (entity.ridingEntity == null
						|| entity.ridingEntity == this) {
						updateRider(entity);
						checkedEntities.add(entity);
					}
				}
			}
		}
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

		if (rider.canBePushed() && !(rider instanceof EntityElevator)) {

			if (this.motionY > 0) {
				rider.motionY = Math.max(	Math.max(	rider.motionY,
														this.motionY),
											0);
				PacketEntityMotionUpdate packet = new PacketEntityMotionUpdate(rider.motionY, rider.entityId);
				PacketDispatcher.sendPacketToAllAround(	rider.posX,
														rider.posY,
														rider.posZ,
														400,
														rider.worldObj.getWorldInfo().getVanillaDimension(),
														packet.getPacket());
				rider.posY = this.posY + 1.2 + rider.getYOffset();
			}

			rider.onGround = true;
			rider.fallDistance = 0;
		}

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

		if (this.ticksExisted > 45) {
			startStops--;
			if (startStops < 0) {
				startStops = 0;
			}
			this.ticksExisted = 0;
		}

		if (this.ticksExisted == 1) {
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
		if (!iscontrolerElevator) {

			if (controlingElevator != null && !controlingElevator.isDead) {

				this.setPosition(	this.posX,
									controlingElevator.posY,
									this.posZ);
			} else if (!this.isDead) {
				this.killElevator();
			}
			return;
		}

		float range = 0.0F;
		float destY = dest + 0.5F;
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
		this.setPosition(	this.posX,
							this.posY + this.motionY,
							this.posZ);

		updateRiderPosition();
		if (!emerHalt) {
			if (MathHelper.abs((float) motionY) < minElevatorMovingSpeed
				&& stillcount++ > 10) {
				killAllConjoined();
			} else {
				stillcount = 0;
			}
		}
	}

	private void killAllConjoined() {
		Iterator<EntityElevator> iter = this.conjoinedelevators.iterator();
		while (iter.hasNext()) {
			EntityElevator curElevator = iter.next();
			curElevator.killElevator();
		}
		if (this.iscontrolerElevator) {
			TileEntityElevatorComputer comTile = this.getParentElevatorComputer();
			if (comTile != null) {
				comTile.elevatorArrived(MathHelper.floor_double(this.posY),
										iscontrolerElevator);
			}

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
		AxisAlignedBB boundBox = this.getBoundingBox().expand(	0,
																2.0,
																0);
		// boundBox.minY += 1;

		Set<Entity> potentialEntities = new HashSet<Entity>();
		potentialEntities.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	this,
																				boundBox));
		Iterator<Entity> iter = potentialEntities.iterator();
		while (iter.hasNext()) {
			Entity entity = iter.next();
			Set<Entity> checkedEntities = new HashSet<Entity>();
			if (entity != null && !(entity instanceof EntityElevator)
				&& !checkedEntities.contains(entity)) {
				if (entity.ridingEntity == null) {
					entity.moveEntity(	0,
										(Math.floor(controlingElevator.posY)
											+ entity.yOffset + 1)
												- entity.posY,
										0);
					checkedEntities.add(entity);
				}
			}
		}

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
																																			|| destFloorName.trim().isEmpty() ? this.dest : this.destFloorName))));

			}

			setDead();
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

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger(	"destY",
									dest);
		if (this.destFloorName != null && !this.destFloorName.trim().isEmpty()) nbttagcompound.setString(	"destName",
																											this.destFloorName);
		nbttagcompound.setBoolean(	"emerHalt",
									emerHalt);
		nbttagcompound.setBoolean(	"isClient",
									isClient);
		nbttagcompound.setBoolean(	"isCenter",
									iscontrolerElevator);
		nbttagcompound.setInteger(	"metadata",
									dataWatcher.getWatchableObjectInt(17));
		nbttagcompound.setDouble(	"controlerX",
									this.controlingElevator.posX);
		nbttagcompound.setDouble(	"controlerZ",
									this.controlingElevator.posZ);
		nbttagcompound.setInteger(	"ComputerX",
									this.computerPos.posX);
		nbttagcompound.setInteger(	"ComputerY",
									this.computerPos.posY);
		nbttagcompound.setInteger(	"ComputerZ",
									this.computerPos.posZ);

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
		iscontrolerElevator = nbttagcompound.getBoolean("isCenter");
		if (!conjoinedelevators.contains(this)) {
			conjoinedelevators.add(this);
		}
		setPosition(posX,
					posY,
					posZ);

		this.tmpControlerX = nbttagcompound.getDouble("controlerX");
		this.tmpControlerZ = nbttagcompound.getDouble("controlerZ");

		this.computerPos = new ChunkCoordinates(nbttagcompound.getInteger("ComputerX"), nbttagcompound.getInteger("ComputerY"), nbttagcompound.getInteger("ComputerZ"));

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

	public TileEntityElevatorComputer getParentElevatorComputer() {
		TileEntity tile = this.computerPos == null ? null : this.worldObj.getBlockTileEntity(	this.computerPos.posX,
																								this.computerPos.posY,
																								this.computerPos.posZ);
		if (tile == null) {
			// parentTransportComputer = null;
		} else if (!(tile instanceof TileEntityElevatorComputer)) {
			tile = null;
			// parentTransportComputer = null;
		}

		return (TileEntityElevatorComputer) tile;
	}

	public void applyEntityCollision(Entity par1Entity) {
		if (par1Entity.canBePushed() && !(par1Entity instanceof EntityElevator)) {

			if (this.motionY > 0) {
				par1Entity.motionY = Math.max(	Math.max(	par1Entity.motionY,
															this.motionY + .1),
												0);
				PacketEntityMotionUpdate packet = new PacketEntityMotionUpdate(par1Entity.motionY, par1Entity.entityId);
				PacketDispatcher.sendPacketToAllAround(	par1Entity.posX,
														par1Entity.posY,
														par1Entity.posZ,
														400,
														par1Entity.worldObj.getWorldInfo().getVanillaDimension(),
														packet.getPacket());
			}
			par1Entity.onGround = true;
			par1Entity.fallDistance = 0;

		}
	}
}
