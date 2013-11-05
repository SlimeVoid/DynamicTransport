package slimevoid.dynamictransport.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.LocaleLib;
import slimevoid.dynamictransport.items.ItemElevatorTool;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import slimevoidlib.items.ItemBlockBase;
import cpw.mods.fml.common.registry.GameRegistry;

public class DTCore {

	public static void registerNames() {
		LocaleLib.registerLanguages();
	}

	public static void registerBlocks() {
		ConfigurationLib.blockTransportBase = new BlockTransportBase(ConfigurationLib.blockTransportBaseID);
		GameRegistry.registerBlock(	ConfigurationLib.blockTransportBase,
									ItemBlockBase.class,
									BlockLib.BLOCK_TRANSPORT_BASE);
		GameRegistry.registerTileEntity(TileEntityElevator.class,
										"SLIMEVOID_ELEVATOR");
		ConfigurationLib.blockTransportBase.addTileEntityMapping(	BlockLib.BLOCK_ELEVATOR_ID,
																	TileEntityElevator.class);
		ConfigurationLib.blockTransportBase.setItemName(BlockLib.BLOCK_ELEVATOR_ID,
														BlockLib.BLOCK_ELEVATOR);

		GameRegistry.registerTileEntity(TileEntityElevatorComputer.class,
										"SLIMEVOID_ELEVATOR_COMPUTER");
		ConfigurationLib.blockTransportBase.addTileEntityMapping(	BlockLib.BLOCK_ELEVATOR_COMPUTER_ID,
																	TileEntityElevatorComputer.class);
		ConfigurationLib.blockTransportBase.setItemName(BlockLib.BLOCK_ELEVATOR_COMPUTER_ID,
														BlockLib.BLOCK_ELEVATOR_COMPUTER);

		GameRegistry.registerTileEntity(TileEntityFloorMarker.class,
										"SLIMEVOID_DYNAMIC_MARKER");
		ConfigurationLib.blockTransportBase.addTileEntityMapping(	BlockLib.BLOCK_DYNAMIC_MARK_ID,
																	TileEntityFloorMarker.class);
		ConfigurationLib.blockTransportBase.setItemName(BlockLib.BLOCK_DYNAMIC_MARK_ID,
														BlockLib.BLOCK_DYNAMIC_MARK);

		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.blockTransportBase, 1, BlockLib.BLOCK_ELEVATOR_ID),
								new Object[] {
										"IDI",
										"IRI",
										"III",
										Character.valueOf('I'),
										Item.ingotIron,
										Character.valueOf('D'),
										Item.diamond,
										Character.valueOf('R'),
										Item.redstone });
	}

	public static void registerItems() {
		ConfigurationLib.itemElevatorTool = new ItemElevatorTool(ConfigurationLib.itemElevatorToolID);

		ConfigurationLib.itemElevatorTool.setUnlocalizedName(BlockLib.ITEM_ELEVATOR_TOOL);

		ConfigurationLib.itemElevatorTool.setCreativeTab(CreativeTabs.tabTransport);
	}

}
