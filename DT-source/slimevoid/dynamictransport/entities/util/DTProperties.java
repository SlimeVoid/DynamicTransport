package slimevoid.dynamictransport.entities.util;

import net.minecraft.util.ChunkCoordinates;

public class DTProperties {

	private String				elevatorName		= "";
	private String				destFloorName		= "";
	private boolean				canBeHalted			= true;
	private boolean				enableMobilePower	= false;
	private ChunkCoordinates	computerPos			= null;

	public String getElevatorName() {
		return elevatorName;
	}

	public boolean getCanHalt() {
		return canBeHalted;
	}

	public boolean getMobilePower() {
		return enableMobilePower;
	}

}
