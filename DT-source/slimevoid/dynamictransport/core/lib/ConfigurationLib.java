package slimevoid.dynamictransport.core.lib;

import java.io.File;

import net.minecraftforge.common.Configuration;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.items.ItemElevatorTool;

public class ConfigurationLib {

    public static BlockTransportBase blockTransportBase;
    public static int                blockTransportBaseID;
    public static ItemElevatorTool   itemElevatorTool;
    public static boolean            useClientMotionTick;
    public static int                itemElevatorToolID;
    private static File              configurationFile;
    private static Configuration     configuration;
    public static float              elevatorMaxSpeed;
    public static int                MaxBindingRange = 3;

    public static void CommonConfig(File configFile) {
        if (configurationFile == null) {
            configurationFile = configFile;
            configuration = new Configuration(configFile);
        }

        configuration.load();

        blockTransportBaseID = configuration.getBlock("blockTransportBaseID",
                                                      267).getInt();
        itemElevatorToolID = configuration.getItem("itemElevatorToolID",
                                                   268).getInt();
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
