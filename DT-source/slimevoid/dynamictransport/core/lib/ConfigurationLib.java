package slimevoid.dynamictransport.core.lib;

import java.io.File;

import net.minecraftforge.common.Configuration;
import slimevoid.dynamictransport.blocks.BlockTransportBase;

public class ConfigurationLib {

    public static BlockTransportBase blockTransportBase;
    public static int blockTransportBaseID;
	private static File configurationFile;
	private static Configuration configuration;
    
	
	public static void CommonConfig(File configFile) {
		if (configurationFile == null) {
			configurationFile = configFile;
			configuration = new Configuration(configFile);
		}

		configuration.load();

		blockTransportBaseID = configuration.getBlock("blockTransportBaseID", 267).getInt();
		
	}

}
