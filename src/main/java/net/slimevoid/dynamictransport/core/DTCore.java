package net.slimevoid.dynamictransport.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.slimevoid.dynamictransport.blocks.BlockPoweredLight;
import net.slimevoid.dynamictransport.blocks.BlockTransportBase;
import net.slimevoid.dynamictransport.core.lib.BlockLib;
import net.slimevoid.dynamictransport.core.lib.ConfigurationLib;
import net.slimevoid.dynamictransport.core.lib.CoreLib;
import net.slimevoid.dynamictransport.core.lib.LocaleLib;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.dynamictransport.tileentity.TileEntityFloorMarker;
import net.slimevoid.library.items.ItemBlockBase;

public class DTCore {

    public static void registerNames() {
        LocaleLib.registerLanguages();
    }

    public static void registerBlocks() {
        ConfigurationLib.blockTransportBase = new BlockTransportBase();
        GameRegistry.registerBlock(ConfigurationLib.blockTransportBase,
                                   ItemBlockBase.class,
                                   BlockLib.BLOCK_TRANSPORT_BASE);
        ConfigurationLib.blockPoweredLight = new BlockPoweredLight[16];

        for (int i = 0; i < 16; i++){
            ConfigurationLib.blockPoweredLight[i] = new BlockPoweredLight(i);
            GameRegistry.registerBlock(ConfigurationLib.blockPoweredLight[i],
                ItemBlockBase.class,
                BlockLib.BLOCK_POWERED_LiGHT + " " + i
            );
        }
        ConfigurationLib.blockTransportBase.addMapping(BlockLib.BLOCK_ELEVATOR_ID,
                                                       TileEntityElevator.class,
                                                       BlockLib.BLOCK_ELEVATOR);

        ConfigurationLib.blockTransportBase.addMapping(BlockLib.BLOCK_ELEVATOR_COMPUTER_ID,
                                                       TileEntityElevatorComputer.class,
                                                       BlockLib.BLOCK_ELEVATOR_COMPUTER);

        ConfigurationLib.blockTransportBase.addMapping(BlockLib.BLOCK_DYNAMIC_MARK_ID,
                                                       TileEntityFloorMarker.class,
                                                       BlockLib.BLOCK_DYNAMIC_MARK);

        GameRegistry.addRecipe(new ItemStack(ConfigurationLib.blockTransportBase, 5, BlockLib.BLOCK_ELEVATOR_ID),
                               new Object[] {
                                       "IDI",
                                       "IEI",
                                       "IRI",
                                       Character.valueOf('I'),
                                       Items.iron_ingot,
                                       Character.valueOf('D'),
                                       Items.diamond,
                                       Character.valueOf('R'),
                                       Items.redstone,
                                       Character.valueOf('E'),
                                       Items.ender_pearl });
        GameRegistry.addRecipe(new ItemStack(ConfigurationLib.blockTransportBase, 1, BlockLib.BLOCK_ELEVATOR_COMPUTER_ID),

                                       "GEG",
                                       "GAG",
                                       "GRG",
                                      'G',
                                       Items.gold_ingot,
                                       'E',
                                       Items.ender_pearl,
                                       'R',
                                       Items.redstone,
                                       'A',
                                       Blocks.glass );
        GameRegistry.addRecipe(new ItemStack(ConfigurationLib.blockTransportBase, 4, BlockLib.BLOCK_DYNAMIC_MARK_ID),
                               new Object[] {
                                       "III",
                                       "IEI",
                                       "IRI",
                                       Character.valueOf('I'),
                                       Items.iron_ingot,
                                       Character.valueOf('E'),
                                       Items.ender_pearl,
                                       Character.valueOf('R'),
                                       Items.redstone });
    }

    public static void registerItems() {
        ConfigurationLib.itemElevatorTool = new ItemElevatorTool();

        //ConfigurationLib.itemElevatorTool.setUnlocalizedName(BlockLib.ITEM_ELEVATOR_TOOL).setTextureName(CoreLib.MOD_ID
        //                                                                                                 + ":"
        //                                                                                                 + BlockLib.ITEM_ELEVATOR_TOOL);

        ConfigurationLib.itemElevatorTool.setCreativeTab(CreativeTabs.tabTransport);
        GameRegistry.registerItem(ConfigurationLib.itemElevatorTool,
                                  BlockLib.ITEM_ELEVATOR_TOOL);
        GameRegistry.addRecipe(new ItemStack(ConfigurationLib.itemElevatorTool, 1),
                               new Object[] {
                                       "LGL",
                                       "LRL",
                                       "LSL",
                                       Character.valueOf('L'),
                                       new ItemStack(Items.dye, 1, 4),
                                       Character.valueOf('G'),
                                       Blocks.glass_pane,
                                       Character.valueOf('R'),
                                       Items.redstone,
                                       Character.valueOf('S'),
                                       Items.string });
    }

}
