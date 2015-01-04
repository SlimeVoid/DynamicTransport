package net.slimevoid.dynamictransport.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.blocks.BlockTransportBase;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;

import net.slimevoid.dynamictransport.entities.EntityElevatorPart;
import org.lwjgl.opengl.GL11;

public class RenderElevator extends Render {

    public RenderElevator() {
        shadowSize = 0.5F;
    }

    public void renderElevatorEntity(Block elevator, World world, int x, int y, int z, IIcon[] textureData, short overlay) {
        this.field_147909_c/* renderBlocks */.setRenderBoundsFromBlock(elevator);

        float f1 = 0.5F;
        float f2 = 1.0F;
        float f3 = 0.8F;
        float f4 = 0.6F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(elevator.getMixedBrightnessForBlock(world,
                                                                      x,
                                                                      y,
                                                                      z));
        float f5 = 1.0F;
        float f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f1 * f6,
                                     f1 * f6,
                                     f1 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceYNeg(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[0]);
        f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f2 * f6,
                                     f2 * f6,
                                     f2 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceYPos(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[1]);
        f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f3 * f6,
                                     f3 * f6,
                                     f3 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceXPos(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[2]);
        f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f3 * f6,
                                     f3 * f6,
                                     f3 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceXNeg(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[3]);
        f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f4 * f6,
                                     f4 * f6,
                                     f4 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceZNeg(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[4]);
        f6 = 1.0F;

        if (f6 < f5) {
            f6 = f5;
        }

        tessellator.setColorOpaque_F(f4 * f6,
                                     f4 * f6,
                                     f4 * f6);
        this.field_147909_c/* renderBlocks */.renderFaceZPos(elevator,
                                                             -0.5D,
                                                             -0.5D,
                                                             -0.5D,
                                                             textureData[5]);
        if ((overlay & 1) == 1){
            field_147909_c.renderFaceYNeg(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }
        if ((overlay & 2) == 2){
            field_147909_c.renderFaceYPos(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }
        if ((overlay & 4) == 4){
            field_147909_c.renderFaceZNeg(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }
        if ((overlay & 8) == 8){
            field_147909_c.renderFaceZPos(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }
        if ((overlay & 16) == 16){
            field_147909_c.renderFaceXNeg(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }
        if ((overlay & 32) == 32){
            field_147909_c.renderFaceXPos(elevator, -0.5D,
                    -0.5D,
                    -0.5D, BlockTransportBase.getIconSideOverlay());
        }

        tessellator.draw();


    }

    public void doRenderElevator(EntityElevatorPart elevator, double d, double d1, double d2, float f, float f1) {
        if (elevator.ticksExisted <= 1) return;
        GL11.glPushMatrix();
        Block block = ConfigurationLib.blockTransportBase;
        World world = elevator.worldObj;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glTranslatef((float) d,
                          (float) d1,
                          (float) d2);
        // GL11.glScalef(-1F, -1F, 1.0F); - ceilings?
        bindTexture(TextureMap.locationBlocksTexture);

        ItemStack camoItem = elevator.getCamoItem();
        IIcon textureData[] = new IIcon[6];
        if (camoItem != null && camoItem.getItem() != null) {
            for (int i = 0; i < 6; i++) {
                textureData[i] = Block.getBlockFromItem(camoItem.getItem()).getIcon(i,
                                                                                    camoItem.getItemDamage());

            }
        } else {
            int foo = 0;
            for (int i = 0; i < 6; i++) {
                textureData[i] = ConfigurationLib.blockTransportBase.getIcon(i,
                                                                             0);

            }
        }

        renderElevatorEntity(block,
                             world,
                             MathHelper.floor_double(elevator.posX),
                             MathHelper.floor_double(elevator.posY),
                             MathHelper.floor_double(elevator.posZ),
                             textureData,
                elevator.getOverlay());
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
        doRenderElevator((EntityElevatorPart) entity,
                         d,
                         d1,
                         d2,
                         f,
                         f1);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
}
