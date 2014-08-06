package net.slimevoid.dynamictransport.client.proxy;

import java.io.File;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.client.event.PlayerTick;
import net.slimevoid.dynamictransport.client.presentation.gui.GuiDynamicMarker;
import net.slimevoid.dynamictransport.client.presentation.gui.GuiFloorSelection;
import net.slimevoid.dynamictransport.client.render.BlockElevatorRenderer;
import net.slimevoid.dynamictransport.container.ContainerDynamicMarker;
import net.slimevoid.dynamictransport.container.ContainerFloorSelection;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.entities.EntityElevatorPart;
import net.slimevoid.dynamictransport.entities.EntityMasterElevator;
import net.slimevoid.dynamictransport.proxy.CommonProxy;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.util.helpers.BlockHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID){
        case GuiLib.GUIID_FloorSelection:
        	TileEntityFloorMarker tileEntity = (TileEntityFloorMarker) BlockHelper.getTileEntity(world,
                    x,
                    y,
                    z,
                    TileEntityFloorMarker.class);
        	TileEntityElevatorComputer computer = null;
    		if (tileEntity != null) {
    			computer = tileEntity.getParentElevatorComputer();
			}else{
                TileEntityElevator tileElevator = (TileEntityElevator) BlockHelper.getTileEntity(world,
                        x,
                        y,
                        z,
                        TileEntityElevator.class);
                if (tileElevator != null){
                    computer = tileElevator.getParentElevatorComputer();
                }
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
        RenderingRegistry.registerEntityRenderingHandler(EntityElevatorPart.class,
                                                         new net.slimevoid.dynamictransport.client.render.RenderElevator());
        RenderingRegistry.registerBlockHandler(	ConfigurationLib.ElevatorRenderId,
                new BlockElevatorRenderer());
    }

    @Override
    public void registerConfigurationProperties(File configFile) {
        super.registerConfigurationProperties(configFile);
        ConfigurationLib.ClientConfig();
    }

    @Override
    public void registerEventHandlers() {
        FMLCommonHandler.instance().bus().register(new PlayerTick());
    }
}
