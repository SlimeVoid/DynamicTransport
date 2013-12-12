package slimevoid.dynamictransport.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import slimevoid.dynamictransport.core.lib.CoreLib;
import slimevoidlib.network.PacketEntity;

public class PacketEntityMotionUpdate extends PacketEntity {
	public double	motionY;

	public PacketEntityMotionUpdate() {
		super();
		this.setChannel(CoreLib.MOD_CHANNEL);
		this.setCommand("MOTION");
	}

	public PacketEntityMotionUpdate(double motionY, int entityID) {
		this();
		setEntityId(entityID);
		this.motionY = motionY;

	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeDouble(this.motionY);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		this.motionY = data.readDouble();
	}

}
