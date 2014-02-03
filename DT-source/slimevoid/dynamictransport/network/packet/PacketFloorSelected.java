package slimevoid.dynamictransport.network.packet;

import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.CoreLib;
import slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import slimevoidlib.network.PacketGuiEvent;

public class PacketFloorSelected extends PacketGuiEvent {

    public PacketFloorSelected(int guiid, int destY, String floorname, int compX, int compY, int compZ) {
        super();
        this.setGuiID(guiid);
        this.setChannel(CoreLib.MOD_CHANNEL);
        this.setCommand("Call:" + destY + ":" + floorname);
        this.xPosition = compX;
        this.yPosition = compY;
        this.zPosition = compZ;
    }

    public PacketFloorSelected() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean targetExists(World world) {
        return world.getBlockTileEntity(this.xPosition,
                                        this.yPosition,
                                        this.zPosition) instanceof TileEntityElevatorComputer;
    }

}
