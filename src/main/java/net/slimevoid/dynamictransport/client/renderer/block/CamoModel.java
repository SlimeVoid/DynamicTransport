package net.slimevoid.dynamictransport.client.renderer.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.slimevoid.dynamictransport.core.RegistryHandler;
import net.slimevoid.dynamictransport.tileentity.CamoTileEntity;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;
import net.slimevoid.dynamictransport.tileentity.TransportPartTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.slimevoid.dynamictransport.core.DynamicTransport.CAMO;
import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_TOOL;

public class CamoModel implements IDynamicBakedModel {
    private static BlockState lastRenderedCamo = null;

    //should ask Forge to persist the modeldata and facing information down to here for now need to rely on a static value to persist the last rendered camo
    public static int getColor(BlockState blockState, ILightReader iLightReader, BlockPos pos, int i) {
        int color = -1;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if(player != null) {
            for(Hand hand : Hand.values()) {
                ItemStack held = player.getHeldItem(hand);
                if (held.getItem() == ELEVATOR_TOOL.get() && held.hasTag()) {
                    BlockPos controller = NBTUtil.readBlockPos(held.getTag().getCompound("pos"));
                    TileEntity e = iLightReader.getTileEntity(pos);
                    if (e instanceof TransportPartTileEntity && controller.equals(((TransportPartTileEntity) e).getController())) {
                        color = 0xFF0000;
                        break;
                    }
                }
            }
        }
        if(lastRenderedCamo != null && lastRenderedCamo.getBlock() != blockState.getBlock() && i != -2)
            color = GetColorMulti(color, Minecraft.getInstance().getBlockColors().getColor(lastRenderedCamo, iLightReader, pos,i));
        return color;
    }



    private static int GetColorMulti(int raw,int raw2) {
        if(raw == -1 && raw2 != -1) return raw2;
        if(raw2 == -1) return raw;

        int l = (int)(((float)(raw & 255) * ((float) (raw2 >> 16 & 255)) / 255.0F));
        int i1  = (int)(((float)(raw >> 8 & 255) * ((float) (raw2 >> 8 & 255)) / 255.0F));
        int j1 = (int)(((float)(raw >> 16 & 255) * ((float) (raw2 & 255)) / 255.0F));

        raw &= -16777216;
        raw |= j1 << 16 | i1 << 8 | l;


        return raw;
    }
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BlockState> sides = extraData.getData(CamoTileEntity.CAMO_STATE);
        if(sides == null || side == null) return Collections.emptyList();
        BlockState camoState = sides.get(side.getIndex());
        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && ! RenderTypeLookup.canRenderInLayer(camoState, layer)) {
            // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        lastRenderedCamo = camoState;
        //noinspection deprecation
        List<BakedQuad> quads = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState).getQuads(camoState, side, rand);
        return quads.stream().map((q) -> q.hasTintIndex()? q:new BakedQuad(
                q.getVertexData(),
                -2, //apply dummy tint index
                q.getFace(),
                q.func_187508_a(),
                q.shouldApplyDiffuseLighting()
                )
        ).collect(Collectors.toList());
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean func_230044_c_() {
        return true;
    } //is shaded in GUI

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return  Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(RegistryHandler.ELEVATOR_BLOCK.get().getDefaultState().with(CAMO,false)).getParticleTexture(data);
    }
}
