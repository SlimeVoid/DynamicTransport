package net.slimevoid.dynamictransport.client.renderer.entity;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockRenderLayer;
import net.slimevoid.dynamictransport.block.BlockCamoBase;
import net.slimevoid.dynamictransport.block.BlockElevator;
import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.entity.EntityElevator;
import net.slimevoid.dynamictransport.entity.EntityTransportPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ElevatorRenderer extends Render<EntityElevator> {
    public ElevatorRenderer(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }


    @Override
    public void doRender(EntityElevator entity, double x, double y, double z, float f, float f1) {
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        World world = entity.getWorld();
        int i =0;
        //loop through each part
        for (Entity e:entity.getParts()) {
            EntityTransportPart part = ((EntityTransportPart)e);
            BlockPos offset = part.getOffset();
            BlockPos blockpos = new BlockPos(
                    entity.posX, //get x
                    entity.getEntityBoundingBox().maxY,
                    entity.posZ //get z
            );
            IBlockState iblockstate = ModBlocks.getElevator().getDefaultState();
            IBlockState clean = iblockstate;
            if(clean instanceof IExtendedBlockState)
            {
                clean = ((IExtendedBlockState)clean).getClean();
                iblockstate = ((IExtendedBlockState)iblockstate)
                        .withProperty(BlockCamoBase.CAMO_STATE, part.getClientBlockStates())
                        .withProperty(BlockElevator.OVERLAY, part.getOverlay());
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            if (this.renderOutlines)
            {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(this.getTeamColor(entity));
            }

            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
            GlStateManager.translate(
                    (float)(x - (double)blockpos.getX() - 0.5D + offset.getX()),
                    (float)(y - (double)blockpos.getY() + offset.getY()),
                    (float)(z - (double)blockpos.getZ() - 0.5D + offset.getZ()));
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBakedModel model = blockrendererdispatcher.getModelForState(clean);
            for(BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);
                blockrendererdispatcher.getBlockModelRenderer().renderModel(world,
                        model,
                        iblockstate,
                        blockpos,
                        bufferbuilder, false,
                        MathHelper.getPositionRandom(entity.getOrigin()));
            }
            net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
            blockrendererdispatcher.getBlockModelRenderer().renderModel(world,
                    model,
                    iblockstate,
                    blockpos,
                    bufferbuilder, false,
                    MathHelper.getPositionRandom(entity.getOrigin()));
            tessellator.draw();

            if (this.renderOutlines)
            {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            i++;
        }
        super.doRender(entity, x, y, z, f, f1);
    }
    @Override
    public ResourceLocation getEntityTexture(EntityElevator entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
