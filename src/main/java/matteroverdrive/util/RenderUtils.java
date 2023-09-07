package matteroverdrive.util;

import matteroverdrive.client.data.Color;
import matteroverdrive.client.render.tileentity.TileEntityRendererPatternMonitor;
import matteroverdrive.util.math.MOMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {
    private static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private static TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    private static RenderItem renderItem = new RenderItem();
    private static float lastLightMapX, lastLightMapY;

    public static void renderStack(int x, int y, ItemStack stack) {
        renderStack(x, y, 32, stack, false);
    }

    public static void renderStack(int x, int y, int z, ItemStack stack, boolean renderOverlay) {
        if (stack != null && stack.getItem() != null) {
            glPushMatrix();
            glColor3f(1, 1, 1);
            RenderHelper.enableGUIStandardItemLighting();
            glEnable(GL_LIGHTING);
            glEnable(GL_DEPTH_TEST);
            GL11.glTranslatef(0.0F, 0.0F, z);
            //this.zLevel = 200.0F;
            renderItem.zLevel = -50f;
            FontRenderer font = null;
            if (stack != null) font = stack.getItem().getFontRenderer(stack);
            if (font == null) font = Minecraft.getMinecraft().fontRenderer;
            renderItem.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, x, y);
            if (renderOverlay)
                renderItemOverlayIntoGUI(font, stack, x, y, stack.stackSize > 1 ? Integer.toString(stack.stackSize) : null);
            renderItem.zLevel = 0.0F;

            glEnable(GL_ALPHA_TEST);
            glDisable(GL_LIGHTING);
            glPopMatrix();
        }
    }

    /**
     * Should be the same as {@link RenderItem#renderItemOverlayIntoGUI(FontRenderer, TextureManager, ItemStack, int, int, String)}
     */
    public static void renderItemOverlayIntoGUI(FontRenderer p_94148_1_, ItemStack p_94148_3_, int p_94148_4_, int p_94148_5_, String p_94148_6_) {
        if (p_94148_3_ != null) {
            if (p_94148_3_.stackSize > 1 || p_94148_6_ != null) {
                String s1 = p_94148_6_ == null ? String.valueOf(p_94148_3_.stackSize) : p_94148_6_;
                GL11.glDisable(GL11.GL_LIGHTING);
                //GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                p_94148_1_.drawStringWithShadow(s1, p_94148_4_ + 19 - 2 - p_94148_1_.getStringWidth(s1), p_94148_5_ + 6 + 3, 16777215);
                GL11.glEnable(GL11.GL_LIGHTING);
                //GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            if (p_94148_3_.getItem().showDurabilityBar(p_94148_3_)) {
                double health = p_94148_3_.getItem().getDurabilityForDisplay(p_94148_3_);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                GL11.glDisable(GL11.GL_LIGHTING);
                //GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                Tessellator tessellator = Tessellator.instance;
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                drawPlaneWithUV(p_94148_4_ + 2, p_94148_5_ + 13, 0, 13, 2, 0, 0, 0, 0, 0);
                drawPlaneWithUV(p_94148_4_ + 2, p_94148_5_ + 13, 0, 12, 1, 0, 0, 0, 0, i1);
                drawPlaneWithUV(p_94148_4_ + 2, p_94148_5_ + 13, 0, j1, 1, 0, 0, 0, 0, l);
                GL11.glEnable(GL11.GL_BLEND); // Forge: Disable Bled because it screws with a lot of things down the line.
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                //GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    public static void renderStack(int x, int y, ItemStack stack, float opacity) {
        if (stack != null && stack.getItem() != null) {
            glColor3f(1, 1, 1);
            glDisable(GL_CULL_FACE);
            glDepthMask(true);
            RenderHelper.enableGUIStandardItemLighting();
            renderItem.renderItemIntoGUI(fontRenderer, textureManager, stack, x, y);
            RenderHelper.disableStandardItemLighting();
            glEnable(GL_CULL_FACE);
        }
    }

    public static void rotateFromBlock(World world, int x, int y, int z) {
        if (world != null) {
            int metadata = world.getBlockMetadata(x, y, z);

            ForgeDirection direction = ForgeDirection.values()[metadata];

            if (direction == ForgeDirection.WEST) {
                GL11.glRotated(-90, 0, 1, 0);
            } else if (direction == ForgeDirection.EAST) {
                GL11.glRotated(90, 0, 1, 0);
            } else if (direction == ForgeDirection.NORTH) {
                GL11.glRotated(-180, 0, 1, 0);
            }
        }

        //System.out.println("Metadata " + metadata + "at [" + x +","+ y +","+ z + "]");
    }

    public static void rotateFromBlock(Matrix4f mat, IBlockAccess world, int x, int y, int z) {
        if (world != null) {
            int metadata = world.getBlockMetadata(x, y, z);

            ForgeDirection direction = ForgeDirection.values()[metadata];
            Vector3f axis = new Vector3f(0, 1, 0);

            if (direction == ForgeDirection.WEST) {
                mat.rotate(-(float) (Math.PI / 2), axis);
            } else if (direction == ForgeDirection.EAST) {
                mat.rotate((float) (Math.PI / 2), axis);
            } else if (direction == ForgeDirection.NORTH) {
                mat.rotate(-(float) (Math.PI), axis);
            }
        }
    }

    public static void drawCircle(double radius, int segments) {
        glBegin(GL_POLYGON);
        for (int i = 0; i < segments; i++) {
            glVertex3d(Math.sin((i / (double) segments) * Math.PI * 2) * radius, Math.cos((i / (double) segments) * Math.PI * 2) * radius, 0);
        }
        glEnd();
    }

    public static void drawPlane(double size) {
        drawPlane(size, size);
    }

    public static void drawPlane(double x, double y, double z, double sizeX, double sizeY) {
        drawPlaneWithUV(x, y, z, sizeX, sizeY, 0, 0, 1, 1);
    }

    public static void drawPlane(double sizeX, double sizeY) {
        drawPlaneWithUV(0, 0, 0, sizeX, sizeY, 0, 0, 1, 1);
    }

    public static void drawPlaneWithUV(double sizeX, double sizeY, double uStart, double vStart, double uSize, double vSize) {
        drawPlaneWithUV(0, 0, 0, sizeX, sizeY, uStart, vStart, uSize, vSize);
    }

    public static void drawPlaneWithUV(double x, double y, double z, double sizeX, double sizeY, double uStart, double vStart, double uSize, double vSize) {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z, uStart + uSize, vStart + vSize);
        tessellator.addVertexWithUV(x + sizeX, y, z, uStart + uSize, vStart);
        tessellator.addVertexWithUV(x, y, z, uStart, vStart);
        tessellator.addVertexWithUV(x, y + sizeY, z, uStart, vStart + vSize);
        tessellator.draw();
    }

    public static void drawPlaneWithUV(double x, double y, double z, double sizeX, double sizeY, double uStart, double vStart, double uSize, double vSize, int color) {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(color);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z, uStart + uSize, vStart + vSize);
        tessellator.addVertexWithUV(x + sizeX, y, z, uStart + uSize, vStart);
        tessellator.addVertexWithUV(x, y, z, uStart, vStart);
        tessellator.addVertexWithUV(x, y + sizeY, z, uStart, vStart + vSize);
        tessellator.draw();
    }

    public static void drawStencil(int xMin, int yMin, int xMax, int yMax, int mask) {
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glStencilFunc(GL_ALWAYS, mask, mask);
        GL11.glStencilOp(0, 0, GL_REPLACE);
        GL11.glStencilMask(1);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        Tessellator.instance.startDrawingQuads();
        Tessellator.instance.addVertex((double) xMin, (double) yMax, 0.0D);
        Tessellator.instance.addVertex((double) xMax, (double) yMax, 0.0D);
        Tessellator.instance.addVertex((double) xMax, (double) yMin, 0.0D);
        Tessellator.instance.addVertex((double) xMin, (double) yMin, 0.0D);
        Tessellator.instance.draw();
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glStencilFunc(GL_EQUAL, mask, mask);
        GL11.glStencilMask(0);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
    }

    public static void drawCube(double sizeX, double sizeY, double sizeZ, Color color) {
        drawCube(0, 0, 0, sizeX, sizeY, sizeZ, color);
    }

    public static void drawCube(double sizeX, double sizeY, double sizeZ, Color color, float multiply) {
        drawCube(0, 0, 0, sizeX, sizeY, sizeZ, new Color((int) (color.getIntR() * multiply), (int) (color.getIntG() * multiply), (int) (color.getIntB() * multiply)));
    }

    public static void drawCube(double x, double y, double z, double sizeX, double sizeY, double sizeZ, Color color) {
        drawCube(x, y, z, sizeX, sizeY, sizeZ, 0, 0, 1, 1, color);
    }

    public static void drawCube(double x, double y, double z, double sizeX, double sizeY, double sizeZ, double minU, double minV, double maxU, double maxV, Color color) {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();
        if (color != null)
            tessellator.setColorRGBA(color.getIntR(), color.getIntG(), color.getIntB(), color.getIntA());
        //tessellator.setBrightness(255);

        //base
        tessellator.addVertexWithUV(x, y, z, minU, minV);
        tessellator.addVertexWithUV(x + sizeX, y, z, maxU, minV);
        tessellator.addVertexWithUV(x + sizeX, y, z + sizeZ, maxU, maxV);
        tessellator.addVertexWithUV(x, y, z + sizeZ, minU, maxV);

        //top
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z, maxU, minV);
        tessellator.addVertexWithUV(x, y + sizeY, z, minU, minV);
        tessellator.addVertexWithUV(x, y + sizeY, z + sizeZ, minU, maxV);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z + sizeZ, maxU, maxV);

        //west
        tessellator.addVertexWithUV(x, y, z, minU, minV);
        tessellator.addVertexWithUV(x, y, z + sizeZ, maxU, minV);
        tessellator.addVertexWithUV(x, y + sizeY, z + sizeZ, maxU, maxV);
        tessellator.addVertexWithUV(x, y + sizeY, z, minU, maxV);

        //east
        tessellator.addVertexWithUV(x + sizeX, y, z + sizeZ, maxU, minV);
        tessellator.addVertexWithUV(x + sizeX, y, z, minU, minV);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z, minU, maxV);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z + sizeZ, maxU, maxV);

        //north
        tessellator.addVertexWithUV(x, y, z, minU, minV);
        tessellator.addVertexWithUV(x, y + sizeY, z, minU, maxV);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z, maxU, maxV);
        tessellator.addVertexWithUV(x + sizeX, y, z, maxU, minV);

        //south
        tessellator.addVertexWithUV(x, y + sizeY, z + sizeZ, minU, maxV);
        tessellator.addVertexWithUV(x, y, z + sizeZ, minU, minV);
        tessellator.addVertexWithUV(x + sizeX, y, z + sizeZ, maxU, minV);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, z + sizeZ, maxU, maxV);

        tessellator.draw();
    }

    public static void tesseleteModelAsBlock(Matrix4f mat, GroupObject object, IIcon icon, int x, int y, int z, int brightness, boolean lighting, Color color) {
        float uSize = icon.getMaxU() - icon.getMinU();
        float vSize = icon.getMaxV() - icon.getMinV();
        float textureOffset = 0.00005F;
        Vector4f pos = new Vector4f(0, 0, 0, 1);

        for (Face face : object.faces) {
            if (face.faceNormal == null) {
                face.faceNormal = face.calculateFaceNormal();
            }
            float averageU = 0F;
            float averageV = 0F;

            if ((face.textureCoordinates != null) && (face.textureCoordinates.length > 0)) {
                for (int i = 0; i < face.textureCoordinates.length; ++i) {
                    averageU += face.textureCoordinates[i].u;
                    averageV += face.textureCoordinates[i].v;
                }

                averageU = averageU / face.textureCoordinates.length;
                averageV = averageV / face.textureCoordinates.length;
            }

            float offsetU, offsetV;

            for (int i = 0; i < face.vertices.length; ++i) {

                if ((face.textureCoordinates != null) && (face.textureCoordinates.length > 0)) {
                    offsetU = textureOffset;
                    offsetV = textureOffset;

                    if (face.textureCoordinates[i].u > averageU) {
                        offsetU = -offsetU;
                    }
                    if (face.textureCoordinates[i].v > averageV) {
                        offsetV = -offsetV;
                    }

                    pos.x = face.vertices[i].x;
                    pos.y = face.vertices[i].y;
                    pos.z = face.vertices[i].z;
                    pos.w = 1;
                    Matrix4f.transform(mat, pos, pos);

                    Tessellator.instance.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
                    float colorMul = 1f;
                    if (lighting) {
                        colorMul = 0.7f + 0.3f * Vector3f.dot(new Vector3f(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z), new Vector3f(-0.3f, 1, 0));
                    }
                    if (color != null) {
                        Tessellator.instance.setColorOpaque_F(color.getFloatR() * colorMul, color.getFloatG() * colorMul, color.getFloatB() * colorMul);
                    } else {
                        Tessellator.instance.setColorOpaque_F(colorMul, colorMul, colorMul);
                    }
                    if (brightness >= 0) {
                        Tessellator.instance.setBrightness(brightness);
                    }
                    Tessellator.instance.addVertexWithUV(x + 0.5f + pos.x, y + pos.y, z + 0.5f + pos.z, icon.getMinU() + face.textureCoordinates[i].u * uSize + offsetU, icon.getMinV() + face.textureCoordinates[i].v * vSize + offsetV);
                } else {
                    Tessellator.instance.addVertex(x + face.vertices[i].x, y + face.vertices[i].y, z + face.vertices[i].z);
                }
            }
        }
    }

    public static int lerp(int a, int b, float lerp) {
        int MASK1 = 0xff00ff;
        int MASK2 = 0x00ff00;

        int f2 = Math.round(256 * lerp);
        int f1 = Math.round(256 - f2);

        return (((((a & MASK1) * f1) + ((b & MASK1) * f2)) >> 8) & MASK1) | (((((a & MASK2) * f1) + ((b & MASK2) * f2)) >> 8) & MASK2);
    }

    public static Color lerp(Color a, Color b, float lerp) {
        return new Color(lerp(a.getIntR(), b.getIntR(), lerp), lerp(a.getIntG(), b.getIntG(), lerp), lerp(a.getIntB(), b.getIntB(), lerp), lerp(a.getIntA(), b.getIntA(), lerp));
    }

    public static void applyColor(int color) {
        glColor4f((float) (color >> 16 & 255) / 255f, (float) (color >> 8 & 255) / 255f, (float) (color >> 0 & 255) / 256f, (float) (color >> 24 & 255) / 255f);
    }

    public static void applyColor(Color color) {
        glColor3f(color.getFloatR(), color.getFloatG(), color.getFloatB());
    }

    public static void applyColorWithMultipy(Color color, float mul) {
        glColor3f(color.getFloatR() * mul, color.getFloatG() * mul, color.getFloatB() * mul);
    }

    public static void applyColorWithMultipy(int color, float mul) {
        glColor4f((color >> 16 & 255) / 255f * mul, (color >> 8 & 255) / 255f * mul, (color >> 0 & 255) / 256f * mul, (float) (color >> 24 & 255) / 255f);
    }

    public static void applyColorWithAdd(Color color, float add) {
        glColor3f(color.getFloatR() + add, color.getFloatG() + add, color.getFloatB() + add);
    }

    public static void applyColorWithAlpha(Color color, float alphaMultiply) {
        glColor4f(color.getFloatR(), color.getFloatG(), color.getFloatB(), color.getFloatA() * alphaMultiply);
    }

    public static void applyColorWithAlpha(Color color) {
        glColor4f(color.getFloatR(), color.getFloatG(), color.getFloatB(), color.getFloatA());
    }

    public static void beginDrawinngBlockScreen(double x, double y, double z, ForgeDirection side, Color color, TileEntity entity) {
        beginDrawinngBlockScreen(x, y, z, side, color, entity, 0.05, 1);
    }

    public static void beginDrawinngBlockScreen(double x, double y, double z, ForgeDirection side, Color color, TileEntity entity, double offset, float glowAlpha) {
        glEnable(GL_BLEND);
        glDisable(GL_LIGHTING);
        disableLightmap();

        Vector3f dir = new Vector3f(side.offsetX, side.offsetY, side.offsetZ);
        Vector3f front = new Vector3f(0, 0, -1);
        Vector3f c = Vector3f.cross(front, dir, null);
        double omega = Math.acos(Vector3f.dot(front, dir));
        if (omega == Math.PI) {
            c.y = 1;
        }

        glPushMatrix();
        glTranslated(dir.x * (0.5 + offset), dir.y * (0.5 + offset), dir.z * (0.5 + offset));
        glTranslated(x + 0.5, y + 0.5, z + 0.5);
        glRotated(omega * (180 / Math.PI), c.x, c.y, c.z);
        glRotated(180, 0, 0, 1);
        glTranslated(-0.5, -0.5, 0);

        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);

        double multiply = (MOMathHelper.noise(entity.xCoord, entity.getWorldObj().getWorldTime() * 0.01, entity.zCoord) * 0.5 + 0.5) * glowAlpha;
        glColor3d(color.getFloatR() * multiply, color.getFloatG() * multiply, color.getFloatB() * multiply);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TileEntityRendererPatternMonitor.screenTextureGlow);
        RenderUtils.drawPlane(1);

        glTranslated(0, 0, -0.05);
        glColor3d(color.getFloatR() * 0.05f, color.getFloatG() * 0.05f, color.getFloatB() * 0.05f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TileEntityRendererPatternMonitor.screenTextureBack);
        RenderUtils.drawPlane(1);
    }

    public static void drawScreenInfoWithGlobalAutoSize(String[] info, Color color, ForgeDirection side, int leftMargin, int rightMargin, float maxScaleFactor) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPushMatrix();
        glTranslated(0, 0, -0.03);
        glScaled(0.01, 0.01, 0.01);
        int height = 0;
        int maxWidth = 0;

        int sizeX = 100 - leftMargin - rightMargin;
        int sizeY = 80;

        float scaleFactor = 1;

        for (int i = 0; i < info.length; i++) {
            if (maxWidth < fontRenderer.getStringWidth(info[i])) {
                maxWidth = fontRenderer.getStringWidth(info[i]);
            }
        }

        if (maxWidth > 0) {
            scaleFactor = MathHelper.clamp_float((float) sizeX / (float) maxWidth, 0.02f, maxScaleFactor);
        }

        for (int i = 0; i < info.length; i++) {
            int scaledHeight = (int) (fontRenderer.FONT_HEIGHT * scaleFactor);

            if (height + scaledHeight < sizeY) {
                height += scaledHeight;
            }
        }

        height = MathHelper.clamp_int(height, 0, sizeY);

        int yCount = 0;
        for (int i = 0; i < info.length; i++) {
            glPushMatrix();
            int scaledHeight = (int) (fontRenderer.FONT_HEIGHT * scaleFactor);

            if (yCount + scaledHeight < sizeY) {
                int width = fontRenderer.getStringWidth(info[i]);
                glTranslated(leftMargin + sizeX / 2, 50 + yCount + scaledHeight / 2 - height / 2, 0);
                glScaled(scaleFactor, scaleFactor, 0);
                fontRenderer.drawString(info[i], -width / 2, -fontRenderer.FONT_HEIGHT / 2, color.getColor());

            } else {
                glPopMatrix();
                break;
            }

            glPopMatrix();
            yCount += scaledHeight;
        }
        glPopMatrix();
    }

    public static void drawScreenInfoWithLocalAutoSize(String[] info, Color color, ForgeDirection side, int leftMargin, int rightMargin, float maxScaleFactor) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPushMatrix();
        glTranslated(0, 0, -0.03);
        glScaled(0.01, 0.01, 0.01);
        int height = 0;
        int maxWidth = 0;

        int sizeX = 100 - leftMargin - rightMargin;
        int sizeY = 80;

        for (int i = 0; i < info.length; i++) {
            float scaleFactor = 1;
            int width = fontRenderer.getStringWidth(info[i]);
            if (width > 0) {
                scaleFactor = MathHelper.clamp_float((float) sizeX / (float) width, 0.02f, maxScaleFactor);
            }
            int scaledHeight = (int) (fontRenderer.FONT_HEIGHT * scaleFactor);

            if (maxWidth < fontRenderer.getStringWidth(info[i])) {
                maxWidth = fontRenderer.getStringWidth(info[i]);
            }

            if (height + scaledHeight < sizeY) {
                height += scaledHeight;
            }
        }

        height = MathHelper.clamp_int(height, 0, sizeY);

        int yCount = 0;
        for (int i = 0; i < info.length; i++) {

            glPushMatrix();
            float scaleFactor = 1;
            int width = fontRenderer.getStringWidth(info[i]);
            if (width > 0) {
                scaleFactor = MathHelper.clamp_float((float) sizeX / (float) width, 0.02f, maxScaleFactor);
            }
            int scaledHeight = (int) (fontRenderer.FONT_HEIGHT * scaleFactor);

            if (yCount + scaledHeight < sizeY) {
                glTranslated(leftMargin + sizeX / 2, 50 + yCount + scaledHeight / 2 - height / 2, 0);
                glScaled(scaleFactor, scaleFactor, 0);
                fontRenderer.drawString(info[i], -width / 2, -fontRenderer.FONT_HEIGHT / 2, color.getColor());

            } else {
                glPopMatrix();
                break;
            }

            glPopMatrix();
            yCount += scaledHeight;
        }
        glPopMatrix();
    }

    public static void endDrawinngBlockScreen() {
        glPopMatrix();
        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
    }

    public static void disableLightmap() {
        lastLightMapX = OpenGlHelper.lastBrightnessX;
        lastLightMapY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
    }

    public static void enableLightmap() {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastLightMapX, lastLightMapY);
    }

    public static void DrawMultilineInfo(List infos, int x, int y, int maxLines, int maxLineWidth, int color) {
        try {
            int linesCounter = 0;
            String infoText = StringUtils.join(infos, "\n");
            fontRenderer.drawSplitString(infoText, x, y + linesCounter * 10, maxLineWidth, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void DrawMultilineInfoCentered(List infos, int x, int y, int maxLines, int maxLineWidth, int color) {
        try {
            for (int i = 0; i < Math.min(maxLines, infos.size()); i++) {
                String info = infos.get(i).toString();
                info = info.substring(0, Math.min(maxLineWidth, info.length()));
                int width = fontRenderer.getStringWidth(info);
                fontRenderer.drawStringWithShadow(info, x - (width / 2), y + i * 10, color);
            }
        } catch (Exception e) {

        }
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    }

    //From CodeChickenLib
    public static void translateToWorldCoords(Entity entity, float frame) {
        double interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
        double interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
        double interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;

        GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ);
    }

    public static void rotateTo(Entity viewer) {
        glRotated(viewer.rotationYaw, 0, -1, 0);
        glRotated(viewer.rotationPitch, 1, 0, 0);
    }

    public static void tessalateParticle(EntityLivingBase viewer, IIcon particleIcon, double scale, Vec3 position, Color color) {
        tessalateParticle(viewer, particleIcon, scale, position, color.getFloatR(), color.getFloatG(), color.getFloatB(), color.getFloatA());
    }

    public static void tessalateParticle(EntityLivingBase viewer, IIcon particleIcon, double scale, Vec3 position, float r, float g, float b, float a) {
        float f1 = MathHelper.cos(viewer.rotationYaw * 0.017453292F);
        float f2 = MathHelper.sin(viewer.rotationYaw * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(viewer.rotationPitch * 0.017453292F);
        float f4 = f1 * MathHelper.sin(viewer.rotationPitch * 0.017453292F);
        float f5 = MathHelper.cos(viewer.rotationPitch * 0.017453292F);

        float uMin = particleIcon.getMinU();
        float uMax = particleIcon.getMaxU();
        float vMin = particleIcon.getMinV();
        float vMax = particleIcon.getMaxV();

        float x = (float) position.xCoord;
        float y = (float) position.yCoord;
        float z = (float) position.zCoord;
        Tessellator.instance.setColorRGBA_F(r, g, b, a);
        Tessellator.instance.addVertexWithUV(x - f1 * scale - f3 * scale, y - f5 * scale, z - f2 * scale - f4 * scale, (double) uMax, (double) vMax);
        Tessellator.instance.addVertexWithUV(x - f1 * scale + f3 * scale, y + f5 * scale, z - f2 * scale + f4 * scale, (double) uMax, (double) vMin);
        Tessellator.instance.addVertexWithUV(x + f1 * scale + f3 * scale, y + f5 * scale, z + f2 * scale + f4 * scale, (double) uMin, (double) vMin);
        Tessellator.instance.addVertexWithUV(x + f1 * scale - f3 * scale, y - f5 * scale, z + f2 * scale - f4 * scale, (double) uMin, (double) vMax);
    }

    public static void enable3DRender() {
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public static void enable2DRender() {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void drawSizedTexturedModalRect(int x, int y, int u, int v, int width, int height, int widthU, int heightV, float texW, float texH, float zLevel) {
        Tessellator.instance.startDrawingQuads();
        tessalateSizedModelRect(x, y, u, v, width, height, widthU, heightV, texW, texH, zLevel);
        Tessellator.instance.draw();
    }

    public static void tessalateSizedModelRect(int x, int y, int u, int v, int width, int height, int widthU, int heightV, float texW, float texH, float zLevel) {
        float texU = 1 / texW;
        float texV = 1 / texH;
        Tessellator.instance.addVertexWithUV(x, y + height, zLevel, u * texU, (v + heightV) * texV);
        Tessellator.instance.addVertexWithUV(x + width, y + height, zLevel, (u + widthU) * texU, (v + heightV) * texV);
        Tessellator.instance.addVertexWithUV(x + width, y, zLevel, (u + widthU) * texU, v * texV);
        Tessellator.instance.addVertexWithUV(x, y, zLevel, u * texU, v * texV);
    }

    public static void drawString(String string, int x, int y, Color color, float multiply) {
        drawString(Minecraft.getMinecraft().fontRenderer, string, x, y, color, multiply);
    }

    public static void drawString(FontRenderer fontRenderer, String string, int x, int y, Color color, float multiply) {
        fontRenderer.drawString(string, x, y, color.multiplyWithoutAlpha(multiply).getColor());
    }

    public static void beginStencil() {
        glEnable(GL_STENCIL_TEST);
        glColorMask(false, false, false, false);
        glDepthMask(false);
        glStencilFunc(GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0xFF); // Write to stencil buffer
        glClear(GL_STENCIL_BUFFER_BIT);
    }

    public static void beginDrawingDepthMask() {
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearDepth(1f);
        GL11.glDepthFunc(GL11.GL_LESS);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glColorMask(false, false, false, false);
        glDisable(GL_TEXTURE_2D);
    }

    public static void beginDepthMasking() {
        glEnable(GL_TEXTURE_2D);
        glDepthMask(false);
        glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_GREATER);
    }

    public static void endStencil() {
        glStencilFunc(GL_EQUAL, 1, 0xFF); // Pass test if stencil value is 1
        glStencilMask(0x00); // Don't write anything to stencil buffer
        glDepthMask(true); // Write to depth buffer
        glColorMask(true, true, true, true);
        glDisable(GL_STENCIL_TEST);
    }

    public static void endDepthMask() {
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void drawSizeableBackground(int left, int top, int width, int height, int texW, int texH, ResourceLocation texture, float zLevel, int chunkSize) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        //topLeft
        drawSizedTexturedModalRect(left, top, 0, 0, chunkSize, chunkSize, chunkSize, chunkSize, 255, 255, zLevel);
        //top middle
        drawSizedTexturedModalRect(left + chunkSize, top, chunkSize, 0, width - chunkSize * 2, chunkSize, texW - chunkSize * 2, chunkSize, 255, 255, zLevel);
        //top right
        drawSizedTexturedModalRect(left + width - chunkSize, top, texW - chunkSize, 0, chunkSize, chunkSize, chunkSize, chunkSize, 255, 255, zLevel);
        //left middle
        drawSizedTexturedModalRect(left, top + chunkSize, 0, chunkSize, chunkSize, height - chunkSize * 2, chunkSize, texH - chunkSize * 2, 255, 255, zLevel);
        //right middle
        drawSizedTexturedModalRect(left + width - chunkSize, top + chunkSize, texW - chunkSize, chunkSize, chunkSize, height - chunkSize * 2, chunkSize, texH - chunkSize * 2, 255, 255, zLevel);
        //bottom left
        drawSizedTexturedModalRect(left, top + height - chunkSize, 0, texH - chunkSize, chunkSize, chunkSize, chunkSize, chunkSize, 255, 255, zLevel);
        //bottom right
        drawSizedTexturedModalRect(left + width - chunkSize, top + height - chunkSize, texW - chunkSize, texH - chunkSize, chunkSize, chunkSize, chunkSize, chunkSize, 255, 255, zLevel);
        //bottom middle
        drawSizedTexturedModalRect(left + chunkSize, top + height - chunkSize, chunkSize, texH - chunkSize, width - chunkSize * 2, chunkSize, texW - chunkSize * 2, chunkSize, 255, 255, zLevel);
        //midddle
        drawSizedTexturedModalRect(left + chunkSize, top + chunkSize, chunkSize, chunkSize, width - chunkSize * 2, height - chunkSize * 2, texW - chunkSize * 2, texH - chunkSize * 2, 255, 255, zLevel);
    }

    public static void drawSizeableVertical(int left, int top, int u, int v, int width, int height, int texW, int texH, ResourceLocation texture, float zLevel, int chunkSize) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        //top
        drawSizedTexturedModalRect(left, top, u + 0, v + 0, width, v + chunkSize, width, chunkSize, texW, texH, zLevel);
        //middle
        drawSizedTexturedModalRect(left, top + chunkSize, u + 0, chunkSize, width, height - chunkSize * 2, width, texH - chunkSize * 2, texW, texH, zLevel);
        //bottom
        drawSizedTexturedModalRect(left, top + height - chunkSize, u + 0, v + texH - chunkSize, width, chunkSize, width, chunkSize, texW, texH, zLevel);
    }

    public static void drawSizeableHorizontal(int left, int top, int u, int v, int width, int height, int texW, int texH, ResourceLocation texture, float zLevel, int chunkSize) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        //left
        drawSizedTexturedModalRect(left, top, u, v, chunkSize, height, chunkSize, height, texW, texH, zLevel);
        //middle
        drawSizedTexturedModalRect(left + chunkSize, top, u + chunkSize, v, width - chunkSize * 2, height, texW - chunkSize * 2, height, texW, texH, zLevel);
        //right
        drawSizedTexturedModalRect(left + width - chunkSize, top, u + texW - chunkSize, v, chunkSize, height, chunkSize, height, texW, texH, zLevel);
    }

    public static void drawShip(double x, double y, double z, double size) {
        Tessellator.instance.startDrawing(GL_TRIANGLES);
        Tessellator.instance.addVertex(x - size, y, z);
        Tessellator.instance.addVertex(x + size, y, z - size);
        Tessellator.instance.addVertex(x + size, y, z + size);


        Tessellator.instance.addVertex(x - size, y, z);
        Tessellator.instance.addVertex(x + size, y, z + size);
        Tessellator.instance.addVertex(x + size, y + size, z);

        Tessellator.instance.addVertex(x - size, y, z);
        Tessellator.instance.addVertex(x + size, y + size, z);
        Tessellator.instance.addVertex(x + size, y, z - size);

        Tessellator.instance.addVertex(x + size, y, z - size);
        Tessellator.instance.addVertex(x + size, y + size, z);
        Tessellator.instance.addVertex(x + size, y, z + size);
        Tessellator.instance.draw();
    }

    public static void rotateTowards(Vec3 from, Vec3 to, Vec3 up) {
        double dot = from.dotProduct(to);
        if (Math.abs(dot - (-1.0)) < Double.MIN_VALUE) {
            glRotated(180, up.xCoord, up.yCoord, up.zCoord);
        }
        if (Math.abs(dot - (1.0)) < Double.MIN_VALUE) {
            return;
        }

        double rotAngle = Math.acos(dot);
        Vec3 rotAxis = from.crossProduct(to).normalize();
        glRotated(rotAngle * (180d / Math.PI), rotAxis.xCoord, rotAxis.yCoord, rotAxis.zCoord);
    }

    public static void renderIcon(double x, double y, double z, IIcon icon, int width, int height) {
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x, y + (double) height, z, (double) icon.getMinU(), (double) icon.getMaxV());
        var9.addVertexWithUV(x + (double) width, y + (double) height, z, (double) icon.getMaxU(), (double) icon.getMaxV());
        var9.addVertexWithUV(x + (double) width, y, z, (double) icon.getMaxU(), (double) icon.getMinV());
        var9.addVertexWithUV(x, y, z, (double) icon.getMinU(), (double) icon.getMinV());
        var9.draw();
    }

    public static final void setBlockTextureSheet() {
        new ResourceLocation("textures/atlas/blocks.png");
    }
}
