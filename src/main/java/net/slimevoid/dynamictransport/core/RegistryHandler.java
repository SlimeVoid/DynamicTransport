package net.slimevoid.dynamictransport.core;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.slimevoid.dynamictransport.blocks.ElevatorBlock;
import net.slimevoid.dynamictransport.blocks.ElevatorControllerBlock;
import net.slimevoid.dynamictransport.blocks.MarkerBlock;
import net.slimevoid.dynamictransport.entities.ElevatorEntity;
import net.slimevoid.dynamictransport.items.ElevatorToolItem;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;
import net.slimevoid.dynamictransport.tileentity.TransportPartTileEntity;

public class RegistryHandler {
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS,DynamicTransport.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS,DynamicTransport.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES,DynamicTransport.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES,DynamicTransport.MOD_ID);

    public static final RegistryObject<Item> ELEVATOR_TOOL = ITEMS.register("elevator_tool", ElevatorToolItem::new);
    public static final RegistryObject<Block> ELEVATOR_BLOCK = BLOCKS.register("elevator", ElevatorBlock::new);
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> ELEVATOR_ITEM = ITEMS.register("elevator", () -> new BlockItem(ELEVATOR_BLOCK.get(), new Item.Properties().group(ItemGroup.TRANSPORTATION)));

    public static final RegistryObject<Block> MARKER_BLOCK = BLOCKS.register("marker", MarkerBlock::new);
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> MARKER_ITEM = ITEMS.register("marker", () -> new BlockItem(MARKER_BLOCK.get(), new Item.Properties().group(ItemGroup.TRANSPORTATION)));

    public static final RegistryObject<Block> CONTROLLER_BLOCK = BLOCKS.register("controller", ElevatorControllerBlock::new);
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> CONTROLLER_ITEM = ITEMS.register("controller", () -> new BlockItem(CONTROLLER_BLOCK.get(), new Item.Properties().group(ItemGroup.TRANSPORTATION)));

    public static final RegistryObject<EntityType<ElevatorEntity>> ELEVATOR_ENTITY = ENTITIES.register("master_elevator" ,
            () -> EntityType.Builder.create((EntityType.IFactory<ElevatorEntity>) ElevatorEntity::new, EntityClassification.MISC)
                    .disableSummoning()
                    //.setShouldReceiveVelocityUpdates(false)
                    .immuneToFire()
                    .size(1F, 1F)
                    .setUpdateInterval(2)
                    .build( DynamicTransport.MOD_ID + ":elevator")
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<TransportPartTileEntity>> TRANSPORT_PART_TILE_ENTITY = TILE_ENTITIES.register("elevator",
            () -> TileEntityType.Builder.create(TransportPartTileEntity::new, ELEVATOR_BLOCK.get(), MARKER_BLOCK.get()).build(null)
    );
    public static final RegistryObject<TileEntityType<ElevatorControllerTileEntitiy>> ELEVATOR_CONTROLLER_TILE_ENTITIY = TILE_ENTITIES.register("controller",
            () -> TileEntityType.Builder.create(ElevatorControllerTileEntitiy::new, CONTROLLER_BLOCK.get()).build(null));

    /*public static final RegistryObject<TileEntityType<CamoTileEntity>> MARKER_TILE_ENTITIY = TILE_ENTITIES.register("marker",
            () -> TileEntityType.Builder.create(CamoTileEntity::new, MARKER_BLOCK.get()).build(null)
    );*/
    static void Init(IEventBus bus){
        ITEMS.register(bus);
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        TILE_ENTITIES.register(bus);
    }

}
