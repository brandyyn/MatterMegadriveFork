package matteroverdrive.gui.pages;

import matteroverdrive.container.slot.MOSlot;
import matteroverdrive.gui.MOGuiMachine;
import matteroverdrive.gui.element.ElementInventorySlot;
import matteroverdrive.gui.element.MOElementButton;
import matteroverdrive.gui.element.MOElementTextField;
import matteroverdrive.gui.events.ITextHandler;
import matteroverdrive.machines.components.ComponentConfigs;
import matteroverdrive.machines.components.ComponentMatterNetworkConfigs;


public class MatterNetworkConfigPage extends AutoConfigPage implements ITextHandler {
    ComponentMatterNetworkConfigs componentMatterNetworkConfigs;
    ElementInventorySlot filterSlot;
    MOElementTextField destinationTextField;

    public MatterNetworkConfigPage(MOGuiMachine gui, int posX, int posY, int width, int height, ComponentMatterNetworkConfigs componentMatterNetworkConfigs, ComponentConfigs configurable) {
        super(gui, posX, posY, width, height, configurable);
        destinationTextField = new MOElementTextField(gui, this, 4, 42, 96, 16);
        destinationTextField.setName("Destination");
        destinationTextField.setBackground(MOElementButton.HOVER_TEXTURE_DARK);
        destinationTextField.setTextOffset(4, 3);
        this.componentMatterNetworkConfigs = componentMatterNetworkConfigs;
        filterSlot = new ElementInventorySlot(gui, (MOSlot) machineGui.inventorySlots.getSlot(componentMatterNetworkConfigs.getDestinationFilterSlot()), 104, 37, 22, 22, "big");
    }

    @Override
    public void init() {
        super.init();
        addElement(destinationTextField);
        if (componentMatterNetworkConfigs != null) {
            if (componentMatterNetworkConfigs.getDestinationFilter() != null)
                destinationTextField.setText(componentMatterNetworkConfigs.getDestinationFilter());
            addElement(filterSlot);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);
        getFontRenderer().drawString("Destination Address:", posX, posY + 28, 0xFFFFFF);
    }

    @Override
    public void textChanged(String elementName, String text, boolean typed) {
        if (elementName.equals("Destination")) {
            if (componentMatterNetworkConfigs != null) {
                componentMatterNetworkConfigs.setDestinationFilter(text);
                machineGui.getMachine().sendConfigsToServer(false);
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);
        int x = destinationTextField.getPosX() + destinationTextField.getWidth() + 10;
        /*for (ElementSlot slot : networkConfigSlots)
        {
            slot.setPosition(x,destinationTextField.getPosY() + 6);
            x += slot.getWidth();
        }*/
    }
}
