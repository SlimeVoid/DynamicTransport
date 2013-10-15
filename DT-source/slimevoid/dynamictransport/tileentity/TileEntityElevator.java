package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import slimevoid.dynamictransport.core.DynamicTransportMod;
import slimevoid.dynamictransport.core.lib.GuiLib;

public class TileEntityElevator extends TileEntityTransportBase {
	private boolean isFloorsDirty = true;
	private Map <Integer, String> FloorList = new HashMap <Integer, String>();
	private ArrayList<ChunkPosition> ConjoinedBlocks = new ArrayList<ChunkPosition>();
	private int TargetFloor;

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (entityplayer.isSneaking()) {
			return false;
		}
		if (this.worldObj.isRemote) {
			return true;
		} else {
			entityplayer.openGui(DynamicTransportMod.instance,
					GuiLib.GUIID_ELEVATOR, this.worldObj, this.xCoord,
					this.yCoord, this.zCoord);
			return true;
		}
	}

	public int ScanFloors(Boolean isFirstElevator, int minY) {
		if(this.isFloorsDirty){
			FloorList.clear();
			//find first block below elevator
			int y = this.yCoord;
			while (y > (minY > -1?minY:0)){
				y--;
				if (!this.worldObj.isAirBlock(this.xCoord, y - 1, this.zCoord)) break;
			}	
			
			if (isFirstElevator){ 
				int tempMinY = GetFloorsFromNeighbors(y);				
				y = minY > tempMinY?minY:tempMinY;				
			}
			else minY = y;
			
			
			while(y <= this.worldObj.getHeight()){
				if (IsValidFloor(y)){
					FloorList.put(y, "");
				}
			}
		}
		return minY;
	}
	
	private Map<Integer, String> GetFloorList() {
		return this.FloorList;
		
	}

	private int GetFloorsFromNeighbors(int minY) {
		ArrayList<ChunkPosition> stillvalidBlocks = new ArrayList<ChunkPosition>();
		for(ChunkPosition blockPos: this.ConjoinedBlocks){
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(blockPos.x, blockPos.y, blockPos.z);
			if (tileEntity instanceof TileEntityElevator){
				minY = ((TileEntityElevator)tileEntity).ScanFloors(false,minY);
				stillvalidBlocks.add(blockPos);
			}			
		}
		this.ConjoinedBlocks = stillvalidBlocks;
		return minY;
	}


	private boolean IsValidFloor(int y) {
		//does y-- have any bordering solid
		if(ValidSolid(xCoord + 1, y , zCoord)){
			//Can the player walk throught the next two block
			if(ValidNonSolid(xCoord + 1, y + 1, zCoord) && ValidNonSolid(xCoord + 1, y + 2, zCoord)){
				return true;
			}
		}
		if(ValidSolid(xCoord - 1, y , zCoord)){
			if(ValidNonSolid(xCoord + 1, y + 1, zCoord) && ValidNonSolid(xCoord, y + 2, zCoord - 1)){
				return true;
			}
		}
		if(ValidSolid(xCoord, y , zCoord + 1)){
			if(ValidNonSolid(xCoord + 1, y + 1, zCoord) && ValidNonSolid(xCoord + 1, y + 2, zCoord)){
				return true;
			}
		}
		if(ValidSolid(xCoord, y , zCoord - 1)){
			if(ValidNonSolid(xCoord + 1, y + 1, zCoord) && ValidNonSolid(xCoord, y + 2, zCoord - 1)){
				return true;
			}
		}
		return false;
	}

	private boolean ValidNonSolid(int x, int y, int z) {
		if(this.worldObj.isAirBlock(x, y, z)){
			
		}
		return false;
	}

	private boolean ValidSolid(int x, int y, int z) {
		if(!this.worldObj.isAirBlock(x, y, z)){
			
		}
		return false;
	}

}
