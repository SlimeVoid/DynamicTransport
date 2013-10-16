package slimevoid.dynamictransport.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class EntityElevatorPart extends Entity {
	/** The dragon entity this dragon part belongs to */
	public final IEntityMultiPart	entityElevatorObj;

	/** The name of the Dragon Part */
	public final String				name;

	public EntityElevatorPart(IEntityMultiPart par1IEntityMultiPart, String par2Str, float par3, float par4) {
		super(par1IEntityMultiPart.func_82194_d());
		this.setSize(	par3,
						par4);
		this.entityElevatorObj = par1IEntityMultiPart;
		this.name = par2Str;
	}

	protected void entityInit() {
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
	}

	/**
	 * Returns true if other Entities should be prevented from moving through
	 * this Entity.
	 */
	public boolean canBeCollidedWith() {
		return true;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
		return false;
	}

	/**
	 * Returns true if Entity argument is equal to this Entity
	 */
	public boolean isEntityEqual(Entity par1Entity) {
		return this == par1Entity || this.entityElevatorObj == par1Entity;
	}
}