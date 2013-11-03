package slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

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

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation) {
		this.ParentElevatorComputer = ComputerLocation;
	}

	public ChunkCoordinates getParentElevatorComputer() {
		return this.ParentElevatorComputer;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger(	"ParentElevatorComputerX",
									ParentElevatorComputer.posX);
		nbttagcompound.setInteger(	"ParentElevatorComputerY",
									ParentElevatorComputer.posY);
		nbttagcompound.setInteger(	"ParentElevatorComputerZ",
									ParentElevatorComputer.posZ);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.ParentElevatorComputer = new ChunkCoordinates(nbttagcompound.getInteger("ParentElevatorComputerX"), nbttagcompound.getInteger("ParentElevatorComputerY"), nbttagcompound.getInteger("ParentElevatorComputerZ"));

	}

}
