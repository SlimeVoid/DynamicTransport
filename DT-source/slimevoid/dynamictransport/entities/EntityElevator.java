package slimevoid.dynamictransport.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;

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

		if (this.getIsControlerElevator()) {

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
		if (this.isDead || !getIsControlerElevator()) {
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
					&& !(entity instanceof EntityPlayer)
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

		if (rider.canBePushed() && !(rider instanceof EntityElevator)
			&& !(rider instanceof EntityPlayer)) {

			if (this.motionY > 0) {
				rider.motionY = Math.max(	Math.max(	rider.motionY,
														this.motionY),
											0);
				rider.posY = this.posY + getMountedYOffset()
								+ rider.getYOffset();
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
		super.onUpdate();

		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		if (this.motionY < this.maxElevatorSpeed) {
			this.addVelocity(	0,
								this.elevatorAccel,
								0);
		}
		this.posY = this.posY + this.motionY;

		Set<Entity> potentialEntities = new HashSet<Entity>();
		AxisAlignedBB scanbox = this.getBoundingBox();
		scanbox.offset(	0,
						.5,
						0);
		potentialEntities.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(	this,
																				scanbox));
		for (Entity entity : potentialEntities) {
			if (!(entity instanceof EntityElevator)) {
				if (this.getBoundingBox().maxY + .025 - entity.boundingBox.minY >= 0) {
					entity.motionY = Math.max(	this.getBoundingBox().maxY
														+ .025
														- entity.boundingBox.minY,
												entity.motionY);
					entity.onGround = true;
					entity.fallDistance = 0;
					return;
				}
			}
		}
	}

	private boolean getIsControlerElevator() {
		// TODO Auto-generated method stub
		return this.iscontrolerElevator
				|| (this.controlingElevator != null && this.entityId == this.controlingElevator.entityId);
	}

	private void killAllConjoined() {
		Iterator<EntityElevator> iter = this.conjoinedelevators.iterator();
		while (iter.hasNext()) {
			EntityElevator curElevator = iter.next();
			curElevator.killElevator();
		}
		if (iscontrolerElevator) {
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
															this.motionY),
												0);
			}
			par1Entity.onGround = true;
			par1Entity.fallDistance = 0;

		}
	}
}
