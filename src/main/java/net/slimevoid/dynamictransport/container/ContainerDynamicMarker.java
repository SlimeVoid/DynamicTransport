package net.slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;

public class ContainerDynamicMarker extends Container {
	
	protected IInventory marker;
    protected World      world;
    
    public ContainerDynamicMarker(InventoryPlayer playerInventory, IInventory marker, World world) {
    	this.marker =marker;
    	this.world = world;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }
    
    public TileEntityFloorMarker getMarker() {
        if (this.marker != null
                && this.marker instanceof TileEntityFloorMarker) {
            return (TileEntityFloorMarker)this.marker;
        }
        return null;
    }

    public int getFloorY(){
            return this.getMarker().getFloorY();
    }

    public String getFloorName(){
        return this.getMarker().getFloorName();
    }

    public void setFloorY(int newFloor){
        this.getMarker().setFloorY(newFloor);
    }

    public void setFloorName(String newName){
        this.getMarker().setFloorName(newName);
    }
}
