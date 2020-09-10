package net.slimevoid.dynamictransport.item;

import net.slimevoid.dynamictransport.block.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;


public class ModItems {
    private static final Item controller = new ItemBlock(ModBlocks.getController())
            .setRegistryName("controller");
    private static final Item marker = new ItemBlock(ModBlocks.getMarker())
            .setRegistryName("marker");
    private static final Item elevator = new ItemBlock(ModBlocks.getElevator())
            .setRegistryName("elevator");
    private static final Item tool = new ItemElevatorTool().setCreativeTab(CreativeTabs.TRANSPORTATION)
            .setRegistryName("elevator_tool")
            .setUnlocalizedName("dynamictransport.elevator_tool");

    @Nonnull
    public static Item getController() { return controller; }
    @Nonnull
    public static Item getMarker() { return marker; }
    @Nonnull
    public static Item getElevator() { return elevator; }
    @Nonnull
    public static Item getElevatorTool() { return tool; }

}
