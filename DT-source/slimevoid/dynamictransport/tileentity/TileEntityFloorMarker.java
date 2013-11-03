package slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class TileEntityFloorMarker extends TileEntityTransportBase {

	private ChunkCoordinates	parentTransportComputer;
	private int					floorYLvl	= -1;
	private String				floorName;

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation) {
		this.parentTransportComputer = ComputerLocation;
	}

	public ChunkCoordinates getParentElevatorComputer() {
		return this.parentTransportComputer;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger(	"ParentTransportComputerX",
									parentTransportComputer.posX);
		nbttagcompound.setInteger(	"ParentTransportComputerY",
									parentTransportComputer.posY);
		nbttagcompound.setInteger(	"ParentTransportComputerZ",
									parentTransportComputer.posZ);
		nbttagcompound.setString(	"FloorName",
									floorName);
		nbttagcompound.setInteger(	"FloorYLvl",
									floorYLvl);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.parentTransportComputer = new ChunkCoordinates(nbttagcompound.getInteger("ParentTransportComputerX"), nbttagcompound.getInteger("ParentTransportComputerY"), nbttagcompound.getInteger("ParentTransportComputerZ"));
		this.floorName = nbttagcompound.getString("FloorName");
		this.floorYLvl = nbttagcompound.getInteger("FloorYLvl");
	}

}
