package com.slimevoid.dynamictransport.core;

import net.slimevoid.library.ICommonProxy;
import net.slimevoid.library.util.helpers.PacketHelper;

import com.slimevoid.dynamictransport.core.lib.CoreLib;
import com.slimevoid.dynamictransport.core.lib.PacketLib;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = CoreLib.MOD_ID,
        name = CoreLib.MOD_NAME,
        version = CoreLib.MOD_VERSION,
        dependencies = CoreLib.MOD_DEPENDENCIES)
public class DynamicTransportMod {
    @SidedProxy(
            clientSide = CoreLib.CLIENT_PROXY,
            serverSide = CoreLib.COMMON_PROXY)
    public static ICommonProxy        proxy;

    @Instance(CoreLib.MOD_ID)
    public static DynamicTransportMod instance;

    @EventHandler
    public void DynamicTransportPreInit(FMLPreInitializationEvent event) {
        DynamicTransportMod.proxy.registerConfigurationProperties(event.getSuggestedConfigurationFile());
        DynamicTransportMod.proxy.preInit();
        DTInit.preInitialize();
    }

    @EventHandler
    public void DynamicTransportInit(FMLInitializationEvent event) {
        proxy.init();
        PacketHelper.registerHandler(CoreLib.MOD_CHANNEL,
                                     PacketLib.handler);
        DTInit.initialize();
    }

    @EventHandler
    public void DynamicTransportPostInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        DTInit.postInitialize();
    }
}
