package net.slimevoid.dynamictransport.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.slimevoid.dynamictransport.blocks.BlockTransportBase;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.library.util.helpers.BlockHelper;

import org.lwjgl.opengl.GL11;

/**
 * Created by Allen on 7/18/2014.
 */
public class BlockElevatorRenderer {//implements ISimpleBlockRenderingHandler {
    /**@Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (modelId == ConfigurationLib.ElevatorRenderId) {
            Tessellator tessellator = Tessellator.instance;
            block.setBlockBoundsForItemRender();
            renderer.setRenderBoundsFromBlock(block);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
            tessellator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        boolean rendered = renderer.renderStandardBlock(block,x,y,z);
        //add OverLay Logic
        TileEntityElevator tile = (TileEntityElevator)BlockHelper.getTileEntity(world,x,y,z,TileEntityElevator.class);
        if(tile != null) {

            if ((tile.getOverlay() & 1) == 1){
                renderer.renderFaceYNeg(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
            if ((tile.getOverlay() & 2) == 2){
                renderer.renderFaceYPos(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
            if ((tile.getOverlay() & 4) == 4){
                renderer.renderFaceZNeg(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
            if ((tile.getOverlay() & 8) == 8){
                renderer.renderFaceZPos(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
            if ((tile.getOverlay() & 16) == 16){
                renderer.renderFaceXNeg(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
            if ((tile.getOverlay() & 32) == 32){
                renderer.renderFaceXPos(block, (double) x, (double) y, (double) z, BlockTransportBase.getIconSideOverlay());
            }
        }
            return rendered;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return -1;
    }**/
}
