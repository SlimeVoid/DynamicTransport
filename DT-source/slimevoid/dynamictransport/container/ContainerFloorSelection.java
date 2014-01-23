package slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;

public class ContainerFloorSelection extends Container {
	TileEntityElevatorComputer	comp;

	public ContainerFloorSelection(InventoryPlayer playerInventory, TileEntityElevatorComputer workBench) {
		comp = workBench;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

}
