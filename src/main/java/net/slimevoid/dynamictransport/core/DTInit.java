package net.slimevoid.dynamictransport.core;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.slimevoid.dynamictransport.core.lib.CoreLib;
import net.slimevoid.dynamictransport.entities.EntityElevatorPart;
import net.slimevoid.dynamictransport.entities.EntityMasterElevator;
import net.slimevoid.library.core.SlimevoidCore;

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
        EntityRegistry.registerModEntity(EntityMasterElevator.class,
                                         "delv",
                                         0,
                                         DynamicTransportMod.instance,
                                         400,
                                         1,
                                         true);
        EntityRegistry.registerModEntity(EntityElevatorPart.class,
                "delvp",
                1,
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
