package slimevoid.dynamictransport.container;

import slimevoid.dynamictransport.network.packet.PacketGui;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerDynamicElevator extends Container {

	
	
	public ContainerDynamicElevator(InventoryPlayer playerInventory,
			TileEntityElevator workBench) {
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void handleGuiEvent(PacketGui packetGui) {
		
	}

}
