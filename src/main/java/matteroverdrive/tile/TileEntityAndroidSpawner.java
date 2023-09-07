package matteroverdrive.tile;

import cpw.mods.fml.relauncher.Side;
import matteroverdrive.Reference;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.api.weapon.IWeaponColor;
import matteroverdrive.data.BlockPos;
import matteroverdrive.data.Inventory;
import matteroverdrive.data.inventory.ModuleSlot;
import matteroverdrive.data.inventory.TeleportFlashDriveSlot;
import matteroverdrive.entity.monster.EntityMeleeRogueAndroidMob;
import matteroverdrive.entity.monster.EntityRangedRogueAndroidMob;
import matteroverdrive.entity.monster.EntityRogueAndroidMob;
import matteroverdrive.items.TransportFlashDrive;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.machines.configs.ConfigPropertyInteger;
import matteroverdrive.machines.configs.ConfigPropertyString;
import matteroverdrive.util.WeaponHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.*;

public class TileEntityAndroidSpawner extends MOTileEntityMachine {
    public int FLASH_DRIVE_SLOT_START;
    public static final int FLASH_DRIVE_COUNT = 6;
    public int COLOR_MODULE_SLOT;
    private Set<EntityRogueAndroidMob> spawnedAndroids;

    public TileEntityAndroidSpawner() {
        super(0);
        spawnedAndroids = new HashSet<>();
        playerSlotsMain = true;
        playerSlotsHotbar = true;
    }

    protected void RegisterSlots(Inventory inventory) {
        COLOR_MODULE_SLOT = inventory.AddSlot(new ModuleSlot(true, Reference.MODULE_COLOR, null));
        TeleportFlashDriveSlot slot = new TeleportFlashDriveSlot(false);
        slot.setKeepOnDismante(true);
        FLASH_DRIVE_SLOT_START = inventory.AddSlot(slot);

        for (int i = 0; i < FLASH_DRIVE_COUNT - 1; i++) {
            slot = new TeleportFlashDriveSlot(false);
            slot.setKeepOnDismante(true);
            inventory.AddSlot(slot);
        }
        super.RegisterSlots(inventory);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!worldObj.isRemote) {
            if (isActive()) {
                if (getSpawnDelay() == 0 || worldObj.getTotalWorldTime() % getSpawnDelay() == 0) {
                    for (int i = spawnedAndroids.size(); i < getMaxSpawnCount(); ++i) {
                        EntityRogueAndroidMob entity;

                        if (random.nextInt(10) < 3) {
                            entity = new EntityMeleeRogueAndroidMob(worldObj);
                        } else {
                            entity = new EntityRangedRogueAndroidMob(worldObj);
                        }


                        double spawnRange = getSpawnRange();

                        double x = (double) xCoord + MathHelper.clamp_double(worldObj.rand.nextGaussian(), 0, 1) * spawnRange;
                        double y = (double) (yCoord + worldObj.rand.nextInt(3) - 1);
                        double z = (double) zCoord + MathHelper.clamp_double(worldObj.rand.nextGaussian(), 0, 1) * spawnRange;
                        int topY = worldObj.getHeightValue((int) x, (int) z);
                        topY = Math.min(topY, yCoord + 3);
                        entity.setLocationAndAngles(x, topY, z, worldObj.rand.nextFloat() * 360.0F, 0.0F);

                        if (entity.getCanSpawnHere(true, true, true)) {
                            entity.onSpawnWithEgg(null);
                            entity.setSpawnerPosition(new BlockPos(this));
                            entity.func_110163_bv();
                            addSpawnedAndroid(entity);
                            worldObj.playAuxSFX(2004, xCoord, yCoord, zCoord, 0);
                            ScorePlayerTeam team = getTeam();
                            if (team != null) {
                                entity.setTeam(team);
                                if (inventory.getStackInSlot(COLOR_MODULE_SLOT) != null && inventory.getStackInSlot(COLOR_MODULE_SLOT).getItem() instanceof IWeaponColor) {
                                    entity.setVisorColor(((IWeaponColor) inventory.getStackInSlot(COLOR_MODULE_SLOT).getItem()).getColor(inventory.getStackInSlot(COLOR_MODULE_SLOT), null));
                                    if (entity.getHeldItem() != null) {
                                        WeaponHelper.setModuleAtSlot(Reference.MODULE_COLOR, entity.getHeldItem(), inventory.getStackInSlot(COLOR_MODULE_SLOT));
                                    }
                                }
                            }
                            this.spawnEntity(entity);
                            entity.spawnExplosionParticle();
                            forceSync();
                        }
                    }
                }
            }
        }
    }

    public ScorePlayerTeam getTeam() {
        String teamName = getTeamName();
        if (teamName != null && !teamName.isEmpty()) {
            return worldObj.getScoreboard().getTeam(teamName);
        }
        return null;
    }

    public boolean isTeamValid() {
        String teamName = getTeamName();
        if (teamName != null && !teamName.isEmpty()) {
            return worldObj.getScoreboard().getTeam(teamName) != null;
        }
        return true;
    }

    public void assignPath(EntityRogueAndroidMob androidMob) {
        List<Vec3> paths = new ArrayList<>();
        for (int i = FLASH_DRIVE_SLOT_START; i < FLASH_DRIVE_COUNT; i++) {
            ItemStack flashDrive = inventory.getSlot(i).getItem();
            if (flashDrive != null && flashDrive.getItem() instanceof TransportFlashDrive) {
                BlockPos position = ((TransportFlashDrive) flashDrive.getItem()).getTarget(flashDrive);
                if (position != null)
                    paths.add(Vec3.createVectorHelper(position.x, position.y, position.z));
            }
        }

        if (paths.size() <= 0) {
            androidMob.setPath(new Vec3[]{Vec3.createVectorHelper(xCoord, yCoord, zCoord)}, getSpawnRange());
        } else {
            androidMob.setPath(paths.toArray(new Vec3[]{}), getSpawnRange());
        }
    }

    public int getMaxSpawnCount() {
        return configs.getInteger("max_spawn_amount", 6);
    }

    public int getSpawnRange() {
        return configs.getInteger("spawn_range", 4);
    }

    public String getTeamName() {
        return configs.getString("team", null);
    }

    public int getSpawnDelay() {
        return configs.getInteger("spawn_delay", 300);
    }

    public int getSpawnedCount() {
        return spawnedAndroids.size();
    }

    @Override
    protected void registerComponents() {
        super.registerComponents();
        configs.addProperty(new ConfigPropertyInteger("max_spawn_amount", "gui.config.spawn_amount", 0, 32, 6));
        configs.addProperty(new ConfigPropertyInteger("spawn_range", "gui.config.spawn_range", 0, 32, 4));
        configs.addProperty(new ConfigPropertyInteger("spawn_delay", "gui.config.spawn_delay", 0, 100000, 300));
        configs.addProperty(new ConfigPropertyString("team", "gui.config.team", ""));
    }

    public EntityRogueAndroidMob spawnEntity(EntityRogueAndroidMob entity) {
        worldObj.spawnEntityInWorld(entity);
        return entity;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        super.readCustomNBT(nbt, categories);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        super.writeCustomNBT(nbt, categories, toDisk);
    }

    @Override
    public String getSound() {
        return null;
    }

    @Override
    public boolean hasSound() {
        return false;
    }

    @Override
    public boolean getServerActive() {
        return getRedstoneActive() && isTeamValid() && spawnedAndroids.size() <= getMaxSpawnCount();
    }

    @Override
    public float soundVolume() {
        return 0;
    }

    @Override
    protected void onActiveChange() {

    }

    @Override
    public boolean isAffectedByUpgrade(UpgradeTypes type) {
        return false;
    }

    @Override
    protected void onAwake(Side side) {
        if (side == Side.SERVER) {
            for (Entity entity : (List<Entity>) worldObj.loadedEntityList) {
                if (entity instanceof EntityRogueAndroidMob) {
                    if (((EntityRogueAndroidMob) entity).wasSpawnedFrom(this)) {
                        addSpawnedAndroid((EntityRogueAndroidMob) entity);
                        assignPath((EntityRogueAndroidMob) entity);
                    }
                }
            }
        }
    }

    @Override
    public void onAdded(World world, int x, int y, int z) {

    }

    @Override
    public void onPlaced(World world, EntityLivingBase entityLiving) {

    }

    @Override
    public void onDestroyed() {
        removeAllAndroids();
    }

    public void removeAllAndroids() {
        for (EntityRogueAndroidMob androidMob : spawnedAndroids) {
            androidMob.isDead = true;
        }
        spawnedAndroids.clear();
    }

    public void addSpawnedAndroid(EntityRogueAndroidMob androidMob) {
        if (!spawnedAndroids.contains(androidMob)) {
            spawnedAndroids.add(androidMob);
            assignPath(androidMob);
        }
    }

    public void removeAndroid(EntityRogueAndroidMob androidMob) {
        if (spawnedAndroids.remove(androidMob)) {

        }
    }
}
