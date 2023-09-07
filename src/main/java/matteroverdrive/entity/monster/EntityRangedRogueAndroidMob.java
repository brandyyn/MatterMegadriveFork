package matteroverdrive.entity.monster;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.entity.IRangedEnergyWeaponAttackMob;
import matteroverdrive.api.weapon.WeaponShot;
import matteroverdrive.entity.ai.AndroidTargetSelector;
import matteroverdrive.entity.ai.EntityAIMoveAlongPath;
import matteroverdrive.entity.ai.EntityAIPhaserBoltAttack;
import matteroverdrive.entity.ai.EntityAIRangedRunFromMelee;
import matteroverdrive.init.MatterOverdriveItems;
import matteroverdrive.items.android.RogueAndroidParts;
import matteroverdrive.items.weapon.EnergyWeapon;
import matteroverdrive.network.packet.bi.PacketFirePlasmaShot;
import matteroverdrive.util.AndroidPartsFactory;
import matteroverdrive.util.WeaponFactory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityRangedRogueAndroidMob extends EntityRogueAndroidMob implements IRangedEnergyWeaponAttackMob {
    private EntityAIPhaserBoltAttack aiBoltAttack = new EntityAIPhaserBoltAttack(this, 1.0D, 60, 15.0F);
    private EntityAIRangedRunFromMelee aiRangedRunFromMelee = new EntityAIRangedRunFromMelee(this, 1.0D);

    public EntityRangedRogueAndroidMob(World world) {
        super(world);
        init(world);
    }

    public EntityRangedRogueAndroidMob(World world, int level, boolean legendary) {
        super(world, level, legendary);
        init(world);
    }

    protected void init(World world) {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, aiRangedRunFromMelee);
        this.tasks.addTask(3, aiBoltAttack);
        this.tasks.addTask(4, new EntityAIMoveAlongPath(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, true, true, new AndroidTargetSelector(this)));

        if (world != null && !world.isRemote) {
            this.setCombatTask();
        }
    }

    @Override
    protected void dropEquipment(boolean recentlyHit, int lootingLevel) {
        if (this.recentlyHit > 0) {
            int j = this.rand.nextInt(400) - lootingLevel;

            if (j < 5 || getIsLegendary()) {
                this.entityDropItem(getEquipmentInSlot(0).copy(), 0);
            }
        }
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(24);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
        if (!hasTeam() || recentlyHit) {
            int j;
            int k;

            j = this.rand.nextInt(2 + lootingLevel);

            for (k = 0; k < j; ++k) {
                this.dropItem(MatterOverdriveItems.energyPack, 1);
            }

            float lootingModifier = (Math.min(lootingLevel, 10) / 10f);
            if (rand.nextFloat() < (0.15f + lootingModifier) || getIsLegendary()) {
                ItemStack part = MatterOverdrive.androidPartsFactory.generateRandomDecoratedPart(new AndroidPartsFactory.AndroidPartFactoryContext(androidLevel, this, getIsLegendary()));
                if (part.getItem() instanceof RogueAndroidParts) {
                    part.setTagCompound(new NBTTagCompound());
                    part.getTagCompound().setByte("Type", (byte) 1);
                }
                this.entityDropItem(part, 0.0F);
            }
        }
    }

    @Override
    public void addRandomArmor() {
        super.addRandomArmor();
        ItemStack gun = MatterOverdrive.weaponFactory.getRandomDecoratedEnergyWeapon(new WeaponFactory.WeaponGenerationContext(androidLevel, this, getIsLegendary()));
        setCurrentItemOrArmor(0, gun);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(((EnergyWeapon) gun.getItem()).getRange(gun) - 2);
    }

    public void setCombatTask() {
        ItemStack itemstack = this.getHeldItem();

        if (itemstack != null && itemstack.getItem() instanceof EnergyWeapon) {
            this.aiBoltAttack.setMaxChaseDistance(((EnergyWeapon) itemstack.getItem()).getRange(itemstack) - 2);
            if (itemstack.getItem() == MatterOverdriveItems.ionSniper) {
                aiRangedRunFromMelee.setMinDistance(16f);
            } else if (itemstack.getItem() != MatterOverdriveItems.plasmaShotgun) {
                aiRangedRunFromMelee.setMinDistance(3f);
            }
        }
    }

    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData entityLivingData) {
        entityLivingData = super.onSpawnWithEgg(entityLivingData);
        this.addRandomArmor();
        this.enchantEquipment();
        return entityLivingData;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, Vec3 lastSeenPosition, boolean canSee) {
        ItemStack weapon = this.getHeldItem();
        if (!worldObj.isRemote) {
            if (lastSeenPosition == null) {
                lastSeenPosition = Vec3.createVectorHelper(target.posX, target.posY, target.posZ);
            }
            if (weapon.getItem() instanceof EnergyWeapon) {
                EnergyWeapon energyWeapon = (EnergyWeapon) weapon.getItem();
                //magic number from MC
                Vec3 pos = Vec3.createVectorHelper(this.posX, this.posY + getEyeHeight(), this.posZ);
                Vec3 dir = Vec3.createVectorHelper(lastSeenPosition.xCoord - this.posX, lastSeenPosition.yCoord - this.posY, lastSeenPosition.zCoord - this.posZ);
                WeaponShot shot = energyWeapon.createShot(weapon, this, true);
                float difficulty = MathHelper.clamp_float((0.6f / 3f) * worldObj.difficultySetting.getDifficultyId(), 0, 0.6f) + androidLevel * (0.4f / 3f) + (getIsLegendary() ? 0.3f : 0);
                shot.setDamage(shot.getDamage() * difficulty);
                difficulty = (3 - worldObj.difficultySetting.getDifficultyId()) * 4f;
                shot.setAccuracy(shot.getAccuracy() + difficulty);
                energyWeapon.onServerFire(weapon, this, shot, pos, dir, 0);
                energyWeapon.setHeat(weapon, 0);
                if (EntityRogueAndroid.UNLIMITED_WEAPON_ENERGY)
                    energyWeapon.rechargeFully(weapon);
                MatterOverdrive.packetPipeline.sendToAllAround(new PacketFirePlasmaShot(this.getEntityId(), pos, dir, shot), worldObj.provider.dimensionId, posX, posY, posZ, 64);

                difficulty = 1 + (3 - worldObj.difficultySetting.getDifficultyId()) * 0.5f;
                this.aiBoltAttack.setMaxRangedAttackDelay((int) (((EnergyWeapon) weapon.getItem()).getShootCooldown(weapon) * difficulty));
            }
        }
    }

    public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
        super.readEntityFromNBT(p_70037_1_);
        this.setCombatTask();
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
        super.setCurrentItemOrArmor(slot, itemStack);

        if (!this.worldObj.isRemote && slot == 0) {
            this.setCombatTask();
        }
    }

    @Override
    public ItemStack getWeapon() {
        return getHeldItem();
    }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double p_70112_1_) {
        double d1 = this.boundingBox.getAverageEdgeLength();
        d1 *= 64.0D * this.renderDistanceWeight;
        d1 += getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue();
        return p_70112_1_ < d1 * d1;
    }
}
