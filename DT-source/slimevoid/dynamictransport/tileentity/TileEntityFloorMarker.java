package slimevoid.dynamictransport.tileentity;

import net.minecraft.util.ChunkCoordinates;

public class TileEntityFloorMarker extends TileEntityTransportBase {

	private ChunkCoordinates	ParentTransportComputer;

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation) {
		this.ParentTransportComputer = ComputerLocation;
	}

	public ChunkCoordinates getParentElevatorComputer() {
		return this.ParentTransportComputer;
	}
}
