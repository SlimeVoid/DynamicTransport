package net.slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;

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
    
    public IInventory getMarker() {
        return this.marker;
    }

}
