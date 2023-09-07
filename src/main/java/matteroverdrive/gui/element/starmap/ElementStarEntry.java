package matteroverdrive.gui.element.starmap;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.starmap.GalacticPosition;
import matteroverdrive.api.starmap.IShip;
import matteroverdrive.client.data.Color;
import matteroverdrive.client.render.HoloIcon;
import matteroverdrive.client.render.tileentity.starmap.StarMapRendererStars;
import matteroverdrive.gui.GuiStarMap;
import matteroverdrive.gui.element.ElementGroupList;
import matteroverdrive.network.packet.server.starmap.PacketStarMapClientCommands;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.starmap.GalaxyClient;
import matteroverdrive.starmap.data.Planet;
import matteroverdrive.starmap.data.Star;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementStarEntry extends ElementAbstractStarMapEntry<Star> {

    public ElementStarEntry(GuiStarMap gui, ElementGroupList groupList, int width, int height, Star star) {
        super(gui, groupList, width, height, star);
    }

    public void addTooltip(List<String> list) {

    }

    @Override
    protected void drawElementName(Star star, Color color, float multiply) {
        String name = spaceBody.getName();
        GuiStarMap guiStarMap = (GuiStarMap) gui;
        if (guiStarMap.getMachine().getGalaxyPosition().equals(star)) {
            name = "@ " + EnumChatFormatting.ITALIC + name;
        }

        if (Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode || GalaxyClient.getInstance().canSeeStarInfo(star, Minecraft.getMinecraft().thePlayer)) {
            RenderUtils.drawString(name, posX + 16, posY + 10, color, multiply);
        } else {
            RenderUtils.drawString(Minecraft.getMinecraft().standardGalacticFontRenderer, name, posX + 16, posY + 10, color, multiply);
        }
    }

    @Override
    protected Map<HoloIcon, Integer> getIcons(Star star) {
        HashMap<HoloIcon, Integer> icons = new HashMap<>();
        HoloIcon homeIcon = ClientProxy.holoIcons.getIcon("home_icon");
        HoloIcon shipIcon = ClientProxy.holoIcons.getIcon("icon_shuttle");
        HoloIcon factoryIcon = ClientProxy.holoIcons.getIcon("factory");
        icons.put(shipIcon, 0);
        icons.put(factoryIcon, 0);
        for (Planet planet : star.getPlanets()) {
            if (planet.isOwner(Minecraft.getMinecraft().thePlayer)) {
                if (planet.isHomeworld()) {
                    icons.put(homeIcon, -1);
                }
                if (planet.getBuildings().size() > 0) {
                    icons.put(factoryIcon, icons.get(factoryIcon) + 1);
                }
            }
            for (ItemStack ship : planet.getFleet()) {
                if (((IShip) ship.getItem()).isOwner(ship, Minecraft.getMinecraft().thePlayer)) {
                    icons.put(shipIcon, icons.get(shipIcon) + 1);
                }
            }
        }
        return icons;
    }

    @Override
    protected boolean canTravelTo(Star star, EntityPlayer player) {
        return false;
    }

    @Override
    protected boolean canView(Star spaceBody, EntityPlayer player) {
        return true;
    }

    @Override
    protected void onTravelPress() {
        TileEntityMachineStarMap starMap = ((GuiStarMap) gui).getMachine();
        MatterOverdrive.packetPipeline.sendToServer(new PacketStarMapClientCommands(starMap, starMap.getZoomLevel(), new GalacticPosition(spaceBody), starMap.getDestination()));
    }

    @Override
    protected void onSelectPress() {
        TileEntityMachineStarMap starMap = ((GuiStarMap) gui).getMachine();
        MatterOverdrive.packetPipeline.sendToServer(new PacketStarMapClientCommands(starMap, starMap.getZoomLevel(), starMap.getGalaxyPosition(), new GalacticPosition(spaceBody)));
    }

    protected void onViewPress() {
        gui.setPage(2);
    }

    @Override
    protected Color getSpaceBodyColor(Star star) {
        return StarMapRendererStars.getStarColor(star, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    boolean isSelected(Star star) {
        return ((GuiStarMap) gui).getMachine().getDestination().equals(star);
    }

    @Override
    public float getMultiply(Star star) {
        GuiStarMap guiStarMap = (GuiStarMap) gui;
        if (guiStarMap.getMachine().getDestination().equals(star)) {
            return 1;
        } else if (guiStarMap.getMachine().getGalaxyPosition().equals(star)) {
            return 0.5f;
        }
        return 0.1f;
    }
}


