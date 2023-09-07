package matteroverdrive.client.render.tileentity;

import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.machines.fusionReactorController.TileEntityMachineFusionReactorController;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import static org.lwjgl.opengl.GL11.*;

public class TileEntityRendererFusionReactorController extends TileEntitySpecialRenderer {
    public TileEntityRendererFusionReactorController() {

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float ticks) {
        TileEntityMachineFusionReactorController controller = (TileEntityMachineFusionReactorController) tileEntity;
        if (!controller.isValidStructure()) {
            int back = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

            glPushMatrix();
            glTranslated(x, y, z);

            for (int i = 0; i < TileEntityMachineFusionReactorController.positionsCount; i++) {
                Vec3 pos = controller.getPosition(i, back);

                glPushMatrix();
                glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE);
                bindTexture(TileEntityRendererPatternMonitor.screenTextureBack);
                glTranslated(0.1, 0.1, 0.1);
                RenderUtils.drawCube(0.8, 0.8, 0.8, Reference.COLOR_HOLO);
                glDisable(GL_BLEND);
                glPopMatrix();

            }
            glPopMatrix();
        }

        renderInfo(x, y, z, controller);
    }

    private void renderInfo(double x, double y, double z, TileEntityMachineFusionReactorController controller) {
        int meta = controller.getWorldObj().getBlockMetadata(controller.xCoord, controller.yCoord, controller.zCoord);
        ForgeDirection side = ForgeDirection.getOrientation(meta);

        Color color = Reference.COLOR_HOLO;
        if (!controller.isValidStructure())
            color = Reference.COLOR_HOLO_RED;

        RenderUtils.beginDrawinngBlockScreen(x, y, z, side, color, controller);

        String[] info = controller.getMonitorInfo().split("\n");

        RenderUtils.drawScreenInfoWithGlobalAutoSize(info, color, side, 10, 10, 4);

        RenderUtils.endDrawinngBlockScreen();

    }

    private FontRenderer fontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
