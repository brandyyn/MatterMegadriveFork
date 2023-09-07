package matteroverdrive.client.render.tileentity;

import matteroverdrive.Reference;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import javax.vecmath.Vector2f;

public class TileEntityRendererMatterPipe extends TileEntityRendererPipe {
    public TileEntityRendererMatterPipe() {
        texture = new ResourceLocation(Reference.PATH_BLOCKS + "matter_pipe.png");
    }

    @Override
    protected Vector2f getCoreUV(TileEntity entity) {
        return new Vector2f(0, 0);
    }

    @Override
    protected Vector2f getSidesUV(TileEntity entity, ForgeDirection dir) {
        return new Vector2f(1, 0);
    }
}
