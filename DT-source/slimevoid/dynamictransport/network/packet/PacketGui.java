package slimevoid.dynamictransport.network.packet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import slimevoid.dynamictransport.core.lib.CoreLib;
import slimevoidlib.network.PacketGuiEvent;
import slimevoidlib.util.helpers.SlimevoidHelper;

public class PacketGui extends PacketGuiEvent {

	public PacketGui() {
		super();
		this.setChannel(CoreLib.MOD_CHANNEL);
	}

	public PacketGui(int x, int y, int z, String command, int guiID) {
		this();
		this.setPosition(	x,
							y,
							z,
							0);
		this.setCommand(command);
		this.setGuiID(guiID);
	}

	@Override
	public boolean targetExists(World world) {
		return world.blockExists(	this.xPosition,
									this.yPosition,
									this.zPosition);
	}

	public TileEntity getTarget(World world) {
		return SlimevoidHelper.getBlockTileEntity(	world,
													this.xPosition,
													this.yPosition,
													this.zPosition);
	}
}
