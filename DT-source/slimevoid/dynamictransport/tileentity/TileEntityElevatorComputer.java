package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.util.XZCoords;

public class TileEntityElevatorComputer extends TileEntityTransportBase {

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

	public void addElevator(ChunkCoordinates elevator) {

		if (!boundElevatorBlocks.contains(new XZCoords(elevator.posX, elevator.posZ))
			&& elevator.posY - 1 == this.yCoord) {
			if (MathHelper.sqrt_double(Math.pow((double) this.xCoord
														- (double) elevator.posX,
												2)
										+ Math.pow(	(double) this.zCoord
															- (double) elevator.posZ,
													2)) <= 3) {
				if (this.worldObj.getBlockId(	elevator.posX,
												elevator.posY,
												elevator.posZ) == ConfigurationLib.blockTransportBase.blockID
					&& this.worldObj.getBlockMetadata(	elevator.posX,
														elevator.posY,
														elevator.posZ) == BlockLib.BLOCK_ELEVATOR_ID) {
					this.boundElevatorBlocks.add(new XZCoords(elevator.posX, elevator.posZ));
				}

			} else {
				if (boundElevatorBlocks.size() != 0) {

					for (XZCoords boundBlock : boundElevatorBlocks) {
						if (MathHelper.sqrt_double(Math.pow((double) boundBlock.x
																	- (double) elevator.posX,
															2)
													+ Math.pow(	(double) boundBlock.z
																		- (double) elevator.posZ,
																2)) <= 3) {
							if (this.worldObj.getBlockId(	elevator.posX,
															elevator.posY,
															elevator.posZ) == ConfigurationLib.blockTransportBase.blockID
								&& this.worldObj.getBlockMetadata(	elevator.posX,
																	elevator.posY,
																	elevator.posZ) == BlockLib.BLOCK_ELEVATOR_ID) {
								this.boundElevatorBlocks.add(new XZCoords(elevator.posX, elevator.posZ));
							}
						}
					}
				}
			}
		}

	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getHeldItem() != null
			&& entityplayer.getHeldItem().itemID == ConfigurationLib.itemElevatorTool.itemID) {
			if (this.worldObj.isRemote) {
				return true;
			}
			if (entityplayer.isSneaking()) {
				ItemStack heldItem = entityplayer.getHeldItem();
				NBTTagCompound tags = new NBTTagCompound();
				if (this.curTechnicianName == entityplayer.username) {
					this.curTechnicianName = "";
					this.mode = ElevatorMode.Available;
				} else if (this.curTechnicianName == null
							|| this.curTechnicianName.isEmpty()) {
					tags.setInteger("ComputerX",
									this.xCoord);
					tags.setInteger("ComputerY",
									this.yCoord);
					tags.setInteger("ComputerZ",
									this.zCoord);
					this.curTechnicianName = entityplayer.username;
					// Move Elevator for Maintenance
					this.CallElevator(	this.yCoord + 1,
										true);
				}
				heldItem.setTagCompound(tags);
			} else {
				// open GUI
			}
		}
		return false;
	}

	private void CallElevator(int i) {
		this.CallElevator(	i,
							false);
	}

	private void CallElevator(int i, boolean forMaintenance) {
		// TODO Auto-generated method stub
		if (forMaintenance) {// will be replaced once we get some moving parts
			this.mode = ElevatorMode.Maintenance;
		}
	}

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
