package matteroverdrive.gui.pages.starmap;

import matteroverdrive.gui.GuiStarMap;
import matteroverdrive.gui.element.ElementBaseGroup;
import matteroverdrive.gui.element.ElementGroupList;
import matteroverdrive.gui.element.starmap.ElementShipEntry;
import matteroverdrive.gui.events.IListHandler;
import matteroverdrive.starmap.GalaxyClient;
import matteroverdrive.starmap.data.Planet;
import matteroverdrive.tile.TileEntityMachineStarMap;

public class PagePlanetStats extends ElementBaseGroup implements IListHandler {
    TileEntityMachineStarMap starMap;
    ElementGroupList shipList;

    public PagePlanetStats(GuiStarMap gui, int posX, int posY, int width, int height, TileEntityMachineStarMap starMap) {
        super(gui, posX, posY, width, height);
        this.starMap = starMap;
        shipList = new ElementGroupList(gui, this, 16, 16, width, 256);
    }

    @Override
    public void init() {
        super.init();
        addElement(shipList);
        loadShips();
    }

    public void loadShips() {
        shipList.init();
        Planet planet = GalaxyClient.getInstance().getTheGalaxy().getPlanet(starMap.getGalaxyPosition());
        if (planet != null && !starMap.getGalaxyPosition().equals(starMap.getDestination())) {
            for (int i = 0; i < planet.getFleet().size(); i++) {
                shipList.addElement(new ElementShipEntry((GuiStarMap) gui, shipList, 186, 32, planet, planet.getFleet().get(i), i));
            }
        }
        shipList.update(0, 0);
    }

    @Override
    public void ListSelectionChange(String name, int selected) {

    }
}
