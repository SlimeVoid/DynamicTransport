package slimevoid.dynamictransport.client.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import slimevoid.dynamictransport.client.presentation.gui.GuiFloorSelection;
import slimevoid.dynamictransport.container.ContainerFloorSelection;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.entities.EntityElevator;
import slimevoid.dynamictransport.proxy.CommonProxy;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import slimevoidlib.util.helpers.BlockHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiLib.GUIID_FloorSelection) {
			TileEntityFloorMarker tileentity = (TileEntityFloorMarker) BlockHelper.getTileEntity(	world,
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
		}
		return null;
	}

	@Override
	public void preInit() {
		super.preInit();

	}

	@Override
	public void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(	EntityElevator.class,
															new slimevoid.dynamictransport.client.render.RenderElevator());
	}

	@Override
	public void registerConfigurationProperties(File configFile) {
		super.registerConfigurationProperties(configFile);
		ConfigurationLib.ClientConfig();
	}

}
