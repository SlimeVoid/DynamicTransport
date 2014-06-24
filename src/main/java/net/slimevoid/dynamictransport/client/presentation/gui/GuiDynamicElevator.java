package net.slimevoid.dynamictransport.client.presentation.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.container.ContainerDynamicElevator;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;

import org.lwjgl.opengl.GL11;

public class GuiDynamicElevator extends GuiContainer {

    private TileEntityElevator elevator;

    public GuiDynamicElevator(EntityPlayer entityplayer, InventoryPlayer playerInventory, World world, TileEntityElevator elevator) {
        super(new ContainerDynamicElevator(playerInventory, elevator));
        this.elevator = elevator;
        // TODO:On Init Scan for floors if dirty flag set

        this.ySize = 222;

    }

    @Override
    public void initGui() {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F,
                       1.0F,
                       1.0F,
                       1.0F);
        this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);
        this.drawTexturedModalRect(this.guiLeft,
                                   this.guiTop,
                                   0,
                                   0,
                                   this.xSize,
                                   this.ySize);

    }

}
