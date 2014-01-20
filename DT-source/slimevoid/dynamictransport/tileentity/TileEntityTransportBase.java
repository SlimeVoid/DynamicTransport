package slimevoid.dynamictransport.tileentity;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.tileentity.TileEntityBase;

public class TileEntityTransportBase extends TileEntityBase {
	private final Random	random		= new Random();
	protected ItemStack		camoItem;
	protected String		owner;
	protected Privacy		privacyLvl	= Privacy.Public;

	public enum Privacy {
		Public, Restricted, Private
	}

	@Override
	public int getBlockID() {
		return ConfigurationLib.blockTransportBaseID;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (this.camoItem != null) {
			NBTTagCompound itemNBTTagCompound = new NBTTagCompound();
			this.camoItem.writeToNBT(itemNBTTagCompound);

			nbttagcompound.setTag(	"CamoItem",
									itemNBTTagCompound);
		}

		if (owner != null && !owner.isEmpty()) nbttagcompound.setString("Owner",
																		owner);
		nbttagcompound.setInteger(	"PrivacyLvl",
									privacyLvl.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		this.camoItem = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem"));

		owner = nbttagcompound.getString("Owner");

		this.privacyLvl = Privacy.values()[nbttagcompound.getInteger("PrivacyLvl")];
	}

	@Override
	public int getExtendedBlockID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ItemStack getCamoItem() {
		return this.camoItem;
	}

	protected void setCamoItem(ItemStack itemstack) {
		this.camoItem = itemstack;
		this.camoItem.stackSize = 1;

	}

	protected void removeCamoItem() {
		float f = this.random.nextFloat() * 0.8F + 0.1F;
		float f1 = this.random.nextFloat() * 0.8F + 0.1F;
		float f2 = this.random.nextFloat() * 0.8F + 0.1F;
		EntityItem entityitem = new EntityItem(this.worldObj, (double) ((float) this.xCoord + f), (double) ((float) this.yCoord + f1), (double) ((float) this.zCoord + f2), new ItemStack(this.camoItem.itemID, 1, this.camoItem.getItemDamage()));
		float f3 = 0.05F;
		entityitem.motionX = (double) ((float) this.random.nextGaussian() * f3);
		entityitem.motionY = (double) ((float) this.random.nextGaussian() * f3 + 0.2F);
		entityitem.motionZ = (double) ((float) this.random.nextGaussian() * f3);
		this.worldObj.spawnEntityInWorld(entityitem);
		this.camoItem = null;
	}

}
