package net.slimevoid.dynamictransport.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.slimevoid.dynamictransport.network.play.client.CPacketUpdateMarker;
import net.slimevoid.dynamictransport.tileentity.TileEntityMarker;

import java.io.IOException;

public class GuiConfigureMarker extends GuiScreen {
    private GuiTextField nameField;
    private final TileEntityMarker tileMarker;

    public GuiConfigureMarker(TileEntityMarker t) {
        tileMarker = t;
    }

    @Override
    public void initGui() {
        this.nameField = new GuiTextField(3,this.fontRenderer, this.width / 2 - 100, this.height / 2 - 52, 200,20);
        this.nameField.setMaxStringLength(15);
        this.nameField.setEnableBackgroundDrawing(true);
        this.nameField.setVisible(true);
        this.nameField.setTextColor(16777215);
        this.nameField.setCanLoseFocus(false);
        this.nameField.setFocused(true);
        this.nameField.setText(tileMarker.floorName);

        this.buttonList.add(new GuiButton(0, this.width / 2 - 70, this.height / 2 - 24, 20, 20, "-"));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 50, this.height / 2 - 24, 20, 20, "+"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 2 + 32, I18n.format("gui.done")));
        GuiLabel floorName;
        this.labelList.add(floorName = new GuiLabel(this.fontRenderer,4,this.width / 2 - 100, this.height / 2 - 70,100,20,-1));
        floorName.addLine("dynamictransport.gui.floormarker.name");
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.nameField.drawTextBox();
        this.drawCenteredString(this.fontRenderer,Integer.toString(tileMarker.offSet), this.width / 2, this.height / 2 - 19, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.nameField.textboxKeyTyped(typedChar,keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.nameField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton Button)
    {
        if (Button.enabled)
        {
            switch (Button.id) {
                case 0:
                    this.tileMarker.offSet -= 1;
                    break;
                case 1:
                    this.tileMarker.offSet += 1;
                    break;
                case 2:
                    DynamicTransportMod.CHANNEL.sendToServer(new CPacketUpdateMarker(this.tileMarker.getPos(), this.tileMarker.offSet, this.nameField.getText()));
                    this.mc.displayGuiScreen(null);
                    if (this.mc.currentScreen == null)
                        this.mc.setIngameFocus();
                    break;
            }
        }
    }
}
