package net.slimevoid.dynamictransport.network.play.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.network.ModGuiHandler;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;

import java.util.ArrayList;
import java.util.List;

public class SPacketOpenFloorSelection implements IMessage, IMessageHandler<SPacketOpenFloorSelection, IMessage> {
    private BlockPos pos;
    private List<BlockPos> floors;

    @SuppressWarnings("unused")
    public SPacketOpenFloorSelection(){}
    public SPacketOpenFloorSelection(BlockPos pos, List<BlockPos> floors) {
        this.pos = pos;
        this.floors = floors;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        long j = buf.readLong();
        floors = new ArrayList<>();
        for(long i = 0; i< j; i++){
            floors.add(BlockPos.fromLong(buf.readLong()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeLong(floors.size());
        for(BlockPos floor: floors){
            buf.writeLong(floor.toLong());
        }
    }

    @Override
    public IMessage onMessage(SPacketOpenFloorSelection message, MessageContext ctx) {
        if(ctx.side == Side.CLIENT){
            EntityPlayer player = Minecraft.getMinecraft().player;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                TileEntity e = player.world.getTileEntity(message.pos);
                if(e instanceof TileEntityElevatorController){
                    ((TileEntityElevatorController)e).SetboundMarkerBlocks(message.floors);
                }
                player.openGui(DynamicTransportMod.MOD_ID, ModGuiHandler.FLOOR_SELECT, player.world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
            });
        }
        return null;
    }
}
