package slimevoid.dynamictransport.client.presentation.gui;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import slimevoid.dynamictransport.container.ContainerDynamicElevator;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;

public class GuiDynamicElevator extends GuiContainer {

	private TileEntityElevator elevator;

	public GuiDynamicElevator(EntityPlayer entityplayer, InventoryPlayer playerInventory, World world,
			TileEntityElevator elevator) {
		super(new ContainerDynamicElevator(playerInventory, elevator));
		this.elevator = elevator;
		//TODO:On Init Scan for floors if dirty flag set
		elevator.ScanFloors(true, -1, world.getHeight());
		this.ySize = 222;
		
		
	}
	
	 @Override
     public void initGui() {
             super.initGui();
     		 TreeMap<Integer, String> floors = this.elevator.GetFloorList();  
             int j2 = 0;
             for (Map.Entry<Integer, String> entry : floors.entrySet())
             {                  
             
            	 j2++;
                  GuiButton curButton;
                  
                          curButton = new GuiButton(
                                          j2,
                                                  this.guiLeft + 10 + ((j2 - 1) % 5) * 25,
                                                  this.guiTop + this.ySize - 40 - 25 * ((j2 - 1) / 5),
                                                  20,
                                                  20,
                                                  (new StringBuilder()).append(entry.getValue()==null || entry.getValue().trim()==""?entry.getKey():entry.getValue()).toString());
                 
                  if (this.elevator.yCoord == entry.getKey()) {
                          curButton.enabled = false;
                  }
                  this.buttonList.add(curButton);
                  
                  if(j2 == 70)break;
     	}
     }
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiLib.GUI_ELEVATOR);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		
		
		
		
     }
     
     
	}

