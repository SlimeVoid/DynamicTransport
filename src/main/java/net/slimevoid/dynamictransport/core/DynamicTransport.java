package net.slimevoid.dynamictransport.core;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTransport.MOD_ID)
public class DynamicTransport {
    public static final IProperty<Boolean> CAMO = BooleanProperty.create("camo");
    public static final String MOD_ID = "dynamictransport";
    public DynamicTransport(){
        RegistryHandler.Init(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
