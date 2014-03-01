package com.slimevoid.dynamictransport.container;

import com.slimevoid.dynamictransport.tileentity.TileEntityElevator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerDynamicElevator extends Container {

    public ContainerDynamicElevator(InventoryPlayer playerInventory, TileEntityElevator workBench) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }

}
