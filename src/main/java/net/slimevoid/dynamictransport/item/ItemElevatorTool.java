package net.slimevoid.dynamictransport.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemElevatorTool extends Item {
    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTagCompound();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if(playerIn.isSneaking())
        {
            ItemStack usedItem = playerIn.getHeldItem(handIn);
            usedItem.setTagCompound(null);
            return ActionResult.newResult(EnumActionResult.PASS,usedItem);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
