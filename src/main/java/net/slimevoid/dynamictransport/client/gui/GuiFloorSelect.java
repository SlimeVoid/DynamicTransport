package net.slimevoid.dynamictransport.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.network.play.client.CPacketSelectFloor;
import net.slimevoid.dynamictransport.network.play.client.CPacketUpdateMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityElevatorController;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;

import java.io.IOException;
import java.util.*;

public class GuiFloorSelect extends GuiScreen {


    private final TileEntityElevatorController controller;
    private final List<FloorHoverChecker> hoverCheckers = new ArrayList<>();

    public GuiFloorSelect(TileEntityElevatorController controller) {
        this.controller = controller;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = (this.width) / 2 - 10;
        int maxY = ((this.height) / 2);
        int y = maxY;
        int id = 0;
        for (Map.Entry<Integer, ArrayList<String>> set : getFloorList().entrySet()) {
            GuiButton btn = new GuiButton(id++, x, y, 20, 20, set.getKey().toString());
            this.buttonList.add(btn);
            this.hoverCheckers.add(new FloorHoverChecker(btn, 200, set.getValue()));

            if(id % 4 == 0){
                x += 30;
                y = maxY;
            }else{
                y -= 30;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        for(FloorHoverChecker checker: hoverCheckers){
            if (checker.checkHover(mouseX, mouseY))
                GuiUtils.drawHoveringText(checker.getFloorNames(), mouseX, mouseY, width, height, 300, fontRenderer);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        //call elevator with button value
        DynamicTransportMod.CHANNEL.sendToServer(
                new CPacketSelectFloor(
                        this.controller.getPos(),
                        Integer.parseInt(button.displayString),
                        "?"
                       ));
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null)
            this.mc.setIngameFocus();
    }

    public SortedMap<Integer, ArrayList<String>> getFloorList() {
        SortedMap<Integer, ArrayList<String>> floors = new TreeMap<>();
        for (BlockPos boundBlock : controller.getBoundMarkerBlocks()) {
            TileEntity tile = this.controller.getWorld().getTileEntity(boundBlock);
            if (tile instanceof TileEntityMarker) {
                int floorY = ((TileEntityMarker) tile).getDestination();
                if (!floors.containsKey(floorY)) {
                    floors.put(floorY, new ArrayList<>());
                }
                floors.get(floorY).add(((TileEntityMarker) tile).getFloorName());
            }
        }
        return floors;
    }

    private static class FloorHoverChecker extends HoverChecker{

        private final List<String> floorNames;

        public FloorHoverChecker(GuiButton button, int threshold, List<String> floorNames) {
            super(button, threshold);
            this.floorNames = floorNames;
        }

        public List<String> getFloorNames() {
            return floorNames;
        }
    }
}
