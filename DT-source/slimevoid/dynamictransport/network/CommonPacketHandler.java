/**
 * 
 */
package slimevoid.dynamictransport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import slimevoidlib.data.Logger;
import slimevoidlib.data.LoggerSlimevoidLib;
import slimevoidlib.network.handlers.SubPacketHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

/**
 * @author alcoo_000
 * 
 */
public class CommonPacketHandler implements IPacketHandler {

	private static Map<Integer, SubPacketHandler>	commonHandlers;
	public static final String[]					CHANNELS		= {
			"DE_GUI_REQUEST",
			"DE_GUI_RESPONSE",
			"DE_UPDATE",
			"DE_EPROP",
			"DE_ERROR",
			"DE_SHCI",
			"DE_BUPDATE"											};
	public static final int							UPDATE_RIDERS	= 2;

	/**
	 * Initializes the commonHandler Map
	 */
	public static void init() {
		commonHandlers = new HashMap<Integer, SubPacketHandler>();
	}

	/**
	 * Register a sub-handler with the server-side packet handler.
	 * 
	 * @param packetID
	 *            Packet ID for the sub-handler to handle.
	 * @param handler
	 *            The sub-handler.
	 */
	public static void registerPacketHandler(int packetID, SubPacketHandler handler) {
		if (commonHandlers.containsKey(packetID)) {
			LoggerSlimevoidLib.getInstance(Logger.filterClassName(CommonPacketHandler.class.toString())).write(	false,
																												"PacketID ["
																														+ packetID
																														+ "] already registered.",
																												Logger.LogLevel.ERROR);
			throw new RuntimeException("PacketID [" + packetID
										+ "] already registered.");
		}
		commonHandlers.put(	packetID,
							handler);
	}

	/**
	 * Retrieves the registered sub-handler from the server side list
	 * 
	 * @param packetID
	 * @return the sub-handler
	 */
	public static SubPacketHandler getPacketHandler(int packetID) {
		if (!commonHandlers.containsKey(packetID)) {
			LoggerSlimevoidLib.getInstance(Logger.filterClassName(CommonPacketHandler.class.toString())).write(	false,
																												"Tried to get a Packet Handler for ID: "
																														+ packetID
																														+ " that has not been registered.",
																												Logger.LogLevel.WARNING);
			throw new RuntimeException("Tried to get a Packet Handler for ID: "
										+ packetID
										+ " that has not been registered.");
		}
		return commonHandlers.get(packetID);
	}

	/**
	 * The server-side packet handler receives a packet.<br>
	 * Fetches the packet ID and routes it on to sub-handlers.
	 */
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			getPacketHandler(packetID).onPacketData(manager,
													packet,
													player);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
