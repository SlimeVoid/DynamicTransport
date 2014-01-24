package slimevoid.dynamictransport.core;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.CoreLib;
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
		ConfigurationLib.blockTransportBase.addMapping(	BlockLib.BLOCK_ELEVATOR_ID,
														TileEntityElevator.class,
														BlockLib.BLOCK_ELEVATOR);

		ConfigurationLib.blockTransportBase.addMapping(	BlockLib.BLOCK_ELEVATOR_COMPUTER_ID,
														TileEntityElevatorComputer.class,
														BlockLib.BLOCK_ELEVATOR_COMPUTER);

		ConfigurationLib.blockTransportBase.addMapping(	BlockLib.BLOCK_DYNAMIC_MARK_ID,
														TileEntityFloorMarker.class,
														BlockLib.BLOCK_DYNAMIC_MARK);

		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.blockTransportBase, 5, BlockLib.BLOCK_ELEVATOR_ID),
								new Object[] {
										"IDI",
										"IEI",
										"IRI",
										Character.valueOf('I'),
										Item.ingotIron,
										Character.valueOf('D'),
										Item.diamond,
										Character.valueOf('R'),
										Item.redstone,
										Character.valueOf('E'),
										Item.enderPearl });
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.blockTransportBase, 1, BlockLib.BLOCK_ELEVATOR_COMPUTER_ID),
								new Object[] {
										"GEG",
										"GAG",
										"GRG",
										Character.valueOf('G'),
										Item.ingotGold,
										Character.valueOf('E'),
										Item.enderPearl,
										Character.valueOf('R'),
										Item.redstone,
										Character.valueOf('A'),
										Block.glass });
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.blockTransportBase, 4, BlockLib.BLOCK_DYNAMIC_MARK_ID),
								new Object[] {
										"III",
										"IEI",
										"IRI",
										Character.valueOf('I'),
										Item.ingotIron,
										Character.valueOf('E'),
										Item.enderPearl,
										Character.valueOf('R'),
										Item.redstone });
	}

	public static void registerItems() {
		ConfigurationLib.itemElevatorTool = new ItemElevatorTool(ConfigurationLib.itemElevatorToolID);

		ConfigurationLib.itemElevatorTool.setUnlocalizedName(BlockLib.ITEM_ELEVATOR_TOOL).setTextureName(CoreLib.MOD_ID
																											+ ":"
																											+ BlockLib.ITEM_ELEVATOR_TOOL);

		ConfigurationLib.itemElevatorTool.setCreativeTab(CreativeTabs.tabTransport);
		GameRegistry.addRecipe(	new ItemStack(ConfigurationLib.itemElevatorTool, 1),
								new Object[] {
										"LGL",
										"LRL",
										"LSL",
										Character.valueOf('L'),
										new ItemStack(Item.dyePowder, 1, 4),
										Character.valueOf('G'),
										Block.glass,
										Character.valueOf('R'),
										Item.redstone,
										Character.valueOf('S'),
										Item.silk });
	}

}
