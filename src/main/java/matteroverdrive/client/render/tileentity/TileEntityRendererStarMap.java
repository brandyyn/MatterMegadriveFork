package matteroverdrive.client.render.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.api.renderer.ISpaceBodyHoloRenderer;
import matteroverdrive.gui.GuiStarMap;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.starmap.GalaxyClient;
import matteroverdrive.starmap.data.SpaceBody;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import java.util.Collection;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public class TileEntityRendererStarMap extends TileEntityRendererStation<TileEntityMachineStarMap> {
    @Override
    protected void renderHologram(TileEntityMachineStarMap starMap, double x, double y, double z, float partialTicks, double noise) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiStarMap)) {
            if (isUsable(starMap)) {
                render(starMap, x, y, z, partialTicks);
            } else {
                super.renderHologram(starMap, x, y, z, partialTicks, noise);
            }
        }
    }

    public void render(TileEntityMachineStarMap starMap, double x, double y, double z, float partialTicks) {
        renderHologramBase(starMap, x, y, z, partialTicks);
    }

    protected void renderHologramBase(TileEntityMachineStarMap starMap, double x, double y, double z, float partialTicks) {
        glPushMatrix();
        glTranslated(x, y, z);
        glTranslated(0.5, 0.5, 0.5);
        glEnable(GL_BLEND);
        glDisable(GL_LIGHTING);
        RenderUtils.disableLightmap();
        glBlendFunc(GL_ONE, GL_ONE);
        float distance = (float) Vec3.createVectorHelper(x, y, z).lengthVector();

        if (starMap.getActiveSpaceBody() != null) {
            Collection<ISpaceBodyHoloRenderer> renderers = ClientProxy.renderHandler.getStarmapRenderRegistry().getStarmapRendererCollection(starMap.getActiveSpaceBody().getClass());
            if (renderers != null) {
                for (ISpaceBodyHoloRenderer renderer : renderers) {
                    if (renderer.displayOnZoom(starMap.getZoomLevel(), starMap.getActiveSpaceBody())) {
                        SpaceBody spaceBody = starMap.getActiveSpaceBody();
                        if (spaceBody != null) {
                            glTranslated(0, renderer.getHologramHeight(spaceBody), 0);
                            glPushMatrix();
                            renderer.renderBody(GalaxyClient.getInstance().getTheGalaxy(), spaceBody, starMap, partialTicks, distance);
                            glPopMatrix();

                            if (drawHoloLights()) {
                                glPushMatrix();
                                Vec3 playerPosition = Minecraft.getMinecraft().renderViewEntity.getPosition(partialTicks);
                                playerPosition.yCoord = 0;
                                Vec3 mapPosition = Vec3.createVectorHelper(starMap.xCoord + 0.5, 0, starMap.zCoord + 0.5);
                                Vec3 dir = playerPosition.subtract(mapPosition).normalize();
                                double angle = Math.acos(dir.dotProduct(Vec3.createVectorHelper(1, 0, 0)));
                                if (Vec3.createVectorHelper(0, 1, 0).dotProduct(dir.crossProduct(Vec3.createVectorHelper(1, 0, 0))) < 0) {
                                    angle = Math.PI * 2 - angle;
                                }
                                drawHoloGuiInfo(renderer, spaceBody, starMap, (Math.PI / 2 - angle) * (180 / Math.PI), partialTicks);
                                glPopMatrix();
                            }
                        }
                    }
                }
            }
        }
        glPopMatrix();
        RenderUtils.enableLightmap();
    }

    @Override
    protected boolean drawHoloLights() {
        return !(Minecraft.getMinecraft().currentScreen instanceof GuiStarMap);
    }

    @Override
    protected double getLightHeight() {
        return 1;
    }

    @Override
    protected double getLightsSize() {
        return 2;
    }

    public void drawHoloGuiInfo(ISpaceBodyHoloRenderer renderer, SpaceBody spaceBody, TileEntityMachineStarMap starMap, double angle, float partialTicks) {
        angle = Math.round(angle / 90d) * 90;
        glPushMatrix();
        glTranslated(0, -renderer.getHologramHeight(spaceBody) + 0.3, 0);
        glRotated(angle, 0, 1, 0);
        glTranslated(1, 0, -0.8);
        glScaled(0.01, 0.01, 0.01);
        glRotated(180, 0, 0, 1);
        if (spaceBody != null)
            renderer.renderGUIInfo(GalaxyClient.getInstance().getTheGalaxy(), spaceBody, starMap, partialTicks, 0.5f);
        glPopMatrix();
    }
}
