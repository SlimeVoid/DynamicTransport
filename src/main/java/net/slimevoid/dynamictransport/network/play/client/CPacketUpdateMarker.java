package net.slimevoid.dynamictransport.network.play.client;


import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;


public class CPacketUpdateMarker implements IMessage {
    private BlockPos pos;
    private int offSet;
    private String floorName;

    @SuppressWarnings("unused")
    public CPacketUpdateMarker(){}
    public CPacketUpdateMarker(BlockPos pos, int offSet, String floorName) {
        this.pos = pos;
        this.offSet = offSet;
        this.floorName = floorName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.offSet = buf.readInt();
        this.floorName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(offSet);
        ByteBufUtils.writeUTF8String(buf,this.floorName);
    }

    public static class UpdateMarkerHandler implements IMessageHandler<CPacketUpdateMarker, IMessage> {

        @Override
        public IMessage onMessage(CPacketUpdateMarker message, MessageContext ctx) {

            World world = ctx.getServerHandler().player.world;
            if(world.isBlockLoaded(message.pos)) {
                TileEntity t = world.getTileEntity(message.pos);
                if(t instanceof TileEntityMarker){
                    TileEntityMarker tm = (TileEntityMarker)t;
                    tm.floorName = message.floorName;
                    tm.offSet = message.offSet;
                    tm.markDirty();
                    world.notifyBlockUpdate(message.pos, world.getBlockState(message.pos), world.getBlockState(message.pos), 3);
                }
            }
            //no reply
            return null;
        }
    }
}
