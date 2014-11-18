package net.slimevoid.dynamictransport.client.presentation.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.SortedMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.slimevoid.dynamictransport.container.ContainerFloorSelection;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.library.core.SlimevoidCore;
import net.slimevoid.library.core.lib.CoreLib;
import net.slimevoid.library.data.Logger;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class GuiFloorSelection extends GuiContainer {
    public GuiFloorSelection(ContainerFloorSelection container) {
        super(container);
        if (container.getComputer() != null
            && container.getComputer() instanceof TileEntityElevatorComputer) {
            marker = (TileEntityElevatorComputer) container.getComputer();
        } else {
            SlimevoidCore.console(CoreLib.MOD_ID,
                                  "Failed build Floor Marker GUI",
                                  Logger.LogLevel.WARNING.ordinal());
        }
        this.xSize = 176+39;
    }

    protected TileEntityElevatorComputer marker;

    @Override
    public void initGui() {
        super.initGui();
        // get list of floors
        SortedMap<Integer, ArrayList<String>> floorList = marker.getFloorList();
        int x = (this.width - this.xSize) / 2 + 10;
        int y = ((this.height - this.ySize) / 2) + 130;
        int id = 0;
        for (Entry<Integer, ArrayList<String>> set : floorList.entrySet()) {
            this.buttonList.add(new GuiFloorButton(id++, x, y, 80, 20, set.getKey().toString(), set.getValue().get(0)));
            if (y < ((this.height - this.ySize) / 2) + 30) {
                x += 90;
                y = ((this.height - this.ySize) / 2) + 130;
            } else {
                y -= 30;
            }
        }



    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j){
        for (Object x : buttonList) {
            GuiFloorButton set = (GuiFloorButton)x;
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
    protected void actionPerformed(GuiButton button) {
        GuiFloorButton guibutton;
        if (button instanceof GuiFloorButton) {
            guibutton = (GuiFloorButton) button;
        } else {
            System.out.println("Something went wrong!");
            return;
        }
        String floorName = "";
        for(String name :marker.getFloorList().get(Integer.parseInt(guibutton.getFloorLevel()))){
            floorName = name + ", ";
        }
        if (floorName.length() > 0) {
            floorName = floorName.substring(0,floorName.length() - 2);
        }
        PacketLib.sendFloorSelection(guibutton.getFloorLevel(),
                floorName,
                this.marker.xCoord,
                this.marker.yCoord,
                this.marker.zCoord);

        this.onGuiClosed();
        FMLClientHandler.instance().getClient().thePlayer.closeScreen();
    }
}
