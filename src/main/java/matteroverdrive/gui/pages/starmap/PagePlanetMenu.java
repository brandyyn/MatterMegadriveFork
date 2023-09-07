package matteroverdrive.gui.pages.starmap;

import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.container.ContainerStarMap;
import matteroverdrive.container.slot.MOSlot;
import matteroverdrive.gui.MOGuiBase;
import matteroverdrive.gui.element.ElementBaseGroup;
import matteroverdrive.gui.element.ElementInventorySlot;
import matteroverdrive.gui.element.starmap.ElementSlotBuilding;
import matteroverdrive.gui.element.starmap.ElementSlotShip;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.starmap.data.Planet;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.util.StarmapHelper;
import net.minecraft.client.Minecraft;

import static org.lwjgl.opengl.GL11.*;

public class PagePlanetMenu extends ElementBaseGroup {

    TileEntityMachineStarMap starMap;
    ElementInventorySlot[] factorySlots;
    ElementInventorySlot[] shipSlots;

    public PagePlanetMenu(MOGuiBase gui, int posX, int posY, int width, int height, ContainerStarMap starMapContainer, TileEntityMachineStarMap starMap) {
        super(gui, posX, posY, width, height);
        //this.starMap = starMap;
        factorySlots = new ElementInventorySlot[Planet.SLOT_COUNT / 2];
        shipSlots = new ElementInventorySlot[Planet.SLOT_COUNT / 2];
        this.starMap = starMap;

        for (int i = 0; i < factorySlots.length; i++) {
            double angle = (-Math.PI / 1.8) + (Math.PI / 15) * i;
            factorySlots[i] = new ElementSlotBuilding(gui, (MOSlot) starMapContainer.getSlot(i), width / 2 - 10 + (int) (Math.sin(angle) * 140), height / 2 - 48 + (int) (Math.cos(angle) * 140), 22, 22, "holo", ClientProxy.holoIcons.getIcon("factory"), starMap);
            factorySlots[i].setColor(new Color(Reference.COLOR_HOLO.getIntR() / 2, Reference.COLOR_HOLO.getIntG() / 2, Reference.COLOR_HOLO.getIntB() / 2));
        }

        for (int i = 0; i < shipSlots.length; i++) {
            double angle = (-Math.PI / 1.8) + (Math.PI / 15) * (i + factorySlots.length);
            MOSlot slot = (MOSlot) starMapContainer.getSlot(i + factorySlots.length);
            shipSlots[i] = new ElementSlotShip(gui, slot, width / 2 - 10 + (int) (Math.sin(angle) * 140), height / 2 - 48 + (int) (Math.cos(angle) * 140), 22, 22, "holo", ClientProxy.holoIcons.getIcon("icon_shuttle"), starMap);
            shipSlots[i].setColor(new Color(Reference.COLOR_HOLO.getIntR() / 2, Reference.COLOR_HOLO.getIntG() / 2, Reference.COLOR_HOLO.getIntB() / 2));
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        boolean planetSlotVisible = starMap.getPlanet() != null && starMap.getPlanet().isOwner(Minecraft.getMinecraft().thePlayer);

        for (ElementInventorySlot slot : factorySlots) {
            slot.setVisible(planetSlotVisible);
        }

        for (ElementInventorySlot slot : shipSlots) {
            slot.setVisible(planetSlotVisible);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);
        if (starMap.getPlanet() != null) {
            glPushMatrix();
            int width = getFontRenderer().getStringWidth(starMap.getPlanet().getName());
            glTranslated(sizeY / 2 + width / 2, 16, 0);
            glScaled(1, 1, 1);
            StarmapHelper.drawPlanetInfo(starMap.getPlanet(), starMap.getPlanet().getName(), 12 - width / 2, 4);
            glPopMatrix();
        }
    }

    @Override
    public void init() {
        super.init();
        for (int i = 0; i < factorySlots.length; i++) {
            addElement(factorySlots[i]);
        }
        for (int i = 0; i < shipSlots.length; i++) {
            addElement(shipSlots[i]);
        }
    }
}
