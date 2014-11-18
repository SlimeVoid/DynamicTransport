package net.slimevoid.dynamictransport.client.presentation.gui;

import net.minecraft.client.gui.GuiButton;

public class GuiFloorButton extends GuiButton {

    private String floorLevel;

    public GuiFloorButton(int id, int x, int y, int width, int height, String floorLevel, String floorName) {
        super(id,x,y,width,height,floorName);
        this.floorLevel = floorLevel;
    }

    public String getFloorLevel() {
        return this.floorLevel;
    }
}
