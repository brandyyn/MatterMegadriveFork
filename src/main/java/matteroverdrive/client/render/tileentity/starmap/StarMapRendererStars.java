package matteroverdrive.client.render.tileentity.starmap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.starmap.data.Quadrant;
import matteroverdrive.starmap.data.Star;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public abstract class StarMapRendererStars extends StarMapRendererAbstract {
    protected void renderStars(Quadrant quadrant, TileEntityMachineStarMap starMap, double distanceMultiply, double starSizeMultiply) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        glColor4d(1, 1, 1, 1);
        Vec3 pos = Vec3.createVectorHelper(0, 0, 0);

        if (quadrant != null) {
            glLineWidth(1);

            Tessellator.instance.startDrawingQuads();
            Star from = null, to = null;
            bindTexture(ClientProxy.renderHandler.getRenderParticlesHandler().getAdditiveTextureSheet());
            for (Star star : quadrant.getStars()) {
                pos.xCoord = star.getPosition().xCoord * distanceMultiply;
                pos.yCoord = star.getPosition().yCoord * distanceMultiply;
                pos.zCoord = star.getPosition().zCoord * distanceMultiply;
                drawStarParticle(quadrant, star, pos, player, starMap, starSizeMultiply);
                if (starMap.getGalaxyPosition().equals(star)) {
                    from = star;
                }
                if (starMap.getDestination().equals(star)) {
                    to = star;
                }
            }

            if (from != null && to != null && from != to)
                drawConnection(from, to, distanceMultiply);
            Tessellator.instance.draw();
        }
    }

    protected void drawConnection(Star from, Star to, double distanceMultiply) {
        glDisable(GL_TEXTURE_2D);
        RenderUtils.applyColorWithMultipy(Reference.COLOR_HOLO, 0.3f);
        glBegin(GL_LINE_STRIP);
        glVertex3d(from.getX() * distanceMultiply, from.getY() * distanceMultiply, from.getZ() * distanceMultiply);
        glVertex3d(to.getX() * distanceMultiply, to.getY() * distanceMultiply, to.getZ() * distanceMultiply);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    protected void drawStarParticle(Quadrant quadrant, Star star, Vec3 pos, EntityPlayer player, TileEntityMachineStarMap starMap, double starSizeMultiply) {
        Color color = getStarColor(star, player);
        double size = 0.01;
        if (starMap.getDestination().equals(star)) {
            size = 0.035;
            RenderUtils.tessalateParticle(Minecraft.getMinecraft().renderViewEntity, selectedIcon, star.getSize() * 0.05 * starSizeMultiply, pos, color);
        }
        if (starMap.getGalaxyPosition().equals(star)) {
            size = 0.035;
            RenderUtils.tessalateParticle(Minecraft.getMinecraft().renderViewEntity, currentIcon, star.getSize() * 0.05 * starSizeMultiply, pos, color);
        }
        if (star.isClaimed(player) == 3) {
            size = 0.025;
        }
        RenderUtils.tessalateParticle(Minecraft.getMinecraft().renderViewEntity, star_icon, star.getSize() * size * starSizeMultiply, pos, color);
    }

    private void bindTexture(ResourceLocation location) {
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
    }

    public static Color getStarColor(Star star, EntityPlayer player) {
        return new Color(star.getColor());
    }
}
