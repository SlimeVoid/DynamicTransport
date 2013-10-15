package slimevoid.dynamictransport.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.BlockLib;
import slimevoidlib.blocks.BlockBase;

public class BlockTransportBase extends BlockBase {
	
	protected Icon[][] iconList;

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
    public Icon getIcon(int side, int metadata){
    	return this.iconList[metadata][side];
    	
    }
    
    @Override
	public void registerIcons(IconRegister iconRegister) {
		iconList = new Icon[BlockLib.BLOCK_MAX_TILES][6];
		iconList = BlockLib.registerIcons(iconRegister, iconList);
	}

}
