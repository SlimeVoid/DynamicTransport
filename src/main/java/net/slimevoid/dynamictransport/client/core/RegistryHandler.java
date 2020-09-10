package net.slimevoid.dynamictransport.client.core;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.slimevoid.dynamictransport.block.BlockCamoBase;
import net.slimevoid.dynamictransport.client.renderer.BackedModelLoader;
import net.slimevoid.dynamictransport.client.renderer.entity.ElevatorRenderer;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.block.ModBlocks;
import net.slimevoid.dynamictransport.client.renderer.block.CamoModel;
import net.slimevoid.dynamictransport.entity.EntityElevator;
import net.slimevoid.dynamictransport.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DynamicTransportMod.MOD_ID)
@SideOnly(Side.CLIENT)
public class RegistryHandler {


    @SubscribeEvent
    public static void clientSetup(ColorHandlerEvent.Block e) {
        e.getBlockColors().registerBlockColorHandler(RegistryHandler::getColor, ModBlocks.getElevator(),ModBlocks.getMarker());
    }

    private static int getColor(IBlockState blockState, IBlockAccess iLightReader, BlockPos pos, int i) {
        int color = -1;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if(player != null) {
            for(EnumHand hand : EnumHand.values()) {
                ItemStack held = player.getHeldItem(hand);
                if (held.getItem() == ModItems.getElevatorTool() && held.getTagCompound() != null) {
                    BlockPos controller = NBTUtil.getPosFromTag(held.getTagCompound().getCompoundTag("pos"));
                    if(iLightReader != null) {
                        TileEntity e = iLightReader.getTileEntity(pos);
                        if (e instanceof TileEntityTransportPart && controller.equals(((TileEntityTransportPart) e).getController())) {
                            color = 0xFF0000;
                            break;
                        }
                    }
                }
            }
        }

        List<IBlockState> sides = ((IExtendedBlockState)blockState).getValue(BlockCamoBase.CAMO_STATE);
        if(sides != null) {
            EnumFacing side = EnumFacing.values()[i >> 8];
            IBlockState camoState = sides.get(side.getIndex());
            int tintIndex = (i & 255) - 1;
            if (camoState != null && camoState.getBlock() != blockState.getBlock() && tintIndex != -1)
                color = GetColorMulti(color, Minecraft.getMinecraft().getBlockColors().colorMultiplier(camoState, iLightReader, pos, tintIndex));
        }
        return color;
    }

    private static int GetColorMulti(int raw,int raw2) {
        if(raw == -1 && raw2 != -1) return raw2;
        if(raw2 == -1) return raw;

        int l = (int)(((float)(raw & 255) * ((float) (raw2 >> 16 & 255)) / 255.0F));
        int i1  = (int)(((float)(raw >> 8 & 255) * ((float) (raw2 >> 8 & 255)) / 255.0F));
        int j1 = (int)(((float)(raw >> 16 & 255) * ((float) (raw2 & 255)) / 255.0F));

        raw &= -16777216;
        raw |= j1 << 16 | i1 << 8 | l;


        return raw;
    }


    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {

        ModelLoader.setCustomModelResourceLocation(ModItems.getController(), 0, new ModelResourceLocation( ModItems.getController().getRegistryName(),"normal"));
        ModelLoader.setCustomModelResourceLocation(ModItems.getMarker(), 0, new ModelResourceLocation( ModItems.getMarker().getRegistryName(),"normal"));
        ModelLoader.setCustomModelResourceLocation(ModItems.getElevator(), 0, new ModelResourceLocation( ModItems.getElevator().getRegistryName(),"normal"));
        ModelLoader.setCustomModelResourceLocation(ModItems.getElevatorTool(), 0, new ModelResourceLocation( ModItems.getElevatorTool().getRegistryName(),"normal"));

        ModelLoaderRegistry.registerLoader(new BackedModelLoader());
        RenderingRegistry.registerEntityRenderingHandler(EntityElevator.class, ElevatorRenderer::new);
        for(Block b: ModBlocks.getTransientBlocks()) ModelLoader.setCustomStateMapper(b, blockIn -> Collections.emptyMap());

    }


}
