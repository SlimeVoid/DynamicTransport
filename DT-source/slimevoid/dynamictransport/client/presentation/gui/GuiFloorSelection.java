package slimevoid.dynamictransport.client.presentation.gui;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.opengl.GL11;

import slimevoid.dynamictransport.container.ContainerFloorSelection;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.network.packet.PacketFloorSelected;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoidlib.core.SlimevoidCore;
import slimevoidlib.core.lib.CoreLib;
import slimevoidlib.data.Logger;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

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
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F,
                       1.0F,
                       1.0F,
                       1.0F);
        this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k,
                                   l,
                                   0,
                                   0,
                                   this.xSize + 39,
                                   3 * 18 + 17);
        this.drawTexturedModalRect(k,
                                   l + 3 * 18 + 17,
                                   0,
                                   126,
                                   this.xSize + 39,
                                   96);

    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        // create packet
        PacketFloorSelected packet = new PacketFloorSelected(GuiLib.GUIID_FloorSelection, Integer.valueOf(guibutton.displayString), "", this.marker.xCoord, this.marker.yCoord, this.marker.zCoord);

        PacketDispatcher.sendPacketToServer(packet.getPacket());
        this.onGuiClosed();
        FMLClientHandler.instance().getClient().thePlayer.closeScreen();
    }

}
