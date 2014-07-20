package net.slimevoid.dynamictransport.client.event;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
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
