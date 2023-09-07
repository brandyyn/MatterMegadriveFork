package matteroverdrive.client.render.tileentity.starmap;

import matteroverdrive.Reference;
import matteroverdrive.api.starmap.IShip;
import matteroverdrive.client.data.Color;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.starmap.data.Galaxy;
import matteroverdrive.starmap.data.Planet;
import matteroverdrive.starmap.data.SpaceBody;
import matteroverdrive.starmap.data.TravelEvent;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.util.MOStringHelper;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class StarMapRenderPlanetStats extends StarMapRendererPlanet {
    @Override
    public void renderBody(Galaxy galaxy, SpaceBody spaceBody, TileEntityMachineStarMap starMap, float partialTicks, float viewerDistance) {
        if (spaceBody instanceof Planet) {

            glLineWidth(1);

            Planet to = (Planet) spaceBody;
            Planet from = galaxy.getPlanet(starMap.getGalaxyPosition());

            if (from != null && from != to) {
                glPushMatrix();
                Matrix4f rotationMat = new Matrix4f();
                rotationMat.rotate(Minecraft.getMinecraft().renderViewEntity.rotationYaw * (float) (Math.PI / 180D), new Vector3f(0, -1, 0));
                glEnable(GL_BLEND);
                glPushMatrix();
                Vector4f pos = new Vector4f((float) (getClampedSize(from) + 0.25), 0, 0, 1);
                Matrix4f.transform(rotationMat, pos, pos);
                glTranslated(pos.x, pos.y, pos.z);
                renderPlanet(from, viewerDistance);
                glPopMatrix();

                glPushMatrix();
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_ALPHA_TEST);
                RenderUtils.rotateTo(Minecraft.getMinecraft().renderViewEntity);
                glScaled(0.01, 0.01, 0.01);
                glRotated(180, 0, 0, 1);
                drawTravelingShips(galaxy, to);
                glTranslated(-9, -9, 0);
                ClientProxy.holoIcons.renderIcon("arrow_right", 0, 0);
                glPopMatrix();

                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE);
                glPushMatrix();
                pos = new Vector4f((float) -(getClampedSize(to) + 0.25), 0, 0, 1);
                Matrix4f.transform(rotationMat, pos, pos);
                glTranslated(pos.x, pos.y, pos.z);
                renderPlanet(to, viewerDistance);
                glPopMatrix();
                glPopMatrix();
            } else {
                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE);
                glPushMatrix();
                renderPlanet(to, viewerDistance);
                glPopMatrix();
            }

            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_TEXTURE_2D);
        }
    }

    protected void drawTravelingShips(Galaxy galaxy, Planet planet) {
        int heightCount = -24;
        for (TravelEvent travelEvent : galaxy.getTravelEvents()) {
            if (travelEvent.getTo().equals(planet) && ((IShip) travelEvent.getShip().getItem()).isOwner(travelEvent.getShip(), Minecraft.getMinecraft().thePlayer)) {
                String time = MOStringHelper.formatRemainingTime(travelEvent.getTimeRemainning(Minecraft.getMinecraft().theWorld) / 20);
                int width = fontRenderer.getStringWidth(time);
                RenderUtils.renderStack(-8, heightCount - 8, 0, travelEvent.getShip(), false);
                fontRenderer.drawString(time, -width / 2, heightCount + 8, Reference.COLOR_HOLO.getColor());
                heightCount -= 26;
            }
        }
    }

    @Override
    protected void drawPlanetInfoClose(Planet planet) {

    }

    @Override
    public void renderGUIInfo(Galaxy galaxy, SpaceBody spaceBody, TileEntityMachineStarMap starMap, float partialTicks, float opacity) {
        if (spaceBody instanceof Planet) {
            Planet planet = (Planet) spaceBody;
            int y = 0;
            for (ItemStack shipStack : planet.getFleet()) {
                if (shipStack.getItem() instanceof IShip) {
                    IShip ship = (IShip) shipStack.getItem();
                    Color shipColor = Reference.COLOR_HOLO;
                    if (!ship.isOwner(shipStack, Minecraft.getMinecraft().thePlayer)) {
                        shipColor = Reference.COLOR_HOLO_RED;
                    }

                    RenderUtils.renderStack(16, y - 16, 0, shipStack, false);
                    fontRenderer.drawString(shipStack.getDisplayName(), 36, y - 10, shipColor.getColor());
                    y -= 16;
                }
            }
        }
    }

    @Override
    public boolean displayOnZoom(int zoom, SpaceBody spaceBody) {
        return zoom == 4;
    }
}
