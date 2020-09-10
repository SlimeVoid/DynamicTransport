package net.slimevoid.dynamictransport.network.play.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;

public class CPacketSelectFloor implements IMessage {
    private BlockPos controller;
    private Integer floorY;
    private String name;
    public CPacketSelectFloor(){}
    public CPacketSelectFloor(BlockPos controller, Integer floorY, String name) {
        this.controller = controller;
        this.floorY = floorY;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        floorY = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        controller = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(floorY);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeLong(controller.toLong());
    }

    public static class Handler implements IMessageHandler<CPacketSelectFloor, IMessage> {
        @Override
        public IMessage onMessage(CPacketSelectFloor message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.world;
            if(world.isBlockLoaded(message.controller)) {
                TileEntity t = world.getTileEntity(message.controller);
                if(t instanceof TileEntityElevatorController){
                    TileEntityElevatorController tm = (TileEntityElevatorController)t;
                    tm.callElevator(message.floorY,message.name);
                }
            }
            //no reply
            return null;
        }
    }
}
