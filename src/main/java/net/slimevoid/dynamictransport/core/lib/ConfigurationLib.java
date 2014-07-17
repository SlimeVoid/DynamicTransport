package net.slimevoid.dynamictransport.core.lib;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.slimevoid.dynamictransport.blocks.BlockPoweredLight;
import net.slimevoid.dynamictransport.blocks.BlockTransportBase;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;

public class ConfigurationLib {

    public static BlockTransportBase blockTransportBase;
    public static ItemElevatorTool   itemElevatorTool;
    public static boolean            useClientMotionTick;
    private static File              configurationFile;
    private static Configuration     configuration;
    public static float              elevatorMaxSpeed;
    public static int                MaxBindingRange = 3;
    public static BlockPoweredLight[] blockPoweredLight;

    public static void CommonConfig(File configFile) {
        if (configurationFile == null) {
            configurationFile = configFile;
            configuration = new Configuration(configFile);
        }

        configuration.load();

        elevatorMaxSpeed = (float) configuration.get("Common",
                                                     "MaxElevatorSpeed",
                                                     0.15).getDouble(0.15);

        configuration.save();

    }

    public static void ClientConfig() {
        // TODO Auto-generated method stub
        useClientMotionTick = configuration.get("Client",
                                                "useClientMotionTickHandler",
                                                false).getBoolean(false);

        configuration.save();
    }

}
