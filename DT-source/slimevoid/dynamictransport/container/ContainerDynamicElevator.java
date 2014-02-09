package slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;

public class ContainerDynamicElevator extends Container {

    public ContainerDynamicElevator(InventoryPlayer playerInventory, TileEntityElevator workBench) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }

}
