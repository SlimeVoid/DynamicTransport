package slimevoid.dynamictransport.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimevoid.dynamictransport.blocks.BlockTransportBase;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoid.dynamictransport.core.lib.LocaleLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevator;
import slimevoidlib.items.ItemBlockBase;
import cpw.mods.fml.common.registry.GameRegistry;



public class DTCore {

    public static void registerNames()
    {
        LocaleLib.registerLanguages();        
    }

    public static void registerBlocks()
    {
        ConfigurationLib.blockTransportBase = new BlockTransportBase(ConfigurationLib.blockTransportBaseID);
        GameRegistry.registerBlock(ConfigurationLib.blockTransportBase, ItemBlockBase.class, BlockLib.BLOCK_TRANSPORT_BASE);
        GameRegistry.registerTileEntity(TileEntityElevator.class, "SLIMEVOID_ELEVATOR");
        ConfigurationLib.blockTransportBase.addTileEntityMapping(BlockLib.BLOCK_ELEVATOR_ID, TileEntityElevator.class);
        ConfigurationLib.blockTransportBase.setItemName(BlockLib.BLOCK_ELEVATOR_ID, BlockLib.BLOCK_ELEVATOR);
        GameRegistry.addRecipe(new ItemStack(ConfigurationLib.blockTransportBase, 1, BlockLib.BLOCK_ELEVATOR_ID),
                new Object[] {
            "IDI",
            "IRI",
            "III",
            Character.valueOf('I'),
            Item.ingotIron,
            Character.valueOf('D'),
            Item.diamond,
            Character.valueOf('R'),
            Item.redstone
        });
    }

    public static void registerItems()
    {
        // none to register
        
    }

}
