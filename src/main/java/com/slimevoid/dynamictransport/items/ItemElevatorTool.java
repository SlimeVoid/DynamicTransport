package com.slimevoid.dynamictransport.items;

import com.slimevoid.dynamictransport.core.lib.ConfigurationLib;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemElevatorTool extends Item {

    public ItemElevatorTool(int par1) {
        super(par1);
        this.maxStackSize = 1;
    }

    @Override
    public boolean isItemTool(ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public EnumRarity getRarity(ItemStack ist) {
        return EnumRarity.rare;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z) {
        return world.getBlockId(x,
                                y,
                                z) == ConfigurationLib.blockTransportBaseID;
    }

}
