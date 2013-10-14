package slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.Icon;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoidlib.blocks.BlockBase;

public class BlockTransportBase extends BlockBase {

    public BlockTransportBase(int blockID)
    {
        super(blockID, Material.iron, BlockLib.BLOCK_MAX_TILES);
    }

    @Override
    public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabTransport;
    }
    
    @Override
    public Icon getIcon(int side, int meta){
    	//TODO: Add a tab to GUI to place cover blocks
    	return side == 1 ?Block.blockDiamond.getIcon(side, meta):Block.blockIron.getIcon(side, meta);
    }

}
