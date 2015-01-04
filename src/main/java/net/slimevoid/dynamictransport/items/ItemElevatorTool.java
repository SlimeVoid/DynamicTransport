package net.slimevoid.dynamictransport.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;

public class ItemElevatorTool extends Item {

    public ItemElevatorTool() {
        super();
        this.maxStackSize = 1;
    }

    @Override
    public boolean isItemTool(ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public EnumRarity getRarity(ItemStack ist) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer entityplayer) {
        return world.getBlockState(pos).getBlock() == ConfigurationLib.blockTransportBase;
    }

}
