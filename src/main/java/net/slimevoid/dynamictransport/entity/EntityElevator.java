package net.slimevoid.dynamictransport.entity;

import javafx.scene.chart.Axis;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.core.RegistryHandler;
import net.slimevoid.dynamictransport.tileentity.TileEntityCamo;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.slimevoid.dynamictransport.block.BlockCamoBase;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class EntityElevator extends Entity implements IEntityMultiPart, IEntityAdditionalSpawnData {
    private static final float maxSpeed = 0.2F;
    private static final float minSpeed = 0.016F;
    private static final float maxAcceleration = 0.01F;
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityElevator.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> DEST = EntityDataManager.createKey(EntityElevator.class, DataSerializers.VARINT);

    private int rest;
    private BlockPos controller;
    private boolean slowingDown;
    private String floorName = null;
    private Entity obstructed = null;
    private final List<EntityTransportPart> parts = new ArrayList<>();
    private AxisAlignedBB renderBoundingBox;

    public EntityElevator(World worldIn) {
        super(worldIn);
        this.noClip = true;
        this.width = 1F;
        this.height = 1F;
        this.rest = 25;
    }

    public EntityElevator(World worldIn,BlockPos controller, List<BlockPos> parts, int elevatorPos, int floorY, String floorName){
        this(worldIn);
        this.setDest(floorY);
        this.controller = controller;
        this.floorName = floorName;
        BlockPos center = new BlockPos(controller.getX(),elevatorPos,controller.getZ());
        double x = (double)center.getX() + 0.5D;
        double y = center.getY();
        double z = (double)center.getZ() + 0.5D;
        this.setPosition(x, y + (double)((1.0F - this.height) / 2.0F), z);
        this.setVelocity(0,0,0);
        this.setOrigin(center);

        for(BlockPos pos: parts){
            EntityTransportPart part = new EntityTransportPart(this,pos);
            part.setPosition(this.posX + pos.getX(),this.posY + pos.getY(),this.posZ + pos.getZ());
            TileEntity t = this.world.getTileEntity(new BlockPos(part));
            if(t instanceof TileEntityElevator) {
                part.setItems(((TileEntityCamo)t).getCamoSides());
                part.setOverlay(((TileEntityElevator)t).getOverlay());
                this.parts.add(part);
            }
        }

    }

    /**
     * Gets the bounding box of this Entity, adjusted to take auxiliary entities into account (e.g. the tile contained
     * by a minecart, such as a command block).
     */
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if(renderBoundingBox == null){
            int minY = 0,maxY = 0,minX = 0,maxX = 0,minZ = 0,maxZ = 0;
            for(EntityTransportPart e: this.parts) {
                BlockPos pos = e.getOffset();
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
            }
            renderBoundingBox =
                    new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
        }
        return renderBoundingBox.offset(this.posX,this.posY,this.posZ);
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return super.getEntityBoundingBox();
    }

    private void setOrigin(BlockPos blockPos) {
        this.dataManager.set(ORIGIN, blockPos);
    }
    private void setDest(int value) {
        this.dataManager.set(DEST, value);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ORIGIN, BlockPos.ORIGIN);
        this.dataManager.register(DEST, 0);
    }

    public BlockPos getOrigin() {
        return this.dataManager.get(ORIGIN);
    }
    public int getDest() {return this.dataManager.get(DEST); }

    @Override
    public void onUpdate() {
        if (!world.isRemote && isUnObstructed()) {
            if(firstUpdate){
                for(EntityTransportPart part: parts) {
                    BlockPos pos = new BlockPos(this).add(part.getOffset());
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    world.notifyNeighborsOfStateChange(pos, Blocks.AIR, true);
                }
            }
            CalcVelocity();
        }
        // check whether at the destination or not
        if ((MathHelper.abs((float) (getDest() - this.posY)) < MathHelper.abs((float)this.motionY))) {
            Arrived();
            return;
        }
        MoveParts();
        if(!world.isRemote){
            setTransientBlock();
        }

        super.onUpdate();
    }

    private void setTransientBlock() {
        BlockPos pos = new BlockPos(this);
        BlockPos prev = new BlockPos(this.prevPosX,prevPosY,prevPosZ);
        for(EntityTransportPart part: parts) {
            BlockPos partPos = pos.add(part.getOffset());
            if (part.getItems() != null) {
                int light = 0;
                int power = 0;
                for(ItemStack s: part.getItems()){
                    light = Math.max(light, getLight(s,partPos));
                    power = Math.max(power, getPower(s,partPos));
                }
                if (light + power > 0) {
                    if(rest <= 0)
                        power = 0;

                    //attempt to replace block
                    if(this.world.isAirBlock(partPos) && light + power > 0) {
                        this.world.setBlockState(partPos, ModBlocks.getTransientBlocks()[light].getDefaultState().withProperty(ModBlocks.POWER, power), 3);
                    }
                    //remove lingering blocks
                    if(!prev.equals(pos)){
                        world.scheduleBlockUpdate(prev.add(part.getOffset()),ModBlocks.getTransientBlocks()[light],10,0);
                    }
                }
            }
        }
    }

    private int getLight(ItemStack stack, BlockPos pos){
        Block camouflage = Block.getBlockFromItem(stack.getItem());
        return camouflage.getLightValue(camouflage.getStateFromMeta(stack.getMetadata()),this.world,pos);
    }

    private int getPower(ItemStack stack, BlockPos pos){
        Block camouflage = Block.getBlockFromItem(stack.getItem());
        return camouflage.getWeakPower(camouflage.getStateFromMeta(stack.getMetadata()),this.world,pos, EnumFacing.NORTH);
    }

    private void Arrived() {
        this.setPosition(this.posX, getDest() + (double)((1.0F - this.height) / 2.0F), this.posZ);
        this.rest = 40;
        this.setVelocity(0, 0, 0);

        for (EntityTransportPart part : this.parts) {
            BlockPos pos = part.getOffset();
            part.setPosition(this.posX + pos.getX(),this.posY + pos.getY(),this.posZ + pos.getZ());

            double yTarget = getDest() + pos.getY() + 1;
            for (Entity rider : this.world.getEntitiesInAABBexcluding(this, part.getEntityBoundingBox().setMaxY(yTarget), PossibleRider::test)) {
                if((rider.getEntityBoundingBox().minY)< yTarget){
                    rider.move(MoverType.SHULKER, 0,yTarget - rider.getEntityBoundingBox().minY,0);
                    rider.motionY = Math.max(0, rider.motionY - this.motionY);
                }
            }

        }
        if(!world.isRemote){
            boolean timeToDie = true;
            TileEntity t = world.getTileEntity(this.controller);
            if(t instanceof TileEntityElevatorController) {
                TileEntityElevatorController com = ((TileEntityElevatorController) t);
                Tuple<Integer, String> next = com.elevatorArrived(getDest());
                if (next != null) {
                    setDest(next.getFirst());
                    this.floorName = next.getSecond();
                    this.slowingDown = false;
                    timeToDie = false;
                }
            }
            if(timeToDie) {
                this.setDead();
                for (EntityTransportPart part : this.parts) {
                    BlockPos bPos = new BlockPos(this.posX, getDest(), this.posZ).add(part.getOffset());
                    if (this.world.setBlockState(bPos, ModBlocks.getElevator().getDefaultState(), 3)) {
                        if (!this.world.isRemote) {
                            TileEntity e = this.world.getTileEntity(bPos);
                            if (e instanceof TileEntityElevator) {
                                ((TileEntityCamo) e).setCamoSides(part.getItems());
                                ((TileEntityTransportPart) e).setComputer(this.controller);
                                ((TileEntityElevator) e).setOverlay(part.getOverlay());
                            }
                        }
                    }
                }
            }
        }
        if(world.getMinecraftServer() != null)
        world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, posX,posY,posZ,4,world.provider.getDimension(),
                new SPacketChat( new TextComponentTranslation("entityElevator.arrive", floorName), ChatType.GAME_INFO));
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        //first read length as int
        int j = additionalData.readInt();
        for(int i=0; i< j;i++){
            EntityTransportPart part = new EntityTransportPart(this, BlockPos.fromLong(additionalData.readLong()));
            part.setEntityId(i + this.getEntityId());
            BlockPos offset = part.getOffset();
            part.setPosition(this.posX + offset.getX(),this.posY + offset.getY(),this.posZ + offset.getZ());
            this.parts.add(part);
            part.setOverlay(additionalData.readInt());
            ArrayList<IBlockState> clientCamo = new ArrayList<>();
            for(int k = 0; k<6;k++) {
                IBlockState clientState = Block.getStateById(additionalData.readInt());
                if(clientState.getBlock() == ModBlocks.getElevator()){
                    clientState = clientState.withProperty(BlockCamoBase.CAMO,false);
                }
                clientCamo.add(clientState);
            }
            part.setClientCamo(clientCamo);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(this.parts.size());
        for(EntityTransportPart part: this.parts){
            buffer.writeLong(part.getOffset().toLong());
            buffer.writeInt(part.getOverlay());
            for(IBlockState i:getBlockStates(part.getItems())){
                buffer.writeInt(Block.getStateId(i));
            }
        }
    }

    private Iterable<? extends IBlockState> getBlockStates(NonNullList<ItemStack> camoSides) {
        //noinspection deprecation
        return camoSides.stream().map( s -> s.isEmpty()?
                ModBlocks.getElevator().getDefaultState().withProperty(BlockCamoBase.CAMO, false):
                Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItem().getMetadata(s.getMetadata())))::iterator;
    }

    private void MoveParts() {
        ListIterator<EntityTransportPart> parts = this.parts.listIterator();
        while(parts.hasNext()){
            EntityTransportPart part = parts.next();
            part.prevPosX = part.posX;
            part.prevPosY = part.posY;
            part.prevPosZ = part.posZ;
            BlockPos offset = part.getOffset();
            part.move(MoverType.SELF,
                    (this.posX + offset.getX()) - part.posX,
                    (this.posY + offset.getY()) - part.posY,
                    (this.posZ + offset.getZ()) - part.posZ);
            if (part.collidedVertically) {
                obstructed = part;
                break;
            }
        }

        //move reference based on how the parts moved
        double moved = obstructed != null?obstructed.posY - obstructed.prevPosY:this.motionY;
        this.move(MoverType.SELF,
                this.motionX,
                moved,
                this.motionZ);
        if(obstructed != null) {
            //if obstructed then ensure all parts are synced
            while (parts.hasNext()) {
                EntityTransportPart part = parts.next();
                BlockPos offset = part.getOffset();
                part.move(MoverType.SELF,
                        (this.posX + offset.getX()) - part.posX,
                        (this.posY + offset.getY()) - part.posY,
                        (this.posZ + offset.getZ()) - part.posZ);
            }
        }

        updateRiders(moved);
    }

    private void CalcVelocity() {
        float elevatorSpeed = MathHelper.abs((float) this.motionY);
        if (this.rest == 5) {
            this.setVelocity(0, getDestinationY() > this.posY ? minSpeed: -minSpeed, 0);
            this.rest--;
        } else if (this.rest == 0) {
            float currentAcceleration;
            if (!this.slowingDown
                    && MathHelper.abs(getDestinationY() - (float) this.posY) >= (((elevatorSpeed * elevatorSpeed) - (minSpeed * minSpeed)) / (2f * maxAcceleration))) {
                currentAcceleration = Math.min(maxSpeed - elevatorSpeed, maxAcceleration);
            } else {
                this.slowingDown = true;
                currentAcceleration = Math.max(minSpeed - elevatorSpeed, -maxAcceleration);
            }
            this.addVelocity(0, (getDestinationY() > this.posY ? currentAcceleration : -currentAcceleration), 0);
            if(currentAcceleration != 0) this.markVelocityChanged();
        } else {
            this.rest--;
        }
    }

    private float getDestinationY() {
        return getDest() + 0.5f;
    }

    private boolean isUnObstructed() {
        if(obstructed != null){
            float minVelocity = getDestinationY() > this.posY ? minSpeed: -minSpeed;
            obstructed.move(MoverType.SELF, 0, minVelocity,0);
            if(!obstructed.collidedVertically){
                obstructed.move(MoverType.SELF, 0, minVelocity,0);
                obstructed = null;
            }
        }
        return obstructed == null;
    }

    private static final Predicate<Entity> PossibleRider = (entity) ->
            entity != null && !(entity instanceof EntityTransportPart) && entity.getRidingEntity() == null && (!(entity instanceof EntityPlayerMP) || !((EntityPlayerMP)entity).isSpectator());

    private void updateRiders(double moved) {
        for(Entity part: this.parts) {
            for (Entity rider : this.world.getEntitiesInAABBexcluding(this, part.getEntityBoundingBox().expand(0,0.125,0), PossibleRider::test)) {
                if(rider.motionY < moved) rider.motionY = moved;


                if((rider.getEntityBoundingBox().minY + moved) < part.getEntityBoundingBox().maxY) {
                    rider.move(MoverType.SHULKER,0,part.getEntityBoundingBox().maxY - rider.getEntityBoundingBox().minY + moved, 0);
                }

                rider.onGround = true;
                rider.isAirBorne=false;
                rider.fallDistance = 0;
            }
        }
    }

    public Entity[] getParts() {
        return this.parts.toArray(new Entity[parts.size()]);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setDest(compound.getInteger("dest"));
        rest = compound.getInteger("rest");
        slowingDown = compound.getBoolean("slowingDown");
        controller = NBTUtil.getPosFromTag(compound.getCompoundTag("controller"));
        for(NBTBase p: compound.getTagList("parts",10)){
            EntityTransportPart part = new EntityTransportPart(this, NBTUtil.getPosFromTag((NBTTagCompound) p));
            BlockPos offset = part.getOffset();
            part.setPosition(this.posX + offset.getX(),this.posY + offset.getY(),this.posZ + offset.getZ());
            ItemStackHelper.loadAllItems((NBTTagCompound) p, part.getItems());
            this.parts.add(part);
        }
        if(this.parts.size() == 0){
            this.setDead();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("dest",getDest());
        compound.setInteger("rest",rest);
        compound.setBoolean("slowingDown", slowingDown);
        compound.setTag("controller",NBTUtil.createPosTag(controller));
        NBTTagList parts = new NBTTagList();
        for(EntityTransportPart p: this.parts){
            NBTTagCompound part = NBTUtil.createPosTag(p.getOffset());
            ItemStackHelper.saveAllItems(part, p.getItems());
            parts.appendTag(part);
        }
        compound.setTag("parts",parts);
    }

    @Override
    @Nonnull
    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean attackEntityFromPart(@Nonnull MultiPartEntityPart dragonPart, @Nonnull DamageSource source, float damage) {
        return false;
    }



    @Override
    public void setDead()
    {
        this.isDead = true;
    }

}
