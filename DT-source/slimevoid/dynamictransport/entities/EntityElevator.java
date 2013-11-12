package slimevoid.dynamictransport.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;

public class EntityElevator extends Entity implements IEntityMultiPart {

	// counts how long an elevator is at a position
	private byte					stillcount				= 0;
	// used while setting up the entity
	private byte					waitToAccelerate		= 0;

	// the y coord that the elevator will try to get to
	private double					targetY;

	private static final float		elevatorAccel			= 0.01F;
	private static final float		maxElevatorSpeed		= 0.4F;
	private static final float		minElevatorMovingSpeed	= 0.016F;

	// set to true when within
	private boolean					slowingDown				= false;
	// this holds all the actual elevators to be rendered
	private EntityElevatorPart[]	ElevatorBlockArray;

	public EntityElevator(World world, int calledFloor, ChunkCoordinates Computer, ChunkCoordinates elevators[]) {
		super(world);
		this.ElevatorBlockArray = new EntityElevatorPart[elevators.length];
		this.setSize(	0F,
						0F);
		this.noClip = true;
		this.isImmuneToFire = true;
		this.targetY = calledFloor + 0.5D;
		this.ignoreFrustumCheck = true;
		this.preventEntitySpawning = true;
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public World func_82194_d() {
		return this.worldObj;
	}

	@Override
	public boolean attackEntityFromPart(EntityDragonPart entitydragonpart, DamageSource damagesource, float f) {
		return false;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		float elevatorSpeed = (float) (this.motionY + elevatorAccel);
		if (elevatorSpeed > maxElevatorSpeed) {
			elevatorSpeed = maxElevatorSpeed;
		}
		// Calculate elevator range to break
		float range = (elevatorSpeed * elevatorSpeed - minElevatorMovingSpeed
														* minElevatorMovingSpeed)
						/ (2 * elevatorAccel);
		if (slowingDown
			|| !(MathHelper.abs((float) (this.targetY - posY)) >= (range))) {
			elevatorSpeed -= elevatorAccel;
			slowingDown = true;
		}
		if (elevatorSpeed > maxElevatorSpeed) {
			elevatorSpeed = maxElevatorSpeed;
		}
		if (elevatorSpeed < minElevatorMovingSpeed) {
			elevatorSpeed = minElevatorMovingSpeed;
		}

		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (!this.worldObj.isRemote) {
			int i = MathHelper.floor_double(this.posX);
			int j = MathHelper.floor_double(this.posY);
			int k = MathHelper.floor_double(this.posZ);
			boolean atDestination = onGround
									|| (MathHelper.abs((float) (this.targetY - posY)) < this.motionY);
			if (!atDestination) {
				motionY = (this.targetY > posY) ? elevatorSpeed : -elevatorSpeed;
				// updateAllConjoined();
			} else if (atDestination) {
				// killAllConjoined();
				return;
			}

			this.moveEntity(this.motionX,
							this.motionY,
							this.motionZ);
			for (EntityElevatorPart part : ElevatorBlockArray) {
				part.onUpdate();
				part.setPosition(	part.posX,
									this.posY,
									part.posZ);
			}
		}

	}

	public void killElevator() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		int blockID = ConfigurationLib.blockTransportBaseID;
		boolean blockPlaced = !worldObj.isRemote
								&& (worldObj.getBlockId(i,
														j,
														k) == blockID || worldObj.canPlaceEntityOnSide(	blockID,
																										i,
																										j,
																										k,
																										true,
																										1,
																										(Entity) null,
																										null)
																			&& worldObj.setBlockMetadataWithNotify(	i,
																													j,
																													k,
																													blockID,
																													BlockLib.BLOCK_ELEVATOR_ID));

		if (!worldObj.isRemote && !blockPlaced) {
			entityDropItem(	new ItemStack(blockID, 1, BlockLib.BLOCK_ELEVATOR_ID),
							0);
		}

		if (!worldObj.isRemote) {
			// send players message floor reached
			setDead();
		}
	}

	private void killAllConjoined() {

		for (EntityElevatorPart curElevator : this.ElevatorBlockArray) {

			curElevator.killElevator();

		}
	}

	public Icon[] getTextureData() {
		// TODO Auto-generated method stub
		return null;
	}

	public World getWorld() {
		// TODO Auto-generated method stub
		return this.worldObj;
	}
}
