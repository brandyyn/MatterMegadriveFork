package matteroverdrive.client.render.entity;

import matteroverdrive.Reference;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;

public class EntityRendererFailedChicken extends RenderChicken {
    private static final ResourceLocation chickenTextures = new ResourceLocation(Reference.PATH_ENTITIES + "failed_chicken.png");

    public EntityRendererFailedChicken(ModelBase model, float f) {
        super(model, f);
    }

    protected ResourceLocation getEntityTexture(EntityChicken entity) {
        return chickenTextures;
    }
}
