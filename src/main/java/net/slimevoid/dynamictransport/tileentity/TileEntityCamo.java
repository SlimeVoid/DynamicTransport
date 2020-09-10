package net.slimevoid.dynamictransport.tileentity;

import net.slimevoid.dynamictransport.block.BlockCamoBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class TileEntityCamo extends TileEntity {
    private NonNullList<ItemStack> camoSides = NonNullList.withSize(6, ItemStack.EMPTY);
    private NonNullList<BlockPos> origins = NonNullList.withSize(6, BlockPos.ORIGIN);

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, this.camoSides);
        NBTTagList originTags = new NBTTagList();
        for(int i = 0; i < origins.size(); i++) {
            BlockPos origin = origins.get(i);
            if(origin != BlockPos.ORIGIN) {
                NBTTagCompound originTag = NBTUtil.createPosTag(origin);;
                originTag.setByte("slot", (byte)i);
                originTags.appendTag(originTag);
            }
        }
        compound.setTag("origins",originTags);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        camoSides.replaceAll((a)->ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound,this.camoSides);
        NBTTagList originTags = compound.getTagList("origins",10);
        for(int i = 0; i < originTags.tagCount(); ++i) {
            NBTTagCompound origin = originTags.getCompoundTagAt(i);
            int index = origin.getByte("slot") & 255;
            if (index < origins.size()) {
                this.origins.set(index, NBTUtil.getPosFromTag(origin));
            }
        }
        super.readFromNBT(compound);
    }

    public void setCamoSide(int index, ItemStack itemIn, BlockPos pos) {
        camoSides.set(index,itemIn);
        origins.set(index,pos);
        markDirtyClient();
    }

    public int getLightValue() {
        OptionalInt light = Arrays.stream(EnumFacing.values()).mapToInt((side)-> {
            int i = side.getOpposite().getIndex();
            IBlockState camo = getBlockStates().get(i);
            if (!(camo.getBlock() instanceof BlockCamoBase))
                return camo.getLightValue(getWorld(), pos);
            return 0;
        }).max();
        int newLight = 0;
        if(light.isPresent())
            newLight = light.getAsInt();

        return newLight;
    }

    @Nonnull
    public ItemStack getCamoSide(int index) { return  camoSides.get(index); }
    @Nonnull
    public NonNullList<ItemStack> getCamoSides() { return camoSides; }
    @Nonnull
    public void setCamoSides(NonNullList<ItemStack> camoSides) {
        this.camoSides = camoSides;
    }

    public List<IBlockState> getBlockStates(){
        IExtendedBlockState def = ((IExtendedBlockState) this.getWorld().getBlockState(this.getPos()).withProperty(BlockCamoBase.CAMO,false));
        return camoSides.stream().map( s -> s.isEmpty()?
                def:
                Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItem().getMetadata(s.getMetadata()))
                ).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, writeToNBT(new NBTTagCompound()));
    }
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        int old = getLightValue();
        readFromNBT(pkt.getNbtCompound());
        if(old != getLightValue()) this.getWorld().checkLight(pos);
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }
    protected void markDirtyClient() {
        markDirty();
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        //getWorld().scheduleBlockUpdate(pos,this.getBlockType(),0,0);
    }
}
