package net.slimevoid.dynamictransport.client.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.slimevoid.dynamictransport.items.ItemElevatorTool;

/**
 * Created by Allen on 7/20/2014.
 */
@SideOnly(Side.CLIENT)
public class PlayerTick {
    private boolean hasMechanicView;
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        boolean hasMechanicEye = player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemElevatorTool;
        if (hasMechanicEye != hasMechanicView) {
            hasMechanicView = !hasMechanicView;
            FMLClientHandler.instance().getClient().renderGlobal.markBlockRangeForRenderUpdate(
                    (int) player.posX - 32, (int) player.posY - 32, (int) player.posZ - 32,
                    (int) player.posX + 32, (int) player.posY + 32, (int) player.posZ + 32);
        }
    }
}
