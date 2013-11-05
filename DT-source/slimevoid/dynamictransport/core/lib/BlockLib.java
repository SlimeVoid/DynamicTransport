package slimevoid.dynamictransport.core.lib;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class BlockLib {

	public static final int		BLOCK_MAX_TILES				= 3;
	public static final int		BLOCK_ELEVATOR_ID			= 0;
	public static final int		BLOCK_ELEVATOR_COMPUTER_ID	= 1;
	public static final int		BLOCK_DYNAMIC_MARK_ID		= 2;

	private static final String	PREFIX						= "dt";

	public static final String	BLOCK_TRANSPORT_BASE		= PREFIX + "base";

	private static final String	BLOCK_TRANSPORT_PREFIX		= PREFIX
																+ ".transport";
	public static final String	BLOCK_ELEVATOR				= BLOCK_TRANSPORT_PREFIX
																+ ".elevator";
	public static final String	BLOCK_ELEVATOR_COMPUTER		= BLOCK_TRANSPORT_PREFIX
																+ ".computer";
	public static final String	BLOCK_DYNAMIC_MARK			= BLOCK_TRANSPORT_PREFIX
																+ ".marker";
	public static final String	ITEM_ELEVATOR_TOOL			= PREFIX
																+ ".elevatortool";

	public static Icon[][] registerIcons(IconRegister iconRegister, Icon[][] iconList) {
		iconList[BLOCK_ELEVATOR_ID][1] = Block.blockDiamond.getIcon(1,
																	0);
		iconList[BLOCK_ELEVATOR_ID][0] = Block.blockDiamond.getIcon(2,
																	0);
		iconList[BLOCK_ELEVATOR_ID][2] = Block.blockDiamond.getIcon(2,
																	0);
		iconList[BLOCK_ELEVATOR_ID][3] = Block.blockDiamond.getIcon(2,
																	0);
		iconList[BLOCK_ELEVATOR_ID][4] = Block.blockDiamond.getIcon(2,
																	0);
		iconList[BLOCK_ELEVATOR_ID][5] = Block.blockDiamond.getIcon(2,
																	0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][1] = Block.blockGold.getIcon(	1,
																			0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][0] = Block.blockGold.getIcon(	2,
																			0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][2] = Block.blockGold.getIcon(	2,
																			0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][3] = Block.blockGold.getIcon(	2,
																			0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][4] = Block.blockGold.getIcon(	2,
																			0);
		iconList[BLOCK_ELEVATOR_COMPUTER_ID][5] = Block.blockGold.getIcon(	2,
																			0);
		iconList[BLOCK_DYNAMIC_MARK_ID][1] = Block.blockIron.getIcon(	1,
																		0);
		iconList[BLOCK_DYNAMIC_MARK_ID][0] = Block.blockIron.getIcon(	2,
																		0);
		iconList[BLOCK_DYNAMIC_MARK_ID][2] = Block.blockIron.getIcon(	2,
																		0);
		iconList[BLOCK_DYNAMIC_MARK_ID][3] = Block.blockIron.getIcon(	2,
																		0);
		iconList[BLOCK_DYNAMIC_MARK_ID][4] = Block.blockIron.getIcon(	2,
																		0);
		iconList[BLOCK_DYNAMIC_MARK_ID][5] = Block.blockIron.getIcon(	2,
																		0);
		return iconList;
	}

}
