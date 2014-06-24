package net.slimevoid.dynamictransport.client.presentation.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.slimevoid.dynamictransport.container.ContainerDynamicMarker;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.core.SlimevoidCore;
import net.slimevoid.library.core.lib.CoreLib;
import net.slimevoid.library.data.Logger;
import org.lwjgl.input.Keyboard;

import org.lwjgl.opengl.GL11;

public class GuiDynamicMarker extends GuiContainer {

    private TileEntityFloorMarker marker;
    private GuiTextField nameField;
    private String floorName = "";
    private int floorY;
    public boolean active = true;

    public GuiDynamicMarker(ContainerDynamicMarker container) {
    	 super(container);
         if (container.getMarker() != null
             && container.getMarker() instanceof TileEntityFloorMarker) {
             marker = (TileEntityFloorMarker) container.getMarker();

         } else {
             SlimevoidCore.console(CoreLib.MOD_ID,
                                   "Failed build Floor Marker GUI",
                                   Logger.LogLevel.WARNING.ordinal());
         }

        this.xSize = 176+39;
        this.ySize = 45;
        this.floorY = this.marker.getFloorY();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiTop -= 25;
        this.nameField = new GuiTextField(this.fontRendererObj, this.guiLeft + 10, this.guiTop + 25, 89, this.fontRendererObj.FONT_HEIGHT);
        this.nameField.setMaxStringLength(15);
        this.nameField.setEnableBackgroundDrawing(true);
        this.nameField.setVisible(true);
        this.nameField.setTextColor(16777215);
        this.nameField.setCanLoseFocus(false);
        this.nameField.setFocused(true);
        this.nameField.setText(marker.getFloorName());

        this.buttonList.add(0, new GuiButton(0,this.guiLeft + 10,this.guiTop + 70,
                //I18n.format("slimevoid.container.floormarker.submitName", new Object[0])
                "Save"
        ));
        this.buttonList.add(1, new GuiButton(1,this.guiLeft + 10,this.guiTop + 40,20,20,

                "-"
        ));
        this.buttonList.add(2, new GuiButton(2,this.guiLeft + 50,this.guiTop + 40,20,20,

                "+"
        ));
    }

    @Override
    protected void drawGuiContainerForegroundLayer (int par1, int par2)
    {
        this.fontRendererObj.drawString(I18n.format("slimevoid.container.floormarker.name", new Object[0]), 10, 10, 0xffffff);
        this.fontRendererObj.drawString(Integer.toString(floorY), 35, 45, 0xffffff);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F,
                1.0F,
                1.0F,
                1.0F);
        this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);

        this.drawTexturedModalRect(guiLeft,
                guiTop,
                0,
                0,
                this.xSize,
                this.ySize - 25);
        this.drawTexturedModalRect(guiLeft,
                guiTop + this.ySize - 25,
                0,
                126,
                this.xSize,
                this.ySize);
        this.nameField.drawTextBox();
    }

    @Override
    public void updateScreen ()
    {
        super.updateScreen();
        this.nameField.updateCursorCounter();
    }

    protected void keyTyped(char par1, int par2)
    {
            if (!this.checkHotbarKeys(par2))
            {
                if (this.nameField.textboxKeyTyped(par1, par2))
                {
                    this.floorName = this.nameField.getText();
                }
                else
                {
                    super.keyTyped(par1, par2);
                }
            }
    }

    protected void actionPerformed(GuiButton Button)
    {
        if (Button.enabled)
        {
            switch (Button.id) {
                case 0:
                    //save
                    PacketLib.sendMarkerConfiguration(this.floorY,
                            this.floorName,
                            this.marker.xCoord,
                            this.marker.yCoord,
                            this.marker.zCoord);
                    break;
                case 1:
                    this.floorY -= 1;
                    break;
                case 2:
                    this.floorY += 1;
                    break;
        }
        }
    }
}
