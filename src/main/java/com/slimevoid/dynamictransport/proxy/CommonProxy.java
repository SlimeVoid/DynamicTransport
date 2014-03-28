package com.slimevoid.dynamictransport.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.slimevoid.library.ICommonProxy;
import net.slimevoid.library.util.helpers.BlockHelper;

import com.slimevoid.dynamictransport.container.ContainerFloorSelection;
import com.slimevoid.dynamictransport.core.DynamicTransportMod;
import com.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import com.slimevoid.dynamictransport.core.lib.GuiLib;
import com.slimevoid.dynamictransport.core.lib.PacketLib;
import com.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import com.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;

import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy implements ICommonProxy {

    @Override
    public void preInit() {
        NetworkRegistry.INSTANCE.registerGuiHandler(DynamicTransportMod.instance,
                                                    DynamicTransportMod.proxy);
        PacketLib.registerPacketHandlers();
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {

    }

    @Override
    public String getMinecraftDir() {
        return null;
    }

    @Override
    public void registerConfigurationProperties(File configFile) {
        ConfigurationLib.CommonConfig(configFile);

    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
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
                return new ContainerFloorSelection(player.inventory, computer, world);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void registerRenderInformation() {
    }

    @Override
    public void registerTileEntitySpecialRenderer(Class<? extends TileEntity> clazz) {
    }

    @Override
    public boolean isClient(World world) {
        return world.isRemote;
    }

    @Override
    public void registerTickHandlers() {
    }

    @Override
    public void registerEventHandlers() {
    }

}
