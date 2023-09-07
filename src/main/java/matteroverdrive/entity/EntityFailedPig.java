package matteroverdrive.entity;

import matteroverdrive.Reference;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.world.World;

public class EntityFailedPig extends EntityPig {
    public EntityFailedPig(World world) {
        super(world);
    }

    @Override
    protected String getLivingSound() {
        return Reference.MOD_ID + ":failed_animal_idle_pig";
    }

    protected String getHurtSound() {
        return Reference.MOD_ID + ":failed_animal_idle_pig";
    }

    @Override
    protected String getDeathSound() {
        return Reference.MOD_ID + ":failed_animal_die_" + rand.nextInt(2);
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    public EntityPig createChild(EntityAgeable entity) {
        return new EntityFailedPig(worldObj);
    }
}
