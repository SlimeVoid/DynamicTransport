package slimevoid.dynamictransport.client.presentation.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import slimevoid.dynamictransport.container.ContainerFloorSelection;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;

public class GuiFloorSelection extends GuiContainer {

	public GuiFloorSelection(EntityPlayer entityplayer, InventoryPlayer playerInventory, World world, TileEntityElevatorComputer elevator) {
		super(new ContainerFloorSelection(playerInventory, elevator));
		// TODO Auto-generated constructor stub
	}

	private TileEntityFloorMarker	marker;

	@Override
	public void initGui() {

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(	1.0F,
						1.0F,
						1.0F,
						1.0F);
		this.mc.getTextureManager().bindTexture(GuiLib.GUI_FLOOR_MARKER);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(	k,
									l,
									0,
									0,
									this.xSize,
									3 * 18 + 17);
		this.drawTexturedModalRect(	k,
									l + 3 * 18 + 17,
									0,
									126,
									this.xSize,
									96);

	}
}
