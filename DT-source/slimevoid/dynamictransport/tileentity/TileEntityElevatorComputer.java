package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.entities.EntityElevator;
import slimevoid.dynamictransport.util.XZCoords;
import slimevoidlib.blocks.BlockBase;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityElevatorComputer extends TileEntityTransportBase {

	public enum ElevatorMode {
		Maintenance, Transit, Available
	}

	// Persistent Data
	private String							elevatorName;
	private List<ChunkCoordinates>			boundMarkerBlocks	= new ArrayList<ChunkCoordinates>();
	private List<XZCoords>					boundElevatorBlocks	= new ArrayList<XZCoords>();
	private LinkedHashMap<Integer, String>	floorSpool			= new LinkedHashMap<Integer, String>();
	private ElevatorMode					mode				= ElevatorMode.Available;
	private String							curTechnicianName;
	private int								elevatorPos;
	public boolean							pendingMantinance	= false;

	public boolean addElevator(ChunkCoordinates elevator, EntityPlayer entityplayer) {
		if (this.mode == ElevatorMode.Maintenance
			&& this.curTechnicianName != null
			&& this.curTechnicianName.equals(entityplayer.username)) {
			if (elevator.posY - 1 == this.yCoord) {
				if (!boundElevatorBlocks.contains(new XZCoords(elevator.posX, elevator.posZ))) {

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
							entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																								&& !this.elevatorName.isEmpty() ? String.format("Block Succesfully Bound to Elevator: %0$s.",
																																				this.elevatorName) : "Block Succesfully Bound to Elevator"));
							return true;
						} else {
							entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																								&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Does Not Seem To Be an Elevator",
																																				this.elevatorName) : "Block Can Not be Bound to Elevator. Block Does Not Seem To Be an Elevator"));
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
										entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																											&& !this.elevatorName.isEmpty() ? String.format("Block Succesfully Bound to Elevator: %0$s.",
																																							this.elevatorName) : "Block Succesfully Bound to Elevator"));
										return true;
									} else {
										entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																											&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Does Not Seem To Be an Elevator",
																																							this.elevatorName) : "Block Can Not be Bound to Elevator. Block Does Not Seem To Be an Elevator"));
									}
								}
							}
							entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																								&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set Withing %1$s Meters of Another Elevator Block or Elevator Computer",
																																				this.elevatorName,
																																				3) : String.format(	"Block Can Not be Bound to Elevator. Block Must be set Withing %0$s Meters of Another Elevator Block or Elevator Computer",
																																									3)));
						} else {
							entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																								&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set Withing %1$s Meters of Elevator Computer",
																																				this.elevatorName,
																																				3) : String.format(	"Block Can Not be Bound to Elevator. Block Must be set Withing %0$s Meters of Elevator Computer",
																																									3)));
						}
					}
				} else {
					entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																						&& !this.elevatorName.isEmpty() ? String.format("Block Already Bound to Elevator: %0$s",
																																		this.elevatorName) : "Block Already Bound to Elevator"));
					return true;
				}
			} else {
				entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																					&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set at Y %1$s",
																																	this.elevatorName,
																																	this.yCoord + 1) : String.format(	"Block Can Not be Bound to Elevator. Block Must be Set at Y %0$s",
																																										this.yCoord + 1)));
			}
		} else {
			entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																				&& !this.elevatorName.isEmpty() ? String.format("You are no longer the Technition for the Elevator %0$s",
																																this.elevatorName) : String.format(	"You are no longer the Technition for the Elevator at %0$s, %1$s ,%2$s",
																																									this.xCoord,
																																									this.yCoord,
																																									this.zCoord)));
			ItemStack heldItem = entityplayer.getHeldItem();
			NBTTagCompound tags = new NBTTagCompound();
			heldItem.setTagCompound(tags);
		}

		return false;
	}

	public boolean addFloorMarker(ChunkCoordinates markerBlock, EntityPlayer entityplayer) {
		if (this.mode == ElevatorMode.Maintenance
			&& this.curTechnicianName != null
			&& this.curTechnicianName.equals(entityplayer.username)) {
			if (!this.boundMarkerBlocks.contains(markerBlock)) {
				if (boundElevatorBlocks.size() != 0) {
					for (XZCoords boundBlock : boundElevatorBlocks) {
						if (MathHelper.sqrt_double(Math.pow((double) boundBlock.x
																	- (double) markerBlock.posX,
															2)
													+ Math.pow(	(double) boundBlock.z
																		- (double) markerBlock.posZ,
																2)) <= 3) {
							if (this.worldObj.getBlockId(	markerBlock.posX,
															markerBlock.posY,
															markerBlock.posZ) == ConfigurationLib.blockTransportBase.blockID
								&& this.worldObj.getBlockMetadata(	markerBlock.posX,
																	markerBlock.posY,
																	markerBlock.posZ) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
								this.boundMarkerBlocks.add(markerBlock);
								entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																									&& !this.elevatorName.isEmpty() ? String.format("Block Succesfully Bound to Elevator: %0$s.",
																																					this.elevatorName) : "Block Succesfully Bound to Elevator"));
								return true;
							} else {
								entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																									&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Does Not Seem To Be a Floor Marker",
																																					this.elevatorName) : "Block Can Not be Bound to Elevator. Block Does Not Seem To Be a Floor Marker"));
							}
						}
					}
					entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																						&& !this.elevatorName.isEmpty() ? String.format("Block Can Not be Bound to Elevator: %0$s. Block Must be set Withing %1$s Meters of an Elevator Block",
																																		this.elevatorName,
																																		3) : String.format(	"Block Can Not be Bound to Elevator. Block Must be set Withing %0$s Meters of an Elevator Block",
																																							3)));
				} else {
					entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																						&& !this.elevatorName.isEmpty() ? "Block Can Not be Bound to Elevator: %0$s. Must Bind at Least One Elevator Block" : "Block Can Not be Bound to Elevator. Must Bind at Least One Elevator Block"));
				}
			} else {
				entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																					&& !this.elevatorName.isEmpty() ? String.format("Block Already Bound to Elevator: %0$s",
																																	this.elevatorName) : "Block Already Bound to Elevator"));

			}
		} else {
			entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																				&& !this.elevatorName.isEmpty() ? String.format("You are no longer the Technition for the Elevator %0$s",
																																this.elevatorName) : String.format(	"You are no longer the Technition for the Elevator at %0$s, %1$s ,%2$s",
																																									this.xCoord,
																																									this.yCoord,
																																									this.zCoord)));
		}
		return false;
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
				if (this.curTechnicianName != null
					&& this.curTechnicianName.equals(entityplayer.username)) {
					this.curTechnicianName = "";
					this.mode = ElevatorMode.Available;
					entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																						&& !this.elevatorName.isEmpty() ? String.format("Elevator: {0} Mantinaince Complete",
																																		this.elevatorName) : "Elevator Mantinaince Complete"));
				} else if (this.curTechnicianName == null
							|| this.curTechnicianName.isEmpty()) {
					// TODO:Ensure if the tools is bound to another elevator
					// that we either inform the previous elevator that
					// Maintenance is over or keep the tool from binding
					tags.setInteger("ComputerX",
									this.xCoord);
					tags.setInteger("ComputerY",
									this.yCoord);
					tags.setInteger("ComputerZ",
									this.zCoord);
					this.curTechnicianName = entityplayer.username;
					entityplayer.sendChatToPlayer(new ChatMessageComponent().addText(this.elevatorName != null
																						&& !this.elevatorName.isEmpty() ? String.format("Elevator: {0} Entering Mantinaince Mode",
																																		this.elevatorName) : "Elevator Entering Mantinaince Mode"));
					// Move Elevator for Maintenance
					if (this.boundElevatorBlocks.size() == 0) {
						this.floorSpool.clear();
						this.elevatorPos = this.yCoord + 1;
						this.mode = ElevatorMode.Maintenance;
					} else {
						this.CallElevator(	this.yCoord + 1,
											true,
											"");
					}

				}
				heldItem.setTagCompound(tags);
			} else {
				// open GUI
			}
		}
		return false;
	}

	@Override
	public boolean removeBlockByPlayer(EntityPlayer player, BlockBase blockBase) {
		for (XZCoords boundElevator : this.boundElevatorBlocks) {
			TileEntity eleTile = this.worldObj.getBlockTileEntity(	boundElevator.x,
																	this.elevatorPos,
																	boundElevator.z);
			if (eleTile != null) {
				((TileEntityElevator) eleTile).RemoveComputer(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
			}
		}
		for (ChunkCoordinates boundFloorMarker : this.boundMarkerBlocks) {
			TileEntity markerTile = this.worldObj.getBlockTileEntity(	boundFloorMarker.posX,
																		boundFloorMarker.posY,
																		boundFloorMarker.posZ);
			if (markerTile != null) {
				((TileEntityFloorMarker) markerTile).RemoveComputer(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
			}
		}
		return super.removeBlockByPlayer(	player,
											blockBase);
	}

	public String CallElevator(int i, String Floorname) {
		return this.CallElevator(	i,
									false,
									Floorname);
	}

	private String CallElevator(int i, boolean forMaintenance, String floorname) {

		if (this.mode == ElevatorMode.Available) {
			if (forMaintenance) {
				this.floorSpool.clear();
				this.pendingMantinance = true;
				sendMeassageFromAllFloors("Elevator Going into Mantinance Mode");

			} else {
				if (i != this.elevatorPos) {
					this.floorSpool.put(i,
										floorname);
				} else {
					return "Elevator Already At Floor "
							+ (floorname == null || floorname.trim().isEmpty() ? i : floorname);
				}
			}
			this.doCallElevator(i,
								floorname);
			return "Elevator Called to Floor "
					+ (floorname == null || floorname.trim().isEmpty() ? i : floorname);

		} else if (this.mode == ElevatorMode.Maintenance) {
			if (forMaintenance) {
				sendMeassageFromAllFloors("Elevator Already in Mantinance Mode");
			}
			return "Elevator in Mantinance Mode please Try Again Later";
		} else if (this.mode == ElevatorMode.Transit) {
			if (forMaintenance) {
				this.pendingMantinance = true;
				sendMeassageFromAllFloors("Mantinance Mode Request Queued");
				return "Mantinance Mode Request Queued";
			} else {
				this.floorSpool.put(i,
									floorname);
				return "Elevator Called to Floor "
						+ (floorname == null || floorname.trim().isEmpty() ? i : floorname);
			}

		}
		return "WTF you should never see me";

	}

	private void sendMeassageFromAllFloors(String string) {
		for (ChunkCoordinates marker : this.boundMarkerBlocks) {
			if (!this.worldObj.isRemote) MinecraftServer.getServer().getConfigurationManager().sendToAllNear(	marker.posX,
																												marker.posY,
																												marker.posZ,
																												4,
																												this.worldObj.provider.dimensionId,
																												new Packet3Chat(new ChatMessageComponent().addText(string)));
		}
		if (!this.worldObj.isRemote) MinecraftServer.getServer().getConfigurationManager().sendToAllNear(	this.xCoord,
																											this.yCoord,
																											this.zCoord,
																											4,
																											this.worldObj.provider.dimensionId,
																											new Packet3Chat(new ChatMessageComponent().addText(string)));

	}

	private void doCallElevator(int i, String floorname) {
		// call elevator now
		Set<EntityElevator> allEntities = new HashSet<EntityElevator>();
		EntityElevator centerElevator = null;
		List<XZCoords> invalidElevators = new ArrayList<XZCoords>();
		boolean first = true;
		this.mode = ElevatorMode.Transit;
		for (XZCoords pos : this.boundElevatorBlocks) {
			if (validElevatorBlock(	pos.x,
									this.elevatorPos,
									pos.z)) {
				int metadata = worldObj.getBlockMetadata(	pos.x,
															this.elevatorPos,
															pos.z);

				EntityElevator curElevator = new EntityElevator(worldObj, pos.x, this.elevatorPos, pos.z);
				if (first) centerElevator = curElevator;
				curElevator.setProperties(	i,
											floorname,
											first,
											FMLCommonHandler.instance().getSide() == Side.CLIENT,
											metadata,
											new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord),
											false,
											centerElevator);
				if (first) first = false; // isClient;
				worldObj.spawnEntityInWorld(curElevator);
			} else {
				invalidElevators.add(pos);
			}
		}
		for (XZCoords pos : invalidElevators) {
			this.boundElevatorBlocks.removeAll(Collections.singleton(pos));
		}

	}

	private boolean validElevatorBlock(int x, int y, int z) {
		TileEntity tile = this.worldObj.getBlockTileEntity(	x,
															y,
															z);
		if (tile != null && tile instanceof TileEntityElevator) {
			if (((TileEntityElevator) tile).getParentElevatorComputer() != null) {
				ChunkCoordinates thisCoords = new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord);
				return ((TileEntityElevator) tile).getParentElevatorComputer().equals(thisCoords);

			}
		}
		return false;
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

		int index = 0;
		int tempSpool[] = new int[floorSpool.size()];
		for (Entry<Integer, String> floorName : this.floorSpool.entrySet()) {
			if (floorName.getValue() != null && !floorName.getValue().isEmpty()) nbttagcompound.setString(	"FloorSpoolNames_"
																													+ index,
																											floorName.getValue());
			tempSpool[index] = floorName.getKey();
		}
		nbttagcompound.setIntArray(	"FloorSpool",
									tempSpool);

		nbttagcompound.setInteger(	"Mode",
									mode.ordinal());
		nbttagcompound.setInteger(	"ElevPos",
									this.elevatorPos);
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
			this.floorSpool.put(tempSpool[i],
								nbttagcompound.getString("FloorSpoolNames_" + i));
		}

		this.mode = ElevatorMode.values()[nbttagcompound.getInteger("Mode")];

		this.elevatorPos = nbttagcompound.getInteger("ElevPos");

		this.curTechnicianName = nbttagcompound.getString("CurTechnicianName");

		elevatorName = nbttagcompound.getString("ElevatorName");

	}

	public String getElevatorName() {
		// TODO Auto-generated method stub
		return this.elevatorName;
	}

	public void RemoveElevatorBlock(XZCoords elevatorPosition) {
		if (this.boundElevatorBlocks.contains(elevatorPosition)) {
			this.boundElevatorBlocks.remove(this.boundElevatorBlocks.indexOf(elevatorPosition));
			List<ChunkCoordinates> invalidBoundMarkerBlocks = new ArrayList<ChunkCoordinates>();
			for (ChunkCoordinates boundMarker : this.boundMarkerBlocks) {
				boolean valid = false;
				for (XZCoords boundElevators : this.boundElevatorBlocks) {
					if (MathHelper.sqrt_double(Math.pow((double) boundElevators.x
																- (double) boundMarker.posX,
														2)
												+ Math.pow(	(double) boundElevators.z
																	- (double) boundMarker.posZ,
															2)) <= 3) {
						if (this.worldObj.getBlockId(	boundMarker.posX,
														boundMarker.posY,
														boundMarker.posZ) == ConfigurationLib.blockTransportBase.blockID
							&& this.worldObj.getBlockMetadata(	boundMarker.posX,
																boundMarker.posY,
																boundMarker.posZ) == BlockLib.BLOCK_DYNAMIC_MARK_ID) {
							valid = true;
						}
					}
				}
				if (!valid) invalidBoundMarkerBlocks.add(boundMarker);
			}
			for (ChunkCoordinates invalidMarker : invalidBoundMarkerBlocks) {
				if (this.boundMarkerBlocks.contains(invalidMarker)) {
					this.boundMarkerBlocks.remove(this.boundMarkerBlocks.indexOf(invalidMarker));
				}
			}
		}

	}

	public void elevatorArrived(int dest, boolean center) {
		this.elevatorPos = dest;
		this.floorSpool.remove(dest);
		for (XZCoords pos : this.boundElevatorBlocks) {
			TileEntity tile = this.worldObj.getBlockTileEntity(	pos.x,
																this.elevatorPos,
																pos.z);
			if (tile != null && tile instanceof TileEntityElevator) {
				TileEntityElevator elevator = (TileEntityElevator) tile;
				elevator.setParentElevatorComputer(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
			}
		}

		if (this.pendingMantinance) {
			if (this.elevatorPos == (this.yCoord + 1)) {
				this.pendingMantinance = false;
				this.mode = ElevatorMode.Maintenance;

			} else {
				this.floorSpool.clear();
				doCallElevator(	this.yCoord + 1,
								"");
			}
		} else {
			this.mode = ElevatorMode.Available;
			if (!this.floorSpool.isEmpty()) {
				Integer nextFloor = this.floorSpool.keySet().iterator().next();
				doCallElevator(	nextFloor,
								this.floorSpool.get(nextFloor));
			}
		}

	}

	public int getElevatorPos() {
		// TODO Auto-generated method stub
		return this.elevatorPos;
	}

	public ElevatorMode getElevatorMode() {
		// TODO Auto-generated method stub
		return this.mode;
	}
}
