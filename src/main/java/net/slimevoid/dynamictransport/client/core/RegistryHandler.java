package net.slimevoid.dynamictransport.client.core;

import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.slimevoid.dynamictransport.client.renderer.block.CamoModel;
import net.slimevoid.dynamictransport.client.renderer.entity.ElevatorRenderer;
import net.slimevoid.dynamictransport.core.DynamicTransport;

import static net.slimevoid.dynamictransport.core.RegistryHandler.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DynamicTransport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class RegistryHandler {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent e) {
        RenderTypeLookup.setRenderLayer(ELEVATOR_BLOCK.get(), (RenderType) -> true);
        e.getMinecraftSupplier().get().getBlockColors().register(CamoModel::getColor, ELEVATOR_BLOCK.get());
        RenderTypeLookup.setRenderLayer(MARKER_BLOCK.get(), (RenderType) -> true);
        e.getMinecraftSupplier().get().getBlockColors().register(CamoModel::getColor, MARKER_BLOCK.get());
    }
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        RenderingRegistry.registerEntityRenderingHandler(ELEVATOR_ENTITY.get(), ElevatorRenderer::new);
    }

    @SubscribeEvent
    public static void RegisterBackedModels(ModelBakeEvent e)
    {
        IBakedModel camoModel = new CamoModel();
        for(int i = 0; i<16; ++i) {
            e.getModelRegistry().put(new ModelResourceLocation(ELEVATOR_BLOCK.getId(), "camo=true,level=" + i), camoModel);
            e.getModelRegistry().put(new ModelResourceLocation(MARKER_BLOCK.getId(), "camo=true,level=" + i), camoModel);
        }
    }

}
