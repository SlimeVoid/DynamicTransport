package slimevoid.dynamictransport.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;
import slimevoidlib.blocks.BlockBase;

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
	public boolean isBlockSolidOnSide(BlockBase blockBase, ForgeDirection side) {
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (parentTransportComputer != null) {
			nbttagcompound.setInteger(	"ParentTransportComputerX",
										parentTransportComputer.posX);
			nbttagcompound.setInteger(	"ParentTransportComputerY",
										parentTransportComputer.posY);
			nbttagcompound.setInteger(	"ParentTransportComputerZ",
										parentTransportComputer.posZ);
		}
		if (this.floorName != null && !this.floorName.isEmpty()) nbttagcompound.setString(	"FloorName",
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
