package slimevoid.dynamictransport.client.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import slimevoid.dynamictransport.client.presentation.gui.GuiFloorSelection;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.entities.EntityElevator;
import slimevoid.dynamictransport.proxy.CommonProxy;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import slimevoidlib.util.helpers.SlimevoidHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiLib.GUIID_FloorSelection) {
			TileEntity tileentity = SlimevoidHelper.getBlockTileEntity(	world,
																		x,
																		y,
																		z);
			if (tileentity != null
				&& tileentity instanceof TileEntityFloorMarker) {
				tileentity = ((TileEntityFloorMarker) tileentity).getParentElevatorComputer();
			}
			if (tileentity != null
				&& tileentity instanceof TileEntityElevatorComputer) {
				return new GuiFloorSelection(player, player.inventory, world, (TileEntityElevatorComputer) tileentity);
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
