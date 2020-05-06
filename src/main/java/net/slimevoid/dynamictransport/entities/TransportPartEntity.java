package net.slimevoid.dynamictransport.entities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;


public class TransportPartEntity extends EnderDragonPartEntity {
    public final Entity parent;
    private final BlockPos offset;
    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY); //server only
    private List<BlockState> clientCamo;

    public TransportPartEntity(Entity parent, BlockPos offset) {
        super(new EnderDragonEntity(EntityType.ENDER_DRAGON,parent.world),"part",1,1);
        this.parent = parent;
        this.offset = offset;
    }
    /**
     * Returns true if Entity argument is equal to this Entity
     */
    public boolean isEntityEqual(Entity entityIn) {
        //if self or is parent or shares parent
        return this == entityIn || this.parent == entityIn || (entityIn instanceof TransportPartEntity && ((TransportPartEntity) entityIn).parent == this.parent);
    }
    public BlockPos getOffset() {
        return offset;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }
    @OnlyIn(Dist.CLIENT)
    public void setClientCamo(List<BlockState> clientCamo) {
        this.clientCamo = clientCamo;
    }

    public List<BlockState> getClientBlockStates() {
        return this.clientCamo;
    }

    public void setItems(NonNullList<ItemStack> camoSides) {
        items = camoSides;
    }


}
