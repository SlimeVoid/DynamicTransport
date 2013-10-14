package slimevoid.dynamictransport.core;

import slimevoid.dynamictransport.core.lib.CoreLib;
import slimevoid.dynamictransport.proxy.CommonProxy;
import slimevoidlib.ICommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(
        modid = CoreLib.MOD_ID,
        name = CoreLib.MOD_NAME,
        version = CoreLib.MOD_VERSION,
        dependencies = CoreLib.MOD_DEPENDENCIES)
@NetworkMod(
        clientSideRequired = true,
        serverSideRequired = false,
        //TODO: Handle Packets
        /*clientPacketHandlerSpec = @SidedPacketHandler(
                channels = { CoreLib.MOD_CHANNEL },
                packetHandler = ClientPacketHandler.class),
        serverPacketHandlerSpec = @SidedPacketHandler(
                channels = { CoreLib.MOD_CHANNEL },
                packetHandler = CommonPacketHandler.class),*/
        connectionHandler = CommonProxy.class
)
public class DynamicTransportMod {
    @SidedProxy(
            clientSide = CoreLib.CLIENT_PROXY,
            serverSide = CoreLib.COMMON_PROXY)
    public static ICommonProxy proxy;
    
    @Instance(CoreLib.MOD_ID)
    public static DynamicTransportMod instance;

    @EventHandler
    public void CollaborativePreInit(FMLPreInitializationEvent event) {
        DynamicTransportMod.proxy.registerConfigurationProperties(event.getSuggestedConfigurationFile());

        DynamicTransportMod.proxy.preInit();
    }

    @EventHandler
    public void CollaborativeInit(FMLInitializationEvent event) {
    }

    @EventHandler
    public void CollaborativePostInit(FMLPostInitializationEvent event) {
        DTInit.initialize();
    }
}
