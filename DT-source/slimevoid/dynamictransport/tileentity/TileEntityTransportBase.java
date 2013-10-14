package slimevoid.dynamictransport.tileentity;

import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.tileentity.TileEntityBase;

public class TileEntityTransportBase extends TileEntityBase {

	@Override
	public int getBlockID() {
		return ConfigurationLib.blockTransportBaseID;
	}

}
