package net.slimevoid.dynamictransport.proxy;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.container.ContainerDynamicMarker;
import net.slimevoid.dynamictransport.container.ContainerFloorSelection;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.GuiLib;
import net.slimevoid.dynamictransport.core.lib.PacketLib;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.ICommonProxy;
import net.slimevoid.library.util.helpers.BlockHelper;
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
        switch(ID){
            case GuiLib.GUIID_FloorSelection:
                TileEntityFloorMarker TE = (TileEntityFloorMarker) BlockHelper.getTileEntity(world,
                        x,
                        y,
                        z,
                        TileEntityFloorMarker.class);
                TileEntityElevatorComputer computer = null;
                if (TE != null) {
                    computer = TE.getParentElevatorComputer();
                }
                if (computer != null) {
                    return new ContainerFloorSelection(player.inventory, computer, world);
                }
                break;
            case GuiLib.GUIID_FLOOR_MARKER:
                TileEntityFloorMarker floorMarker = (TileEntityFloorMarker) BlockHelper.getTileEntity(world,
                        x,
                        y,
                        z,
                        TileEntityFloorMarker.class);
                if (floorMarker != null) {
                    return new ContainerDynamicMarker(player.inventory,floorMarker,world);
                }
                break;
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
