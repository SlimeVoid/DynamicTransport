package slimevoid.dynamictransport.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.DynamicTransportMod;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoidlib.blocks.BlockBase;

public class TileEntityFloorMarker extends TileEntityTransportBase {

	private ChunkCoordinates	parentTransportBase;
	private String				floorName;
	private boolean				Powered	= false;
	private int					yOffset	= -2;

	public ChunkCoordinates getParent() {
		return this.parentTransportBase;
	}

	// used for deciding parent/child behavior
	private boolean isChildMarker() {
		TileEntity tile = parentTransportBase == null ? null : this.worldObj.getBlockTileEntity(this.parentTransportBase.posX,
																								this.parentTransportBase.posY,
																								this.parentTransportBase.posZ);
		return tile == null || tile instanceof TileEntityFloorMarker;
	}

	// will try to find the elevator this is bound to even if the block is bound
	// to another marker
	public TileEntityElevatorComputer getParentElevatorComputer() {
		TileEntity tile = parentTransportBase == null ? null : this.worldObj.getBlockTileEntity(this.parentTransportBase.posX,
																								this.parentTransportBase.posY,
																								this.parentTransportBase.posZ);
		if (tile == null) {
			parentTransportBase = null;
		} else if (!(tile instanceof TileEntityElevatorComputer)) {
			if ((tile instanceof TileEntityFloorMarker && ((TileEntityFloorMarker) tile).getParentElevatorComputer() != null)) {
				tile = ((TileEntityFloorMarker) tile).getParentElevatorComputer();
			} else {
				tile = null;
				parentTransportBase = null;
			}
		}

		return (TileEntityElevatorComputer) tile;
	}

	@Override
	public boolean isBlockSolidOnSide(BlockBase blockBase, ForgeDirection side) {
		return true;
	}

	@Override
	public void onBlockNeighborChange(int blockID) {
		boolean flag = this.worldObj.isBlockIndirectlyGettingPowered(	this.xCoord,
																		this.yCoord,
																		this.zCoord);

		if (!this.Powered) {
			if (flag) {
				this.Powered = true;
				this.callElevator();
			}
		} else if (!flag) {
			this.Powered = false;
		}
	}

	private void callElevator() {
		if (this.isChildMarker()) {
			this.getParentFloorMarker().callElevator();
		} else {
			TileEntityElevatorComputer comTile = this.getParentElevatorComputer();
			if (comTile != null) {
				String msg = comTile.CallElevator(	this.yCoord - this.yOffset,
													this.floorName);
				if (!this.worldObj.isRemote) {
					MinecraftServer.getServer().getConfigurationManager().sendToAllNear(this.xCoord,
																						this.yCoord,
																						this.zCoord,
																						4,
																						this.worldObj.provider.dimensionId,
																						new Packet3Chat(ChatMessageComponent.createFromTranslationKey(msg)));
				}
			}
		}
	}

	private TileEntityFloorMarker getParentFloorMarker() {
		// noop
		return null;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (this.getWorldObj().isRemote) {
			return true;
		}
		if (!this.getWorldObj().isRemote) {
			if (entityplayer.getHeldItem() != null
				&& entityplayer.getHeldItem().itemID == ConfigurationLib.itemElevatorTool.itemID) {
				NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
				if (tags.hasKey("ComputerX") && tags.getByte("Mode") == 0) {
					ChunkCoordinates possibleComputer = new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
					if (entityplayer.isSneaking()) {
						if (possibleComputer.equals(this.parentTransportBase)) {
							entityplayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("slimevoid.DT.dynamicMarker.unbound"));// "Block Unbound"
							this.getParentElevatorComputer().RemoveMarkerBlock(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
							removeParent();
							return true;
						} else if (this.parentTransportBase != null) {
							entityplayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("slimevoid.DT.dynamicMarker.boundToOtherComputer"));// "Block Bound to Another Elevator"
						}
					} else {
						if (this.parentTransportBase == null) {
							setParentComputer(	new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ")),
												entityplayer);
						} else if (possibleComputer.equals(this.parentTransportBase)) {
							// open option GUI
							entityplayer.openGui(	DynamicTransportMod.instance,
													GuiLib.GUIID_FLOOR_MARKER,
													this.worldObj,
													this.xCoord,
													this.yCoord,
													this.zCoord);
							return true;
						}
					}
				}
			} else if (this.getParentElevatorComputer() == null
						|| this.getParentElevatorComputer().getElevatorMode() == TileEntityElevatorComputer.ElevatorMode.Maintenance) {
				if (this.camoItem == null
					&& entityplayer.getHeldItem() != null
					&& entityplayer.getHeldItem().getItem() instanceof ItemBlock
					&& Block.blocksList[entityplayer.getHeldItem().getItem().itemID] != null
					&& !(Block.blocksList[entityplayer.getHeldItem().getItem().itemID] instanceof BlockTransportBase)
					&& Block.blocksList[entityplayer.getHeldItem().getItem().itemID].isBlockNormalCube(	getWorldObj(),
																										xCoord,
																										yCoord,
																										zCoord)) {
					this.setCamoItem(entityplayer.getHeldItem().copy());
					--entityplayer.getHeldItem().stackSize;
					return true;
				}

				if (this.camoItem != null && entityplayer.getHeldItem() == null) {
					this.removeCamoItem();
					return true;
				}
			} else if (!this.getWorldObj().isRemote) {
				if (this.getParent() != null
					&& (this.getParentElevatorComputer().getElevatorMode() != TileEntityElevatorComputer.ElevatorMode.Maintenance)) {
					// show floor selection
					entityplayer.openGui(	DynamicTransportMod.instance,
											GuiLib.GUIID_FloorSelection,
											this.worldObj,
											this.xCoord,
											this.yCoord,
											this.zCoord);
					return true;
				}
			}

		}
		return false;
	}

	private void setParentMarker(ChunkCoordinates possibleMarker, EntityPlayer entityplayer) {
		// noop
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (parentTransportBase != null) {
			nbttagcompound.setInteger(	"ParentTransportComputerX",
										parentTransportBase.posX);
			nbttagcompound.setInteger(	"ParentTransportComputerY",
										parentTransportBase.posY);
			nbttagcompound.setInteger(	"ParentTransportComputerZ",
										parentTransportBase.posZ);
		}
		if (this.getParentFloorMarker() == null && this.floorName != null
			&& !this.floorName.isEmpty()) nbttagcompound.setString(	"FloorName",
																	floorName);
		nbttagcompound.setInteger(	"yOffset",
									yOffset);
		nbttagcompound.setBoolean(	"Powered",
									this.Powered);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.parentTransportBase = new ChunkCoordinates(nbttagcompound.getInteger("ParentTransportComputerX"), nbttagcompound.getInteger("ParentTransportComputerY"), nbttagcompound.getInteger("ParentTransportComputerZ"));

		this.floorName = nbttagcompound.getString("FloorName");

		this.yOffset = nbttagcompound.getInteger("yOffset");
		this.Powered = nbttagcompound.getBoolean("Powered");
	}

	public void removeParent() {

		this.parentTransportBase = null;

	}

	public void setParentComputer(ChunkCoordinates ComputerLocation, EntityPlayer entityplayer) {
		TileEntityElevatorComputer comTile = getParentElevatorComputer();

		if (this.worldObj.getBlockId(	ComputerLocation.posX,
										ComputerLocation.posY,
										ComputerLocation.posZ) == ConfigurationLib.blockTransportBase.blockID
			&& this.worldObj.getBlockMetadata(	ComputerLocation.posX,
												ComputerLocation.posY,
												ComputerLocation.posZ) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {

			comTile = (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(ComputerLocation.posX,
																					ComputerLocation.posY,
																					ComputerLocation.posZ);
			if (comTile.addFloorMarker(	new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord),
										entityplayer)) {
				this.parentTransportBase = ComputerLocation;
				this.onInventoryChanged();
				this.getWorldObj().markBlockForUpdate(	this.xCoord,
														this.yCoord,
														this.zCoord);
			}

		} else {
			ItemStack heldItem = entityplayer.getHeldItem();
			NBTTagCompound tags = new NBTTagCompound();
			entityplayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("slimevoid.DT.dynamicMarker.bindMissingElevator"));
			heldItem.setTagCompound(tags);
		}

	}

	@Override
	public int getExtendedBlockID() {
		return BlockLib.BLOCK_DYNAMIC_MARK_ID;
	}

	public int getFloorY() {
		return this.yCoord + this.yOffset;
	}

	public String getFloorName() {
		return this.floorName;
	}

}
