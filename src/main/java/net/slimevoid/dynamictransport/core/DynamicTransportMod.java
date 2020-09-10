package net.slimevoid.dynamictransport.core;

import com.google.common.graph.Network;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.item.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.slimevoid.dynamictransport.network.ModGuiHandler;
import net.slimevoid.dynamictransport.network.play.client.CPacketSelectFloor;
import net.slimevoid.dynamictransport.network.play.client.CPacketUpdateMarker;
import net.slimevoid.dynamictransport.network.play.server.SPacketOpenFloorSelection;
import org.apache.logging.log4j.Logger;

@Mod(modid = DynamicTransportMod.MOD_ID, name = DynamicTransportMod.NAME, version = DynamicTransportMod.VERSION)
public class DynamicTransportMod
{
    public static final String MOD_ID = "dynamictransport";
    public static final String NAME = "Dynamic Transport";
    public static final String VERSION = "1.0";

    public static Logger logger;
    public static SimpleNetworkWrapper CHANNEL;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ModBlocks.init();
    }

    @EventHandler
    public void Init(FMLInitializationEvent e){
        NetworkRegistry.INSTANCE.registerGuiHandler(MOD_ID, new ModGuiHandler());
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        CHANNEL.registerMessage(CPacketUpdateMarker.UpdateMarkerHandler.class, CPacketUpdateMarker.class,0, Side.SERVER);
        CHANNEL.registerMessage(SPacketOpenFloorSelection.class, SPacketOpenFloorSelection.class,1, Side.CLIENT);
        CHANNEL.registerMessage(CPacketSelectFloor.Handler.class, CPacketSelectFloor.class,2, Side.SERVER);
    }
}
