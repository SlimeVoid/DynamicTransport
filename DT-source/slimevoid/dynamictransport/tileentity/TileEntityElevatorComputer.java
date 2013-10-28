package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ChunkCoordinates;

public class TileEntityElevatorComputer extends TileEntityTransportBase {
	public class XZCoords {
		public int	x;
		public int	z;

		XZCoords(int x, int z) {
			this.x = x;
			this.z = z;
		}

	}

	public enum ElevatorMode {
		Maintenance, Transit, Available
	}

	private List<ChunkCoordinates>	BoundMarkerBlocks	= new ArrayList<ChunkCoordinates>();
	private List<XZCoords>			BoundElevatorBLocks	= new ArrayList<XZCoords>();
	private int						elevatorPos;
	private ElevatorMode			Mode				= ElevatorMode.Available;

	// cached results
	private int						MinY;
	private int						MaxY;

}
