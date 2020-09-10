package net.slimevoid.dynamictransport.client.renderer.block;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.slimevoid.dynamictransport.block.BlockCamoBase;
import net.slimevoid.dynamictransport.block.BlockElevator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CamoModel implements IBakedModel {

    private final VertexFormat format;
    private final ArrayList<BakedQuad> overlayQuads;
    private final TextureAtlasSprite sprite;

    public CamoModel(VertexFormat format, TextureAtlasSprite sprite, TextureAtlasSprite overlayIcon) {
        this.format = format;
        double offset = .001;
        this.sprite = sprite;
        this.overlayQuads = new ArrayList<>();
        if(overlayIcon != null) {
            //d
            overlayQuads.add(createQuad(
                    new Vec3d(0, 0 - offset, 0), //north west
                    new Vec3d(1, 0 - offset, 0), //north east
                    new Vec3d(1, 0 - offset, 1), //south east
                    new Vec3d(0, 0 - offset, 1), //south west
                    overlayIcon));
            //u
            overlayQuads.add(createQuad(
                    new Vec3d(0, 1 + offset, 0), //north west
                    new Vec3d(0, 1 + offset, 1), //south west
                    new Vec3d(1, 1 + offset, 1), //south east
                    new Vec3d(1, 1 + offset, 0), //north east
                    overlayIcon));
            //n
            overlayQuads.add(createQuad(
                    new Vec3d(0, 0, 0 - offset), //down west
                    new Vec3d(0, 1, 0 - offset), //up west
                    new Vec3d(1, 1, 0 - offset), //up east
                    new Vec3d(1, 0, 0 - offset), //down east
                    overlayIcon));
            //s
            overlayQuads.add(createQuad(
                    new Vec3d(1, 0, 1 + offset), //down east
                    new Vec3d(1, 1, 1 + offset), //up east
                    new Vec3d(0, 1, 1 + offset), //up west
                    new Vec3d(0, 0, 1 + offset), //down west
                    overlayIcon));
            //w
            overlayQuads.add(createQuad(
                    new Vec3d(0 - offset, 0, 0), //down north
                    new Vec3d(0 - offset, 0, 1), //down south
                    new Vec3d(0 - offset, 1, 1), //up south
                    new Vec3d(0 - offset, 1, 0), //up north
                    overlayIcon));
            //e
            overlayQuads.add(createQuad(
                    new Vec3d(1 + offset, 1, 0), //up north
                    new Vec3d(1 + offset, 1, 1), //up south
                    new Vec3d(1 + offset, 0, 1), //down south
                    new Vec3d(1 + offset, 0, 0), //down north
                    overlayIcon));
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if(!(state instanceof IExtendedBlockState)) return Collections.emptyList();
        List<BakedQuad> quads = getCamoQuads(state,side,rand);

        if(!overlayQuads.isEmpty() && side != null && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
            if ((((IExtendedBlockState) state).getValue(BlockElevator.OVERLAY) & (int)Math.pow(2,side.getIndex())) > 0){
                quads.add(overlayQuads.get(side.getIndex()));
            }
        }
        return quads;
    }

    private List<BakedQuad> getCamoQuads(IBlockState state, EnumFacing side, long rand) {
        List<IBlockState> sides = ((IExtendedBlockState)state).getValue(BlockCamoBase.CAMO_STATE);
        if(sides == null || side == null) return new ArrayList<>();

        IBlockState camoState = sides.get(side.getIndex());
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (
            (layer != null && !camoState.getBlock().canRenderInLayer(camoState, layer))
            || (layer != BlockRenderLayer.SOLID && camoState.getBlock() == state.getBlock())
        ) {
            // always render in the null layer or the block-breaking textures won't show up
            return new ArrayList<>();
        }

        IBlockState cleanState = camoState;
        if(camoState instanceof IExtendedBlockState) cleanState = ((IExtendedBlockState)camoState).getClean();
        List<BakedQuad> quads = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(cleanState).getQuads(camoState, side, rand);

        return quads.stream().map((q) -> new BakedQuad(
                        q.getVertexData(),
                        (side.getIndex() << 8) + q.getTintIndex() + 1,
                        q.getFace(),
                        q.getSprite(),
                        q.shouldApplyDiffuseLighting(),
                        q.getFormat()
                )
        ).collect(Collectors.toList());
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite);
        return builder.build();
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, float u, float v, TextureAtlasSprite sprite) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
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
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
