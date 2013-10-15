package slimevoid.dynamictransport.core.lib;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class BlockLib {

    public static final int BLOCK_MAX_TILES = 1;
    public static final int BLOCK_ELEVATOR_ID = 0;
    
    private static final String BLOCK_PREFIX = "dt.";
    public static final String BLOCK_TRANSPORT_BASE = BLOCK_PREFIX + "base";
    
    private static final String BLOCK_TRANSPORT_PREFIX = BLOCK_PREFIX + "transport.";
    public static final String BLOCK_ELEVATOR = BLOCK_TRANSPORT_PREFIX + "elevator";
	public static Icon[][] registerIcons(IconRegister iconRegister,
			Icon[][] iconList) {
		iconList[0][1] = Block.blockDiamond.getIcon(1, 0);
		iconList[0][0] = 	Block.blockIron.getIcon(2, 0);
		iconList[0][2] = 	Block.blockIron.getIcon(2, 0);
		iconList[0][3] = 	Block.blockIron.getIcon(2, 0);
		iconList[0][4] = 	Block.blockIron.getIcon(2, 0);
		iconList[0][5] = 	Block.blockIron.getIcon(2, 0);
		return iconList;
	}

}
