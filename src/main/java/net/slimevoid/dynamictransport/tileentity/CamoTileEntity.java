package net.slimevoid.dynamictransport.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.slimevoid.dynamictransport.blocks.BaseCamoBlock;
import net.slimevoid.dynamictransport.core.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static net.minecraft.state.properties.BlockStateProperties.LEVEL_0_15;
import static net.slimevoid.dynamictransport.core.DynamicTransport.CAMO;

public class CamoTileEntity extends TileEntity {
    private NonNullList<ItemStack> camoSides = NonNullList.withSize(6, ItemStack.EMPTY);
    private NonNullList<BlockPos> origins = NonNullList.withSize(6, BlockPos.ZERO);
    public static final ModelProperty<List<BlockState>> CAMO_STATE = new ModelProperty<>();
    private static final ModelProperty<NonNullList<BlockPos>> ORIGIN_STATE = new ModelProperty<>();

    //public CamoTileEntity() {
    //    super(RegistryHandler.ELEVATOR_TILE_ENTITIY.get());
    //}

    public CamoTileEntity(TileEntityType<?> type){
        super(type);
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        ItemStackHelper.saveAllItems(compound, this.camoSides);
        ListNBT originTags = new ListNBT();
        for(int i = 0; i < origins.size(); i++) {
            BlockPos origin = origins.get(i);
            if(origin != BlockPos.ZERO) {
                CompoundNBT originTag = new CompoundNBT();
                originTag.putByte("slot", (byte)i);
                originTag.putInt("x", origin.getX());
                originTag.putInt("y", origin.getY());
                originTag.putInt("z", origin.getZ());
                originTags.add(originTag);
            }
        }
        compound.put("origins",originTags);
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        camoSides.replaceAll((a)->ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound,this.camoSides);
        ListNBT originTags = compound.getList("origins",10);
        for(int i = 0; i < originTags.size(); ++i) {
            CompoundNBT origin = originTags.getCompound(i);
            int index = origin.getByte("slot") & 255;
            if (index < origins.size()) {
                this.origins.set(index, new BlockPos(origin.getInt("x"),origin.getInt("y"),origin.getInt("z")));
            }
        }
        super.read(compound);
        requestModelDataUpdate();
        markDirtyClient();
    }

    public void setCamoSide(int index, @Nonnull ItemStack itemIn, BlockPos pos){
        camoSides.set(index,itemIn);
        origins.set(index,pos);
        updateLight();
    }

    private void updateLight() {
        BlockState state = getWorld().getBlockState(getPos()) ;
        OptionalInt light = Arrays.stream(Direction.values()).mapToInt((side)-> {
            int i = side.getOpposite().getIndex();
            BlockState camo = getBlockStates().get(i);
            if (!(camo.getBlock() instanceof BaseCamoBlock))
                return camo.getLightValue(getWorld(), pos);
            return 0;
        }).max();
        int newLight = 0;
        if(light.isPresent())
            newLight = light.getAsInt();

        if(newLight != state.get(LEVEL_0_15)) {
            //need the extra processing here to update lighting
            getWorld().setBlockState(pos, state.with(LEVEL_0_15, newLight));
        } else {
            //not changing the state so bypass the checks
            getWorld().notifyBlockUpdate(pos, state, state, 3);
            getWorld().notifyNeighbors(pos, state.getBlock());
        }
    }

    @Nonnull
    public ItemStack getCamoSide(int index) { return  camoSides.get(index); }
    @Nonnull
    public NonNullList<ItemStack> getCamoSides() { return  camoSides; }
    @Nonnull
    public void setCamoSides(NonNullList<ItemStack> camoSides) {
        this.camoSides = camoSides;
        updateLight();
    }
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        return new SUpdateTileEntityPacket(getPos(), 1, write(nbtTag));
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        write(updateTag);
        return updateTag;
    }

    @Override
    @Nonnull
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(CAMO_STATE, getBlockStates()).withInitial(ORIGIN_STATE, origins).build();
    }

    public List<BlockState> getBlockStates(){
        return camoSides.stream().map( s -> s.isEmpty()?
                this.getWorld().getBlockState(this.getPos()).getBlock().getDefaultState().with(CAMO,false):
                ((BlockItem)s.getItem()).getBlock().getDefaultState()).collect(Collectors.toList());
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            BlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

}
