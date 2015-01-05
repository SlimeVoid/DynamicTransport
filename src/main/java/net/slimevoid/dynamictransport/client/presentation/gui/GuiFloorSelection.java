package net.slimevoid.dynamictransport.client.presentation.gui;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.slimevoid.dynamictransport.container.ContainerFloorSelection;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportComputer;
import net.slimevoid.library.core.SlimevoidCore;
import net.slimevoid.library.core.lib.CoreLib;
import net.slimevoid.library.data.Logger;

import org.lwjgl.opengl.GL11;

public class GuiFloorSelection extends GuiContainer {
    public GuiFloorSelection(ContainerFloorSelection container) {
        super(container);
        if (container.getComputer() != null
            && container.getComputer() instanceof TileEntityTransportComputer) {
            marker = (TileEntityTransportComputer) container.getComputer();
        } else {
            SlimevoidCore.console(CoreLib.MOD_ID,
                                  "Failed build Floor Marker GUI",
                                  Logger.LogLevel.WARNING.ordinal());
        }
        this.xSize = 176+39;
    }

    protected TileEntityTransportComputer marker;

    @Override
    public void initGui() {
        super.initGui();
        // get list of floors
        SortedMap<Integer, ArrayList<String>> floorList = marker.getFloorList();
        int x = (this.width - this.xSize) / 2 + 10;
        int y = ((this.height - this.ySize) / 2) + 130;
        int id = 0;
        for (Entry<Integer, ArrayList<String>> set : floorList.entrySet()) {
            this.buttonList.add(new GuiButton(id++, x, y, 20, 20, set.getKey().toString()));
            if (y < ((this.height - this.ySize) / 2) + 30) {
                x += 30;
                y = ((this.height - this.ySize) / 2) + 130;
            } else {
                y -= 30;
            }
        }



    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j){
        for (Object x : buttonList) {
            GuiButton set = (GuiButton)x;
            if (i >= set.xPosition && j >= set.yPosition && i < set.xPosition + 20 && j < set.yPosition + 20){

            }
        }

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F,
                       1.0F,
                       1.0F,
                       1.0F);
        this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft,
                guiTop,
                0,
                0,
                this.xSize,
                this.ySize - 5);
        this.drawTexturedModalRect(guiLeft,
                guiTop + this.ySize - 5,
                0,
                207,
                this.xSize,
                5);

    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        String floorName = "";
        for(String name :marker.getFloorList().get(Integer.parseInt(guibutton.displayString))){
            floorName = name + ", ";
        }
        if (floorName.length() > 0) {
            floorName = floorName.substring(0,floorName.length() - 2);
        }
        PacketLib.sendFloorSelection(guibutton.displayString,
                floorName,
                this.marker.getPos());

        this.onGuiClosed();
        FMLClientHandler.instance().getClient().thePlayer.closeScreen();
    }
}
