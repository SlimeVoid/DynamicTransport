package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
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

	// Persistent Data
	private String					elevatorName;
	private List<ChunkCoordinates>	boundMarkerBlocks	= new ArrayList<ChunkCoordinates>();
	private List<XZCoords>			boundElevatorBlocks	= new ArrayList<XZCoords>();
	private List<Integer>			floorSpool			= new ArrayList<Integer>();
	private ElevatorMode			mode				= ElevatorMode.Available;
	private String					curTechnicianName;

	// cached results
	private int						elevatorPos;
	private int						MinY;
	private int						MaxY;

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		// convert lists into arrays
		int BoundMarkerBlocksX[] = new int[boundMarkerBlocks.size()];
		int BoundMarkerBlocksY[] = new int[boundMarkerBlocks.size()];
		int BoundMarkerBlocksZ[] = new int[boundMarkerBlocks.size()];
		for (int i = 0; i < boundMarkerBlocks.size(); i++) {
			BoundMarkerBlocksX[i] = boundMarkerBlocks.get(i).posX;
			BoundMarkerBlocksY[i] = boundMarkerBlocks.get(i).posY;
			BoundMarkerBlocksZ[i] = boundMarkerBlocks.get(i).posZ;
		}
		int BoundElevatorBlocksX[] = new int[boundElevatorBlocks.size()];
		int BoundElevatorBlocksZ[] = new int[boundElevatorBlocks.size()];
		for (int i = 0; i < boundElevatorBlocks.size(); i++) {
			BoundElevatorBlocksX[i] = boundElevatorBlocks.get(i).x;
			BoundElevatorBlocksZ[i] = boundElevatorBlocks.get(i).z;
		}
		int tempSpool[] = new int[floorSpool.size()];
		for (int i = 0; i < floorSpool.size(); i++) {
			tempSpool[i] = floorSpool.get(i);
		}
		if (elevatorName != null && !elevatorName.isEmpty()) nbttagcompound.setString(	"ElevatorName",
																						elevatorName);
		nbttagcompound.setIntArray(	"BoundMarkerBlocksX",
									BoundMarkerBlocksX);
		nbttagcompound.setIntArray(	"BoundMarkerBlocksY",
									BoundMarkerBlocksY);
		nbttagcompound.setIntArray(	"BoundMarkerBlocksZ",
									BoundMarkerBlocksZ);
		nbttagcompound.setIntArray(	"BoundElevatorBlocksX",
									BoundElevatorBlocksX);
		nbttagcompound.setIntArray(	"BoundElevatorBlocksZ",
									BoundElevatorBlocksZ);
		nbttagcompound.setIntArray(	"FloorSpool",
									tempSpool);
		nbttagcompound.setInteger(	"Mode",
									mode.ordinal());
		if (curTechnicianName != null && !curTechnicianName.isEmpty()) nbttagcompound.setString("CurTechnicianName",
																								curTechnicianName);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		int BoundMarkerBlocksX[] = nbttagcompound.getIntArray("BoundMarkerBlocksX");
		int BoundMarkerBlocksY[] = nbttagcompound.getIntArray("BoundMarkerBlocksY");
		int BoundMarkerBlocksZ[] = nbttagcompound.getIntArray("BoundMarkerBlocksZ");
		int BoundElevatorBlocksX[] = nbttagcompound.getIntArray("BoundElevatorBlocksX");
		int BoundElevatorBlocksZ[] = nbttagcompound.getIntArray("BoundElevatorBlocksZ");
		int tempSpool[] = nbttagcompound.getIntArray("FloorSpool");

		for (int i = 0; i < BoundMarkerBlocksX.length; i++) {
			boundMarkerBlocks.add(new ChunkCoordinates(BoundMarkerBlocksX[i], BoundMarkerBlocksY[i], BoundMarkerBlocksZ[i]));
		}
		for (int i = 0; i < BoundElevatorBlocksX.length; i++) {
			boundElevatorBlocks.add(new XZCoords(BoundElevatorBlocksX[i], BoundElevatorBlocksZ[i]));
		}
		for (int i = 0; i < tempSpool.length; i++) {
			this.floorSpool.add(tempSpool[i]);
		}

		this.mode = ElevatorMode.values()[nbttagcompound.getInteger("Mode")];
		this.curTechnicianName = nbttagcompound.getString("CurTechnicianName");

		elevatorName = nbttagcompound.getString("ElevatorName");
	}

}
