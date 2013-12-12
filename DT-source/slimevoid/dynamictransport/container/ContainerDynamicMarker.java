package slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import slimevoid.dynamictransport.network.packet.PacketGui;
import slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;

public class ContainerDynamicMarker extends Container {

	public ContainerDynamicMarker(InventoryPlayer playerInventory, TileEntityFloorMarker workBench) {
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void handleGuiEvent(PacketGui packetGui) {

	}

}
