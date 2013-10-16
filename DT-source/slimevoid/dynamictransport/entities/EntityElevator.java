package slimevoid.dynamictransport.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityElevator extends Entity implements IEntityMultiPart {

	private byte					stillcount				= 0;
	private byte					waitToAccelerate		= 0;
	public int						dest;
	private double					targetY;
	private boolean					atDestination;
	boolean							unUpdated;

	private float					elevatorSpeed			= 0.0F;
	private static final float		elevatorAccel			= 0.01F;
	private static final float		maxElevatorSpeed		= 0.4F;
	private static final float		minElevatorMovingSpeed	= 0.016F;

	public boolean					emerHalt				= false;
	public int						startStops				= 0;

	public int						tickcount				= 0;

	private boolean					slowingDown				= false;
	private EntityElevatorPart[]	ElevatorBlockArray;

	public EntityElevator(World world) {
		super(world);
		this.ElevatorBlockArray = new EntityElevatorPart[] {};
		this.setSize(	0.98F,
						0.98F);
		this.noClip = true;
		this.isImmuneToFire = true;
		this.targetY = 100.0D;
		this.ignoreFrustumCheck = true;
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
		if (this.worldObj.isRemote) {

		} else {
			this.setPosition(	this.posX,
								this.posY + .016,
								this.posZ);
			for (EntityElevatorPart part : ElevatorBlockArray) {
				part.onUpdate();
				part.setPosition(	part.posX,
									this.posY,
									part.posZ);
			}
		}
	}
}
