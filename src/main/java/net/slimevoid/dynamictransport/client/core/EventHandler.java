package net.slimevoid.dynamictransport.client.core;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.slimevoid.dynamictransport.core.DynamicTransport;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;
import net.slimevoid.dynamictransport.tileentity.TransportPartTileEntity;

import java.util.stream.Collectors;

import static net.slimevoid.dynamictransport.core.RegistryHandler.ELEVATOR_TOOL;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DynamicTransport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
    private static boolean hasMechanicView;
    private static BlockPos controllerPosition;

    @SubscribeEvent
    public static void PlayerTick(TickEvent.PlayerTickEvent e) { //instead of using a tick handler may be better to change to a shift right click to change modes on the ELEVATORE_TOOL
        if (e.phase != TickEvent.Phase.END) return;
        boolean hasMechanicEye = false;

        PlayerEntity player = e.player;
        World world = player.getEntityWorld();
        if (world.isRemote()) {
            BlockPos newController = null;
            for (Hand hand : Hand.values()) {
                ItemStack held = player.getHeldItem(hand);
                if (held.getItem() == ELEVATOR_TOOL.get() && held.hasTag()) {
                    BlockPos pos = NBTUtil.readBlockPos(held.getTag().getCompound("pos"));
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof ElevatorControllerTileEntitiy) {
                        hasMechanicEye = true;
                        newController = pos;
                        break;
                    }
                }
            }
            if (hasMechanicEye != hasMechanicView || (newController != null && !newController.equals(controllerPosition))) {
                hasMechanicView = hasMechanicEye;
                if (controllerPosition != null)
                    updateControllerBlocks(world, controllerPosition);
                if (newController != null && !newController.equals(controllerPosition))
                {
                    controllerPosition = newController;
                    updateControllerBlocks(world, controllerPosition);
                }
            }
        }
    }
    private static void updateControllerBlocks(World world, BlockPos pos){
        if (pos != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ElevatorControllerTileEntitiy) {
                ElevatorControllerTileEntitiy controller = (ElevatorControllerTileEntitiy) te;
                if (Minecraft.getInstance().worldRenderer.viewFrustum != null) //loading resources don't push updates
                    for (BlockPos part : controller.getParts().collect(Collectors.toList()))
                        world.notifyBlockUpdate(part, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), 3);
            }
        }
    }
}