package slimevoid.dynamictransport.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;

public class TileEntityElevator extends TileEntityTransportBase {

	private ChunkCoordinates	ParentElevatorComputer;

	private int					maxY	= -1;
	private int					minY	= -1;

	public int getMaxY(boolean reCalculate) {
		if (reCalculate || maxY == -1) {
			for (int y = this.yCoord; y < this.worldObj.getActualHeight(); y++) {
				if (!this.worldObj.isAirBlock(	this.xCoord,
												y + 1,
												this.zCoord)
					|| y == this.worldObj.getActualHeight()) {
					this.maxY = y;
					break;
				}
			}
		}

		return maxY;
	}

	public int getMinY(boolean reCalculate) {
		if (reCalculate || minY == -1) {
			for (int y = this.yCoord; y >= 0; y--) {
				if (!this.worldObj.isAirBlock(	this.xCoord,
												y - 1,
												this.zCoord) || y == 0) {
					this.minY = y;
					break;
				}
			}
		}

		return minY;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getHeldItem() != null
			&& entityplayer.getHeldItem().itemID == ConfigurationLib.itemElevatorTool.itemID) {
			if (this.worldObj.isRemote) {
				return true;
			}
			NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
			if (tags.hasKey("ComputerX")) {
				setParentElevatorComputer(new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ")));
			}
		}
		return false;
	}

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation) {
		if ((this.ParentElevatorComputer == null || !this.ParentElevatorComputer.equals(ComputerLocation))
			&& this.worldObj.getBlockId(ComputerLocation.posX,
										ComputerLocation.posY,
										ComputerLocation.posZ) == ConfigurationLib.blockTransportBase.blockID
			&& this.worldObj.getBlockMetadata(	ComputerLocation.posX,
												ComputerLocation.posY,
												ComputerLocation.posZ) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {
			this.ParentElevatorComputer = ComputerLocation;
			TileEntityElevatorComputer comTile = (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(	ComputerLocation.posX,
																												ComputerLocation.posY,
																												ComputerLocation.posZ);
			comTile.addElevator(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
		}
	}

	public ChunkCoordinates getParentElevatorComputer() {
		return this.ParentElevatorComputer;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (ParentElevatorComputer != null) {
			nbttagcompound.setInteger(	"ParentElevatorComputerX",
										ParentElevatorComputer.posX);
			nbttagcompound.setInteger(	"ParentElevatorComputerY",
										ParentElevatorComputer.posY);
			nbttagcompound.setInteger(	"ParentElevatorComputerZ",
										ParentElevatorComputer.posZ);
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.ParentElevatorComputer = new ChunkCoordinates(nbttagcompound.getInteger("ParentElevatorComputerX"), nbttagcompound.getInteger("ParentElevatorComputerY"), nbttagcompound.getInteger("ParentElevatorComputerZ"));

	}

}
