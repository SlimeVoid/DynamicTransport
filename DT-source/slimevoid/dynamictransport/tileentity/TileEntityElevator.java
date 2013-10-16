package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import slimevoid.dynamictransport.core.DynamicTransportMod;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.GuiLib;

public class TileEntityElevator extends TileEntityTransportBase {
	private boolean isFloorsDirty = true;
	private TreeMap <Integer, String> FloorList = new TreeMap <Integer, String>();
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

	public int[] ScanFloors(Boolean isFirstElevator, int minY, int maxY) {
		if(this.isFloorsDirty){
			FloorList.clear();
			//find first block below elevator
			int y = this.yCoord;
			while (y > (minY > -1?minY:0)){
				y--;
				if (!IsBlockValidForShaft(y)) break;
			}	
			
			for (int y2 = this.yCoord; y2 <= maxY; y2++){
				
				if (!IsBlockValidForShaft(y2 + 2)){
					maxY = y2;
					break;
				}
			}
			
			//no Conjoined Elevators yet
			/*if (isFirstElevator){ 
				int tempYs[] = GetFloorsFromNeighbors(y,maxY);				
				y = minY > tempYs[0]?minY:tempYs[0];
				maxY = maxY < tempYs[1]?maxY:tempYs[1];	
			}*/
			minY = y;
			
			
			while(y <= maxY && IsBlockValidForShaft(y + 2)){
				if (IsValidFloor(y)){
					FloorList.put(y, "");
				}
				y++;
			}
			
		}
		int result[] = new int[2];
		result[0] = minY;
		result[1] = maxY;
		return result;
	}
	
	private boolean IsBlockValidForShaft(int y) {
		
		return this.worldObj.getBlockId(this.xCoord , y, this.zCoord) == ConfigurationLib.blockTransportBaseID|| ValidNonSolid(this.xCoord , y, this.zCoord);
	}

	public TreeMap<Integer, String> GetFloorList() {
		return this.FloorList;
		
	}

	private int[] GetFloorsFromNeighbors(int minY, int maxY) {
		int result[] = new int[2];
		result[0] = minY;
		result[1] = maxY;
		ArrayList<ChunkPosition> stillvalidBlocks = new ArrayList<ChunkPosition>();
		for(ChunkPosition blockPos: this.ConjoinedBlocks){
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(blockPos.x, blockPos.y, blockPos.z);
			if (tileEntity instanceof TileEntityElevator){
				result = ((TileEntityElevator)tileEntity).ScanFloors(false,minY,maxY);
				stillvalidBlocks.add(blockPos);
			}			
		}
		this.ConjoinedBlocks = stillvalidBlocks;
		
		return result;
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
			return true;
		}
		return false;
	}

	private boolean ValidSolid(int x, int y, int z) {
		if(!this.worldObj.isAirBlock(x, y, z)){
			return true;
		}
		return false;
	}

}
