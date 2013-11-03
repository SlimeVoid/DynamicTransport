package slimevoid.dynamictransport.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.tileentity.TileEntityBase;

public class TileEntityTransportBase extends TileEntityBase {

	private ItemStack	camoItem;

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
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		this.camoItem = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem"));
	}
}
