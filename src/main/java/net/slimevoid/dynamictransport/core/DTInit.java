package net.slimevoid.dynamictransport.core;

import net.slimevoid.dynamictransport.core.lib.CoreLib;
import net.slimevoid.library.core.*;
import cpw.mods.fml.common.registry.EntityRegistry;

public class DTInit {

    private static boolean initialized;

    public static void preInitialize() {
        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering names...");
        DTCore.registerNames();

        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering blocks...");
        DTCore.registerBlocks();

        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering items...");
        DTCore.registerItems();
    }

    public static void initialize() {
        EntityRegistry.registerModEntity(net.slimevoid.dynamictransport.entities.EntityElevator.class,
                                         "delv",
                                         0,
                                         DynamicTransportMod.instance,
                                         400,
                                         1,
                                         true);

        DynamicTransportMod.proxy.registerTickHandlers();
        DynamicTransportMod.proxy.registerEventHandlers();
        DynamicTransportMod.proxy.registerRenderInformation();
    }

    public static void postInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
    }
}
