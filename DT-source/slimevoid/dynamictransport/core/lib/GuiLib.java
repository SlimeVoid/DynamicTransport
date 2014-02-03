package slimevoid.dynamictransport.core.lib;

import net.minecraft.util.ResourceLocation;

public class GuiLib {

    private static final String          GUI_PREFIX           = "textures/gui/";

    public static final int              GUIID_ELEVATOR       = 0;

    public static final String           ELEVATOR             = GUI_PREFIX
                                                                + BlockLib.BLOCK_ELEVATOR
                                                                + ".png";
    public static final ResourceLocation GUI_ELEVATOR         = new ResourceLocation(CoreLib.MOD_RESOURCES, ELEVATOR);

    public static final String           TITLE_ELEVATOR       = "Dynamic Elevator";

    public static final int              GUIID_FLOOR_MARKER   = 1;
    public static final String           FLOOR_MARKER         = GUI_PREFIX
                                                                + BlockLib.BLOCK_DYNAMIC_MARK
                                                                + ".png";

    public static final int              GUIID_FloorSelection = 2;
    public static final String           CAMO                 = GUI_PREFIX
                                                                + "camo"
                                                                + ".png";

    public static final ResourceLocation GUI_FLOOR_MARKER     = new ResourceLocation(CoreLib.MOD_RESOURCES, FLOOR_MARKER);

    public static final ResourceLocation GUI_CAMO             = new ResourceLocation(CoreLib.MOD_RESOURCES, FLOOR_MARKER);
}