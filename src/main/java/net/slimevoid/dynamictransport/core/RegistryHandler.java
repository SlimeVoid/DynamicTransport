package net.slimevoid.dynamictransport.core;

import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.entity.EntityElevator;
import net.slimevoid.dynamictransport.item.ModItems;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = DynamicTransportMod.MOD_ID)
public class RegistryHandler {
    public static EntityEntry e;
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(ModBlocks.getController(),ModBlocks.getMarker(),ModBlocks.getElevator());
        event.getRegistry().registerAll(ModBlocks.getTransientBlocks());
        GameRegistry.registerTileEntity(TileEntityElevator.class, new ResourceLocation("dynamictransport","elevator"));
        GameRegistry.registerTileEntity(TileEntityMarker.class, new ResourceLocation("dynamictransport","marker"));
        GameRegistry.registerTileEntity(TileEntityElevatorController.class,new ResourceLocation("dynamictransport","controller"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.getElevatorTool(),ModItems.getController(),ModItems.getMarker(),ModItems.getElevator());

    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event){
         e = EntityEntryBuilder.create()
                .entity(EntityElevator.class)
                .tracker(80,2,true)
                .id(new ResourceLocation(DynamicTransportMod.MOD_ID,"elevator"), 0)
                .name("elevator")
                .build();
        event.getRegistry().register(e);
    }
}
