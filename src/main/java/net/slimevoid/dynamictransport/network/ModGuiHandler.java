package net.slimevoid.dynamictransport.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.slimevoid.dynamictransport.client.gui.GuiConfigureMarker;
import net.slimevoid.dynamictransport.client.gui.GuiFloorSelect;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevator;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityTransportPart;

import javax.annotation.Nullable;

public class ModGuiHandler implements IGuiHandler {
    public static final int MARKER_GUI = 0;
    public static final int FLOOR_SELECT = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == MARKER_GUI) {
            TileEntity t = world.getTileEntity(new BlockPos(x, y, z));
            if(t instanceof TileEntityMarker)
                return new GuiConfigureMarker((TileEntityMarker)t);
        } else if (ID == FLOOR_SELECT) {
            TileEntity t = world.getTileEntity(new BlockPos(x, y, z));
            if(t instanceof TileEntityElevatorController) {
                return new GuiFloorSelect((TileEntityElevatorController)t);
            }
        }
        return null;
    }
}
