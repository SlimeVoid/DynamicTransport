package net.slimevoid.dynamictransport.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ElevatorToolItem extends Item {
    public ElevatorToolItem() {
        super(new Item.Properties().group(ItemGroup.TRANSPORTATION));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTag();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(playerIn.isCrouching())
        {
            ItemStack usedItem = playerIn.getHeldItem(handIn);
            usedItem.setTag(null);
            return ActionResult.resultPass(usedItem);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
