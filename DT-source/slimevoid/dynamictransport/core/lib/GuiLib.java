package slimevoid.dynamictransport.core.lib;

import net.minecraft.util.ResourceLocation;

public class GuiLib {

	private static final String GUI_PREFIX = "textures/gui/";

	public static final int GUIID_ELEVATOR = 0;

	public static final String ELEVATOR = GUI_PREFIX + BlockLib.BLOCK_ELEVATOR+ ".png";
	public static final ResourceLocation GUI_ELEVATOR = new ResourceLocation(CoreLib.MOD_RESOURCES, ELEVATOR);

	public static final String TITLE_ELEVATOR = "Dynamic Elevator";
}