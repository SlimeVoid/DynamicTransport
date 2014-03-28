package com.slimevoid.dynamictransport.client.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.slimevoid.library.util.helpers.BlockHelper;

import com.slimevoid.dynamictransport.client.presentation.gui.GuiFloorSelection;
import com.slimevoid.dynamictransport.container.ContainerFloorSelection;
import com.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import com.slimevoid.dynamictransport.core.lib.GuiLib;
import com.slimevoid.dynamictransport.core.lib.PacketLib;
import com.slimevoid.dynamictransport.entities.EntityElevator;
import com.slimevoid.dynamictransport.proxy.CommonProxy;
import com.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import com.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiLib.GUIID_FloorSelection) {
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
                                                         new com.slimevoid.dynamictransport.client.render.RenderElevator());
    }

    @Override
    public void registerConfigurationProperties(File configFile) {
        super.registerConfigurationProperties(configFile);
        ConfigurationLib.ClientConfig();
    }

}
