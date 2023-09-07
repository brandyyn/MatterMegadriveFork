package matteroverdrive.client.render.tileentity;

import matteroverdrive.Reference;
import matteroverdrive.tile.TileEntityMachinePatternStorage;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class TileEntityRendererPatterStorage extends TileEntitySpecialRenderer {
    private IModelCustom model;
    private ResourceLocation texture;
    private ResourceLocation ventTexture;
    EntityItem itemEntity;

    public TileEntityRendererPatterStorage() {
        texture = new ResourceLocation(Reference.PATH_BLOCKS + "pattern_storage.png");
        ventTexture = new ResourceLocation(Reference.PATH_BLOCKS + "vent.png");
        model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.MODEL_PATTERN_STORAGE));
    }

    @Override
    public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float ticks) {
        TileEntityMachinePatternStorage patternStorage = (TileEntityMachinePatternStorage) entity;
        if (patternStorage != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5f, y + 0.5f, z + 0.5f);
            RenderUtils.rotateFromBlock(patternStorage.getWorldObj(), patternStorage.xCoord, patternStorage.yCoord, patternStorage.zCoord);
            bindTexture(texture);

            for (int i = 0; i < patternStorage.pattern_storage_slots.length; i++) {
                ItemStack drive = patternStorage.getStackInSlot(patternStorage.pattern_storage_slots[i]);
                if (drive != null) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(i >= 3 ? -0.3f : 0.3f, 0.1f - 0.2f * (i % 3), -0.2f);
                    model.renderPart("drive");
                    GL11.glPopMatrix();
                }
            }
            GL11.glPopMatrix();
        }
    }
}
