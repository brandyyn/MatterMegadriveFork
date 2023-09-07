package matteroverdrive.client.render.entity;

import matteroverdrive.Reference;
import matteroverdrive.entity.weapon.PlasmaBolt;
import matteroverdrive.util.RenderUtils;
import matteroverdrive.util.animation.MOEasing;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class EntityRendererPhaserFire extends Render {
    private static final ResourceLocation arrowTextures = new ResourceLocation(Reference.PATH_ENTITIES + "PlasmaFire.png");

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(PlasmaBolt plasmaBolt, double x, double y, double z, float p_76986_8_, float p_76986_9_) {
        this.bindEntityTexture(plasmaBolt);
        RenderUtils.disableLightmap();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        RenderUtils.applyColorWithMultipy(plasmaBolt.getColor(), MOEasing.Quad.easeOut(plasmaBolt.getLife(), 0, 1, 0.7f));
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(plasmaBolt.prevRotationYaw + (plasmaBolt.rotationYaw - plasmaBolt.prevRotationYaw) * p_76986_9_ - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(plasmaBolt.prevRotationPitch + (plasmaBolt.rotationPitch - plasmaBolt.prevRotationPitch) * p_76986_9_, 0.0F, 0.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        byte b0 = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float) (b0 * 10) / 32.0F;
        float f5 = (float) (5 + b0 * 10) / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = (float) (5 + b0 * 10) / 32.0F;
        float f9 = (float) (10 + b0 * 10) / 32.0F;
        float f10 = 0.05625F;
        float renderSize = plasmaBolt.getRenderSize();
        double length = 6 * Vec3.createVectorHelper(plasmaBolt.motionX, plasmaBolt.motionY, plasmaBolt.motionZ).lengthVector() + 10;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(f10, f10, f10);
        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
        GL11.glNormal3f(f10, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(1, -renderSize, -renderSize, (double) f6, (double) f8);
        tessellator.addVertexWithUV(1, -renderSize, renderSize, (double) f7, (double) f8);
        tessellator.addVertexWithUV(1, renderSize, renderSize, (double) f7, (double) f9);
        tessellator.addVertexWithUV(1, renderSize, -renderSize, (double) f6, (double) f9);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(length - 1, -renderSize, renderSize, (double) f7, (double) f8);
        tessellator.addVertexWithUV(length - 1, renderSize, renderSize, (double) f7, (double) f9);
        tessellator.addVertexWithUV(length - 1, renderSize, -renderSize, (double) f6, (double) f9);
        tessellator.addVertexWithUV(length - 1, -renderSize, -renderSize, (double) f6, (double) f8);
        tessellator.draw();

        for (int i = 0; i < 2; ++i) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, f10);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(0, -renderSize, 0.0D, (double) f2, (double) f4);
            tessellator.addVertexWithUV(length, -renderSize, 0.0D, (double) f3, (double) f4);
            tessellator.addVertexWithUV(length, renderSize, 0.0D, (double) f3, (double) f5);
            tessellator.addVertexWithUV(0, renderSize, 0.0D, (double) f2, (double) f5);
            tessellator.draw();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        RenderUtils.enableLightmap();
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(PlasmaBolt p_110775_1_) {
        return arrowTextures;
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return this.getEntityTexture((PlasmaBolt) p_110775_1_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        this.doRender((PlasmaBolt) p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}
