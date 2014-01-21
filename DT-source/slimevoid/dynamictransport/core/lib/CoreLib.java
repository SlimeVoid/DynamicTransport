package slimevoid.dynamictransport.core.lib;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CoreLib {
	public static final String	MOD_ID				= "DynamicTransportMod";
	public static final String	MOD_RESOURCES		= "dynamictransport";
	public static final String	MOD_NAME			= "Dynamic Transport Mod";
	public static final String	MOD_VERSION			= "0.0.0.8";
	public static final String	MOD_DEPENDENCIES	= "required-after:SlimevoidLib";
	public static final String	MOD_CHANNEL			= "DYNAMICTRANSPORT";
	public static final String	CLIENT_PROXY		= "slimevoid.dynamictransport.client.proxy.ClientProxy";
	public static final String	COMMON_PROXY		= "slimevoid.dynamictransport.proxy.CommonProxy";
	@SideOnly(Side.CLIENT)
	public static boolean		OPTIFINE_INSTALLED	= FMLClientHandler.instance().hasOptifine();
}
