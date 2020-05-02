package net.slimevoid.dynamictransport.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.util.math.BlockPos;


public class TransportPartEntity extends EnderDragonPartEntity {
    public final Entity parent;
    private final BlockPos offset;

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
}
