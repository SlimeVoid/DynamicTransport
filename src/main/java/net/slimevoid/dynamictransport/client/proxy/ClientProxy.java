package net.slimevoid.dynamictransport.client.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.client.presentation.gui.GuiDynamicMarker;
import net.slimevoid.dynamictransport.client.presentation.gui.GuiFloorSelection;
import net.slimevoid.dynamictransport.container.ContainerDynamicMarker;
import net.slimevoid.dynamictransport.container.ContainerFloorSelection;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.entities.EntityElevator;
import net.slimevoid.dynamictransport.proxy.CommonProxy;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.util.helpers.BlockHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID){
        case GuiLib.GUIID_FloorSelection:
        	TileEntityFloorMarker tileentity = (TileEntityFloorMarker) BlockHelper.getTileEntity(world,
                    x,
                    y,
                    z,
                    TileEntityFloorMarker.class);
        	TileEntityElevatorComputer computer = null;
    		if (tileentity != null) {
    			computer = tileentity.getParentElevatorComputer();
			}
			if (computer != null) {
				return new GuiFloorSelection(new ContainerFloorSelection(player.inventory, computer, world));
			}
        	break;
        case GuiLib.GUIID_FLOOR_MARKER:
        	TileEntityFloorMarker marker = (TileEntityFloorMarker) BlockHelper.getTileEntity(world,
                    x,
                    y,
                    z,
                    TileEntityFloorMarker.class);
        	if(marker != null){
        		return new GuiDynamicMarker(new ContainerDynamicMarker(player.inventory, marker, world));
        	}
        }
        return null;
    }

    @Override
    public void preInit() {
        super.preInit();
        PacketLib.registerClientPacketHandlers();
    }

    @Override
    public void registerRenderInformation() {
        RenderingRegistry.registerEntityRenderingHandler(EntityElevator.class,
                                                         new net.slimevoid.dynamictransport.client.render.RenderElevator());
    }

    @Override
    public void registerConfigurationProperties(File configFile) {
        super.registerConfigurationProperties(configFile);
        ConfigurationLib.ClientConfig();
    }

}
