package slimevoid.dynamictransport.tileentity;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.blocks.BlockBase;
import slimevoidlib.tileentity.TileEntityBase;
import slimevoidlib.util.helpers.ItemHelper;

public abstract class TileEntityTransportBase extends TileEntityBase {
	private final Random	random		= new Random();
	protected ItemStack		camoItem;
	protected String		owner;
	protected Privacy		privacyLvl	= Privacy.Public;

	public enum Privacy {
		Public,
		Restricted,
		Private
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

		if (nbttagcompound.hasKey("CamoItem")) {
			this.camoItem = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem"));
		} else {
			this.camoItem = null;
		}

		owner = nbttagcompound.getString("Owner");

		this.privacyLvl = Privacy.values()[nbttagcompound.getInteger("PrivacyLvl")];
	}

	@Override
	public float getBlockHardness(BlockBase blockBase) {
		return 1.0f; // TODO :: Real Block Hardness
	}

	public ItemStack getCamoItem() {
		return this.camoItem;
	}

	protected void setCamoItem(ItemStack itemstack) {
		this.camoItem = itemstack;
		this.camoItem.stackSize = 1;
		this.updateBlockChange();
	}

	protected void removeCamoItem() {
		ItemHelper.dropItem(this.getWorldObj(),
							this.xCoord,
							this.yCoord,
							this.zCoord,
							this.camoItem);
		this.camoItem = null;
		this.updateBlockChange();
	}

}
