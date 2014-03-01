package com.slimevoid.dynamictransport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

public class ContainerFloorSelection extends Container {

    protected IInventory computer;
    protected World      world;

    public ContainerFloorSelection(InventoryPlayer playerInventory, IInventory computer, World world) {
        this.setComputer(computer);
        this.world = world;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return this.getComputer().isUseableByPlayer(entityplayer);
    }

    public IInventory getComputer() {
        return this.computer;
    }

    public void setComputer(IInventory computer) {
        this.computer = computer;
    }

}
