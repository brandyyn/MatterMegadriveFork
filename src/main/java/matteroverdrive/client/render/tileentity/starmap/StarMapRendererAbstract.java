package matteroverdrive.client.render.tileentity.starmap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.Reference;
import matteroverdrive.api.renderer.ISpaceBodyHoloRenderer;
import matteroverdrive.data.IconHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.Sphere;

import java.util.Random;

@SideOnly(Side.CLIENT)
public abstract class StarMapRendererAbstract implements ISpaceBodyHoloRenderer {
    protected IIcon star_icon = new IconHolder(0, 0, 32f / 128f, 32f / 128f, 32, 32);
    protected IIcon selectedIcon = new IconHolder(32f / 128f, 0, 64f / 128f, 32f / 128f, 32, 32);
    protected IIcon currentIcon = new IconHolder(64f / 128f, 0, 96f / 128f, 32f / 128f, 32, 32);
    protected Sphere sphere;
    protected Disk disk;
    protected Random random;
    protected IModelCustom sphere_model;
    protected FontRenderer fontRenderer;

    public StarMapRendererAbstract() {
        sphere = new Sphere();
        disk = new Disk();
        random = new Random();
        sphere_model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.MODEL_SPHERE));
        fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }
}
