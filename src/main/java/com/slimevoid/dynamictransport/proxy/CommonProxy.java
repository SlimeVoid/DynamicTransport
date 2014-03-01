package com.slimevoid.dynamictransport.proxy;

import java.io.File;

import com.slimevoid.dynamictransport.container.ContainerFloorSelection;
import com.slimevoid.dynamictransport.core.DynamicTransportMod;
import com.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import com.slimevoid.dynamictransport.core.lib.GuiLib;
import com.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import com.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import com.slimevoid.library.ICommonProxy;
import com.slimevoid.library.IPacketHandling;
import com.slimevoid.library.util.helpers.BlockHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;

public class CommonProxy implements ICommonProxy {

    @Override
    public void preInit() {

        NetworkRegistry.instance().registerGuiHandler(DynamicTransportMod.instance,
                                                      DynamicTransportMod.proxy);

    }

    @Override
    public String getMinecraftDir() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerRenderInformation() {
        // TODO Auto-generated method stub

    }

    @Override
    public IPacketHandling getPacketHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerTileEntitySpecialRenderer(Class<? extends TileEntity> clazz) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClient(World world) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionClosed(INetworkManager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerTickHandlers() {

    }

    @Override
    public void registerEventHandlers() {

    }

}
