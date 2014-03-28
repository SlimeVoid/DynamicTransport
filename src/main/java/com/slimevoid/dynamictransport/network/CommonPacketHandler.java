package com.slimevoid.dynamictransport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import com.slimevoid.dynamictransport.core.lib.CoreLib;
import com.slimevoid.dynamictransport.network.packet.PacketFloorSelected;
import com.slimevoid.dynamictransport.tileentity.TileEntityElevatorComputer;
import net.slimevoid.library.network.handlers.SubPacketHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class CommonPacketHandler implements IPacketHandler {

    private static Map<Integer, SubPacketHandler> commonHandlers;

    /**
     * Initializes the commonHandler Map
     */
    public static void init() {
        commonHandlers = new HashMap<Integer, SubPacketHandler>();
    }

    /**
     * The server-side packet handler receives a packet.<br>
     * Fetches the packet ID and routes it on to sub-handlers.
     */
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            if (packet.channel.equals(CoreLib.MOD_CHANNEL)) {
                int packetID = data.read();
                switch (packetID) {
                case 2:
                    PacketFloorSelected pU = new PacketFloorSelected();
                    pU.readData(data);
                    if (pU.command.split(":")[0].equals("Call")) {
                        if (pU.targetExists(((EntityPlayer) player).worldObj)) {
                            TileEntityElevatorComputer comp = (TileEntityElevatorComputer) ((EntityPlayer) player).worldObj.getBlockTileEntity(pU.xPosition,
                                                                                                                                               pU.yPosition,
                                                                                                                                               pU.zPosition);
                            if (pU.command.split(":").length == 2) {
                                comp.callElevator(Integer.valueOf(pU.command.split(":")[1]),
                                                  "");
                            } else if (pU.command.split(":").length == 3) {
                                comp.callElevator(Integer.valueOf(pU.command.split(":")[1]),
                                                  pU.command.split(":")[2]);
                            }
                        }
                    }
                    break;
                }
            } else {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
