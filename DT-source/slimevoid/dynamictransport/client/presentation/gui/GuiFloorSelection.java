package slimevoid.dynamictransport.client.presentation.gui;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import slimevoid.dynamictransport.container.ContainerFloorSelection;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.network.packet.PacketFloorSelected;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiFloorSelection extends GuiContainer {

	public GuiFloorSelection(EntityPlayer entityplayer, InventoryPlayer playerInventory, World world, TileEntityElevatorComputer elevator) {
		super(new ContainerFloorSelection(playerInventory, elevator));
		// TODO Auto-generated constructor stub
		marker = elevator;
	}

	private TileEntityElevatorComputer	marker;

	@Override
	public void initGui() {
		// get list of floors
		SortedMap<Integer, ArrayList<String>> floorList = marker.getFloorList();
		int x = 140;
		int y = 165;
		int id = 0;
		for (Entry<Integer, ArrayList<String>> set : floorList.entrySet()) {
			this.buttonList.add(new GuiButton(id++, x, y, 20, 20, set.getKey().toString()));
			if (y < 60) {
				x += 30;
				y = 165;
			} else {
				y -= 30;
			}

		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(	1.0F,
						1.0F,
						1.0F,
						1.0F);
		this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(	k,
									l,
									0,
									0,
									this.xSize + 39,
									3 * 18 + 17);
		this.drawTexturedModalRect(	k,
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
