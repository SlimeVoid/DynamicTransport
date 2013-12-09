package slimevoid.dynamictransport.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.util.XZCoords;
import slimevoidlib.blocks.BlockBase;

public class TileEntityElevator extends TileEntityTransportBase {

	private ChunkCoordinates	ParentElevatorComputer;

	private int					maxY	= -1;
	private int					minY	= -1;

	public int getMaxY(boolean reCalculate) {
		if (reCalculate || maxY == -1) {
			for (int y = this.yCoord; y < this.worldObj.getActualHeight(); y++) {
				if (!this.worldObj.isAirBlock(	this.xCoord,
												y + 1,
												this.zCoord)
					|| y == this.worldObj.getActualHeight()) {
					this.maxY = y;
					break;
				}
			}
		}

		return maxY;
	}

	public int getMinY(boolean reCalculate) {
		if (reCalculate || minY == -1) {
			for (int y = this.yCoord; y >= 0; y--) {
				if (!this.worldObj.isAirBlock(	this.xCoord,
												y - 1,
												this.zCoord) || y == 0) {
					this.minY = y;
					break;
				}
			}
		}

		return minY;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getHeldItem() != null
			&& entityplayer.getHeldItem().itemID == ConfigurationLib.itemElevatorTool.itemID) {
			if (this.worldObj.isRemote) {
				return true;
			}
			NBTTagCompound tags = entityplayer.getHeldItem().getTagCompound();
			if (tags.hasKey("ComputerX")) {
				setParentElevatorComputer(	new ChunkCoordinates(tags.getInteger("ComputerX"), tags.getInteger("ComputerY"), tags.getInteger("ComputerZ")),
											entityplayer);
			}
		}
		return false;
	}

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation) {
		TileEntityElevatorComputer comTile = ParentElevatorComputer == null ? null : (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(	this.ParentElevatorComputer.posX,
																																					this.ParentElevatorComputer.posY,
																																					this.ParentElevatorComputer.posZ);
		if (comTile == null) this.ParentElevatorComputer = null;
		if (this.worldObj.getBlockId(	ComputerLocation.posX,
										ComputerLocation.posY,
										ComputerLocation.posZ) == ConfigurationLib.blockTransportBase.blockID
			&& this.worldObj.getBlockMetadata(	ComputerLocation.posX,
												ComputerLocation.posY,
												ComputerLocation.posZ) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {
			this.ParentElevatorComputer = ComputerLocation;
		}

	}

	public void setParentElevatorComputer(ChunkCoordinates ComputerLocation, EntityPlayer entityplayer) {
		TileEntityElevatorComputer comTile = ParentElevatorComputer == null ? null : (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(	this.ParentElevatorComputer.posX,
																																					this.ParentElevatorComputer.posY,
																																					this.ParentElevatorComputer.posZ);
		if (comTile == null) this.ParentElevatorComputer = null;

		if (this.worldObj.getBlockId(	ComputerLocation.posX,
										ComputerLocation.posY,
										ComputerLocation.posZ) == ConfigurationLib.blockTransportBase.blockID
			&& this.worldObj.getBlockMetadata(	ComputerLocation.posX,
												ComputerLocation.posY,
												ComputerLocation.posZ) == BlockLib.BLOCK_ELEVATOR_COMPUTER_ID) {

			comTile = (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(ComputerLocation.posX,
																					ComputerLocation.posY,
																					ComputerLocation.posZ);
			if (comTile.addElevator(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord),
									entityplayer)) this.ParentElevatorComputer = ComputerLocation;

		} else {
			ItemStack heldItem = entityplayer.getHeldItem();
			NBTTagCompound tags = new NBTTagCompound();
			entityplayer.sendChatToPlayer(new ChatMessageComponent().addText("Block Can Not be Bound Elevator Computer missing"));
			heldItem.setTagCompound(tags);
		}

	}

	@Override
	public boolean removeBlockByPlayer(EntityPlayer player, BlockBase blockBase) {
		TileEntityElevatorComputer comTile = ParentElevatorComputer == null ? null : (TileEntityElevatorComputer) this.worldObj.getBlockTileEntity(	this.ParentElevatorComputer.posX,
																																					this.ParentElevatorComputer.posY,
																																					this.ParentElevatorComputer.posZ);
		if (comTile != null) {
			((TileEntityElevatorComputer) comTile).RemoveElevatorBlock(new XZCoords(this.xCoord, this.zCoord));
		}
		return super.removeBlockByPlayer(	player,
											blockBase);
	}

	public ChunkCoordinates getParentElevatorComputer() {
		return this.ParentElevatorComputer;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (ParentElevatorComputer != null) {
			nbttagcompound.setInteger(	"ParentElevatorComputerX",
										ParentElevatorComputer.posX);
			nbttagcompound.setInteger(	"ParentElevatorComputerY",
										ParentElevatorComputer.posY);
			nbttagcompound.setInteger(	"ParentElevatorComputerZ",
										ParentElevatorComputer.posZ);
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.ParentElevatorComputer = new ChunkCoordinates(nbttagcompound.getInteger("ParentElevatorComputerX"), nbttagcompound.getInteger("ParentElevatorComputerY"), nbttagcompound.getInteger("ParentElevatorComputerZ"));

	}

	public void RemoveComputer(ChunkCoordinates chunkCoordinates) {
		this.ParentElevatorComputer = null;

	}

}
