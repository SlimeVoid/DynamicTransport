package net.slimevoid.dynamictransport.entity;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntityTransportPart extends MultiPartEntityPart{
    private final BlockPos offset;
    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY); //server only
    private List<IBlockState> clientCamo;
    private int overLay;

    public EntityTransportPart(IEntityMultiPart parent, BlockPos offset) {
        super(parent, "", 1, 1);
        this.offset = offset;
        this.height = 1;
        this.width = 1;
    }
    public BlockPos getOffset() {
        return offset;
    }
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @SideOnly(Side.CLIENT)
    public void setClientCamo(List<IBlockState> clientCamo) {
        this.clientCamo = clientCamo;
    }
    public List<IBlockState> getClientBlockStates() {
        return this.clientCamo;
    }
    public void setItems(NonNullList<ItemStack> camoSides) {
        items = camoSides;
    }

    public void setOverlay(int overlay) {
        this.overLay = overlay;
    }

    public int getOverlay() {
        return this.overLay;
    }


    /*@Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }*/

}
