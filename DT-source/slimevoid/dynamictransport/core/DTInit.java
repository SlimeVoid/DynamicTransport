package slimevoid.dynamictransport.core;

import slimevoid.dynamictransport.core.lib.CoreLib;
import slimevoidlib.core.SlimevoidCore;
import cpw.mods.fml.common.registry.EntityRegistry;

public class DTInit {

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        load();
    }

    public static void load() {
        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering names...");
        DTCore.registerNames();

        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering blocks...");
        DTCore.registerBlocks();

        SlimevoidCore.console(CoreLib.MOD_ID,
                              "Registering items...");
        DTCore.registerItems();

        EntityRegistry.registerModEntity(slimevoid.dynamictransport.entities.EntityElevator.class,
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
}
