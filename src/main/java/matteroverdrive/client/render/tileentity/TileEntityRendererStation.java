package matteroverdrive.client.render.tileentity;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.util.IConfigSubscriber;
import matteroverdrive.util.MOLog;
import matteroverdrive.util.MOStringHelper;
import matteroverdrive.util.RenderUtils;
import matteroverdrive.util.math.MOMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GLContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public abstract class TileEntityRendererStation<T extends MOTileEntityMachine> extends TileEntitySpecialRenderer implements IConfigSubscriber {
    public static ResourceLocation glowTexture = new ResourceLocation(Reference.PATH_FX + "hologram_beam.png");
    ResourceLocation holo_shader_vert_loc = new ResourceLocation(Reference.PATH_SHADERS + "holo_shader.vert");
    ResourceLocation holo_shader_frag_loc = new ResourceLocation(Reference.PATH_SHADERS + "holo_shader.frag");
    String holo_shader_vert;
    String holo_shader_frag;
    protected int shaderProgram;
    protected boolean validShader = true;
    private boolean enableHoloShader = true;
    int vertexShader;
    int fragmentShader;
    Random fliker;

    protected Color holoColor;
    protected Color red_holoColor;

    public TileEntityRendererStation() {
        holoColor = Reference.COLOR_HOLO.multiplyWithoutAlpha(0.25f);
        red_holoColor = Reference.COLOR_HOLO_RED.multiplyWithoutAlpha(0.25f);
        fliker = new Random();

        if (GLContext.getCapabilities().OpenGL20) {
            loadShader();
        } else {
            validShader = false;
            MOLog.warn("Your machine does not support OpenGL 2.0. The holographic shader will be disabled.");
        }
    }

    private void loadShader() {
        shaderProgram = glCreateProgram();
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);


        try {
            InputStream descriptionStream = Minecraft.getMinecraft().getResourceManager().getResource(holo_shader_vert_loc).getInputStream();
            holo_shader_vert = IOUtils.toString(descriptionStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream descriptionStream = Minecraft.getMinecraft().getResourceManager().getResource(holo_shader_frag_loc).getInputStream();
            holo_shader_frag = IOUtils.toString(descriptionStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            glShaderSource(vertexShader, holo_shader_vert);
            glCompileShader(vertexShader);

            glShaderSource(fragmentShader, holo_shader_frag);
            glCompileShader(fragmentShader);

            glAttachShader(shaderProgram, vertexShader);
            glAttachShader(shaderProgram, fragmentShader);

            glLinkProgram(shaderProgram);
            if (glGetProgrami(vertexShader, GL_LINK_STATUS) == GL_FALSE) {
                System.out.println("Could not link shader");
                System.out.println(glGetProgramInfoLog(vertexShader, glGetProgrami(vertexShader, GL_INFO_LOG_LENGTH)));
                validShader = false;
            }

            glValidateProgram(shaderProgram);
            if (glGetProgrami(vertexShader, GL_VALIDATE_STATUS) == GL_FALSE) {
                System.out.println("Could not validate shader");
                System.out.println(glGetProgramInfoLog(vertexShader, glGetProgrami(vertexShader, GL_INFO_LOG_LENGTH)));
                validShader = false;
            }

            if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
                System.out.println("Could not compile shader");
                validShader = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            validShader = false;
        }
    }

    private void drawHoloLights(TileEntity entity, World world, double x, double y, double z, double t) {
        glEnable(GL_BLEND);
        glDisable(GL_LIGHTING);
        glBlendFunc(GL_ONE, GL_ONE);
        glDepthMask(false);
        RenderUtils.disableLightmap();
        glDisable(GL_CULL_FACE);

        Minecraft.getMinecraft().renderEngine.bindTexture(glowTexture);

        double height = 9f * (1f / 16f);
        double hologramHeight = getLightHeight();
        double topSize = getLightsSize() - 1;

        glPushMatrix();
        glTranslated(x, y + height, z);
        Tessellator.instance.startDrawingQuads();

        Tessellator.instance.setColorRGBA_F(getHoloColor(entity).getFloatR(), getHoloColor(entity).getFloatG(), getHoloColor(entity).getFloatB(), 1);
        Tessellator.instance.addVertexWithUV(0, 0, 0, 1, 1);
        Tessellator.instance.addVertexWithUV(-topSize, hologramHeight, -topSize, 1, 0);
        Tessellator.instance.addVertexWithUV(1 + topSize, hologramHeight, -topSize, 0, 0);
        Tessellator.instance.addVertexWithUV(1, 0, 0, 0, 1);

        Tessellator.instance.addVertexWithUV(1, 0, 0, 1, 1);
        Tessellator.instance.addVertexWithUV(1 + topSize, hologramHeight, -topSize, 1, 0);
        Tessellator.instance.addVertexWithUV(1 + topSize, hologramHeight, 1 + topSize, 0, 0);
        Tessellator.instance.addVertexWithUV(1, 0, 1, 0, 1);

        Tessellator.instance.addVertexWithUV(1, 0, 1, 1, 1);
        Tessellator.instance.addVertexWithUV(1 + topSize, hologramHeight, 1 + topSize, 1, 0);
        Tessellator.instance.addVertexWithUV(-topSize, hologramHeight, 1 + topSize, 0, 0);
        Tessellator.instance.addVertexWithUV(0, 0, 1, 0, 1);

        Tessellator.instance.addVertexWithUV(0, 0, 1, 1, 1);
        Tessellator.instance.addVertexWithUV(-topSize, hologramHeight, 1 + topSize, 1, 0);
        Tessellator.instance.addVertexWithUV(-topSize, hologramHeight, -topSize, 0, 0);
        Tessellator.instance.addVertexWithUV(0, 0, 0, 0, 1);

        Tessellator.instance.draw();
        glPopMatrix();

        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glEnable(GL_CULL_FACE);
        RenderUtils.enableLightmap();
    }

    protected double getLightHeight() {
        return 1;
    }

    protected double getLightsSize() {
        return 1.3;
    }

    protected Color getHoloColor(TileEntity entity) {
        if (((MOTileEntityMachine) entity).isUseableByPlayer(Minecraft.getMinecraft().thePlayer)) {
            return holoColor;
        }
        return red_holoColor;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float ticks) {
        double t = MOMathHelper.noise(tileEntity.xCoord * 0.3, tileEntity.yCoord * 0.3, tileEntity.zCoord * 0.3);

        try {
            glPushMatrix();
            glPushAttrib(GL_COLOR_BUFFER_BIT);
            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE);

            try {
                renderHologram((T) tileEntity, x, y, z, ticks, t);
            } catch (ClassCastException e) {
                MOLog.warn("Could not cast to desired station class", e);
            }

            glPopAttrib();
            glPopMatrix();

        } catch (Exception e) {
            MOLog.warn("Error while render a station", e);
        }

        if (drawHoloLights())
            drawHoloLights(tileEntity, tileEntity.getWorldObj(), x, y, z, ticks);
    }

    protected boolean drawHoloLights() {
        return true;
    }

    protected void beginHolo(T tileEntity) {
        if (validShader && enableHoloShader) {
            glUseProgram(shaderProgram);
            glUniform4f(0, getHoloColor(tileEntity).getFloatR(), getHoloColor(tileEntity).getFloatG(), getHoloColor(tileEntity).getFloatB(), 0);
        } else {
            glEnable(GL_ALPHA_TEST);
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
        }
    }

    protected void endHolo() {
        if (validShader) {
            glUseProgram(0);
        }
    }

    protected void rotate(T station, double noise) {
        glRotated((Minecraft.getMinecraft().theWorld.getWorldTime() * 0.5) + (1800 * noise), 0, -1, 0);
    }

    protected boolean isUsable(T station) {
        return (station).isUseableByPlayer(Minecraft.getMinecraft().thePlayer);
    }

    protected void renderHologram(T station, double x, double y, double z, float partialTicks, double noise) {
        if (!isUsable(station)) {
            glPushMatrix();
            glTranslated(x + 0.5, y + 0.8, z + 0.5);
            rotate(station, noise);

            glDisable(GL_CULL_FACE);
            glDisable(GL_LIGHTING);
            glScaled(0.02, 0.02, 0.02);
            glRotated(180, 1, 0, 0);

            Color color = Reference.COLOR_HOLO_RED.multiplyWithoutAlpha(0.33f);
            String info[] = MOStringHelper.translateToLocal("gui.hologram.access_denied").split(" ");
            for (int i = 0; i < info.length; i++) {
                int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(info[i]);
                glPushMatrix();
                glTranslated(-width / 2, -32, 0);
                Minecraft.getMinecraft().fontRenderer.drawString(info[i], 0, i * 10, color.getColor());
                glPopMatrix();
            }

            glPopMatrix();
        }
    }

    @Override
    public void onConfigChanged(ConfigurationHandler config) {
        enableHoloShader = config.getBool("use holo shader", ConfigurationHandler.CATEGORY_CLIENT, true, "Use the custom holo shader for holographic items");
    }
}
