package slimevoid.dynamictransport.tileentity;

import net.minecraft.util.ChunkCoordinates;

public class TileEntityElevator extends TileEntityTransportBase {

	private ChunkCoordinates	ParentElevatorComputer;

	private int					maxY;
	private int					minY;

	public int getMaxY(boolean reCalculate) {
		if (reCalculate) {
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
		if (reCalculate) {
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

}
