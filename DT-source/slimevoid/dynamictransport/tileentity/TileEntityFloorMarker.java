package slimevoid.dynamictransport.tileentity;

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
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.blocks.BlockBase;

public class TileEntityFloorMarker extends TileEntityTransportBase {

	private ChunkCoordinates	parentTransportComputer;
	private int					floorYLvl	= -1;
	private String				floorName;
	private boolean				Powered		= false;

	public ChunkCoordinates getParentComputer() {
		return this.parentTransportComputer;
	}

	public TileEntityElevatorComputer getParentElevatorComputer() {
		TileEntity tile = parentTransportComputer == null ? null : this.worldObj.getBlockTileEntity(this.parentTransportComputer.posX,
																									this.parentTransportComputer.posY,
																									this.parentTransportComputer.posZ);
		if (tile == null) {
			parentTransportComputer = null;
		} else if (!(tile instanceof TileEntityElevatorComputer)) {
			tile = null;
			parentTransportComputer = null;
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
				TileEntityElevatorComputer comTile = this.getParentElevatorComputer();
				if (comTile != null) {
					if (this.floorYLvl == -1) {
						this.floorYLvl = this.yCoord - 2;
					}

					String msg = comTile.CallElevator(	floorYLvl,
														this.floorName);
					if (!this.worldObj.isRemote) {
						MinecraftServer.getServer().getConfigurationManager().sendToAllNear(this.xCoord,
																							this.yCoord,
																							this.zCoord,
																							4,
																							this.worldObj.provider.dimensionId,
																							new Packet3Chat(new ChatMessageComponent().addText(msg)));
					}

				}
			}
		} else if (!flag) {
			this.Powered = false;
		}
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (this.worldObj.isRemote) {
			return true;
		}

		if (entityplayer.getHeldItem() != null
			&& entityplayer.getHeldItem().itemID == ConfigurationLib.itemElevatorTool.itemID) {
			NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
			if (tags.hasKey("ComputerX")) {
				ChunkCoordinates possibleComputer = new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ"));
				if (entityplayer.isSneaking()) {
					if (possibleComputer.equals(this.parentTransportComputer)) {
						entityplayer.sendChatToPlayer(new ChatMessageComponent().addText("Block Unbound"));
						RemoveComputer();
						return true;
					} else if (this.parentTransportComputer != null) {
						entityplayer.sendChatToPlayer(new ChatMessageComponent().addText("Block Bound to Another Elevator"));
					}
				} else {
					if (this.parentTransportComputer == null) {
						setParentComputer(	new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ")),
											entityplayer);
					} else if (possibleComputer.equals(this.parentTransportComputer)) {
						// open option GUI

					}
				}
			}
		} else {
			if (this.getParentElevatorComputer() == null
				|| this.getParentElevatorComputer().getElevatorMode() == TileEntityElevatorComputer.ElevatorMode.Maintenance) {
				if (this.camoItem == null
					&& entityplayer.getHeldItem() != null
					&& entityplayer.getHeldItem().getItem() instanceof ItemBlock) {
					this.setCamoItem(entityplayer.getHeldItem().copy());
					--entityplayer.getHeldItem().stackSize;
					return true;
				}

				if (this.camoItem != null && entityplayer.getHeldItem() == null) {
					this.removeCamoItem();
					return true;
				}
			} else if (this.getParentComputer() != null
						&& this.getParentElevatorComputer().getElevatorMode() != TileEntityElevatorComputer.ElevatorMode.Maintenance) {
				// show floor selection
			}

		}
		return false;
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

	public void RemoveComputer() {
		this.parentTransportComputer = null;

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
										entityplayer)) this.parentTransportComputer = ComputerLocation;

		} else {
			ItemStack heldItem = entityplayer.getHeldItem();
			NBTTagCompound tags = new NBTTagCompound();
			entityplayer.sendChatToPlayer(new ChatMessageComponent().addText("Block Can Not be Bound Computer missing"));
			heldItem.setTagCompound(tags);
		}

	}
}
