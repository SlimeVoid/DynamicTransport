package slimevoid.dynamictransport.client.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import slimevoid.dynamictransport.client.network.ClientPacketHandler;
import slimevoid.dynamictransport.client.presentation.gui.GuiDynamicElevator;
import slimevoid.dynamictransport.client.tickhandler.PlayerMotionTickHandler;
import slimevoid.dynamictransport.core.PacketLib;
import slimevoid.dynamictransport.core.lib.GuiLib;
import slimevoid.dynamictransport.proxy.CommonProxy;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoidlib.util.helpers.SlimevoidHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiLib.GUIID_ELEVATOR) {
			TileEntity tileentity = SlimevoidHelper.getBlockTileEntity(	world,
																		x,
																		y,
																		z);
			if (tileentity != null && tileentity instanceof TileEntityElevator) {
				return new GuiDynamicElevator(player, player.inventory, world, (TileEntityElevator) tileentity);
			}
		}
		return null;
	}

	@Override
	public void preInit() {
		super.preInit();
		ClientPacketHandler.init();
		PacketLib.registerClientPacketHandlers();

	}

	@Override
	public void registerRenderInformation() {
		RenderingRegistry.registerEntityRenderingHandler(	slimevoid.dynamictransport.entities.EntityElevator.class,
															new slimevoid.dynamictransport.client.render.RenderElevator());
	}

	@Override
	public void registerTickHandlers() {
		super.registerTickHandlers();
		TickRegistry.registerTickHandler(	new PlayerMotionTickHandler(),
											Side.CLIENT);
	}

}
