package net.slimevoid.dynamictransport.core.lib;

import java.io.File;
import java.util.regex.Pattern;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.blocks.BlockPoweredLight;
import net.slimevoid.dynamictransport.blocks.BlockTransportBase;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;

public class ConfigurationLib {

    public static BlockTransportBase blockTransportBase;
    public static ItemElevatorTool   itemElevatorTool;
    @SideOnly(Side.CLIENT)
    public static boolean            useClientMotionTick;
    private static File              configurationFile;
    private static Configuration     configuration;
    public static float              elevatorMaxSpeed;
    public static int                MaxBindingRange = 3;
    public static BlockPoweredLight[] blockPoweredLight;
    public static int ElevatorRenderId;
    @SideOnly(Side.CLIENT)
    public static Integer ElevatorMaintenanceHighlight;
    @SideOnly(Side.CLIENT)
    public static Integer ComputerMaintenanceHighlight;
    @SideOnly(Side.CLIENT)
    public static Integer MarkerMaintenanceHighlight;

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

        ElevatorRenderId = RenderingRegistry.getNextAvailableRenderId();
    }

    public static void ClientConfig() {
        useClientMotionTick = configuration.get("Client",
                                                "useClientMotionTickHandler",
                                                false).getBoolean(false);
        Pattern ColorValidator = Pattern.compile("0x^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
        String rawElevatorMaintenanceHighlight = configuration.get("Client",
                "ElevatorMaintenanceHighlight",
                "0x004400","The hexadecimal color the Elevator blocks will highlighted when maintaining the elevator they are bound to")
        .getString();
        String rawMarkerMaintenanceHighlight = configuration.get("Client",
                "MarkerMaintenanceHighlight",
                "0x004400","The hexadecimal color the Marker blocks will highlighted when maintaining the elevator they are bound to")
                .getString();
        String rawComputerMaintenanceHighlight = configuration.get("Client",
                "ComputerMaintenanceHighlight",
                "0x004400","The hexadecimal color the Computer block will highlighted when maintaining the elevator")
                .getString();

        //Validate that the strings are Hex
        ElevatorMaintenanceHighlight = ColorValidator.matcher(rawElevatorMaintenanceHighlight).matches()?Integer.parseInt(rawElevatorMaintenanceHighlight,16):0x004400;
        MarkerMaintenanceHighlight = ColorValidator.matcher(rawMarkerMaintenanceHighlight).matches()?Integer.parseInt(rawMarkerMaintenanceHighlight,16):0x004400;
        ComputerMaintenanceHighlight = ColorValidator.matcher(rawComputerMaintenanceHighlight).matches()?Integer.parseInt(rawComputerMaintenanceHighlight,16):0x004400;

        configuration.save();
    }


}
