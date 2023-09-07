package matteroverdrive.tile;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.Reference;
import matteroverdrive.api.IScannable;
import matteroverdrive.api.events.anomaly.MOEventGravitationalAnomalyConsume;
import matteroverdrive.api.gravity.AnomalySuppressor;
import matteroverdrive.api.gravity.IGravitationalAnomaly;
import matteroverdrive.api.gravity.IGravityEntity;
import matteroverdrive.client.sound.GravitationalAnomalySound;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.fx.GravitationalAnomalyParticle;
import matteroverdrive.init.MatterOverdriveBioticStats;
import matteroverdrive.items.SpacetimeEqualizer;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.util.MOLog;
import matteroverdrive.util.MatterHelper;
import matteroverdrive.util.TimeTracker;
import matteroverdrive.util.math.MOMathHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.logging.log4j.Level;
import org.lwjgl.util.vector.Vector3f;

import java.text.DecimalFormat;
import java.util.*;

public class TileEntityGravitationalAnomaly extends MOTileEntity implements IScannable, IMOTickable, IGravitationalAnomaly {
    public static boolean FALLING_BLOCKS = true;
    public static boolean BLOCK_ENTITIES = true;
    public static boolean VANILLA_FLUIDS = true;
    public static boolean FORGE_FLUIDS = true;
    public static boolean BLOCK_DESTRUCTION = true;
    public static boolean GRAVITATION = true;
    public static final float MAX_VOLUME = 0.5f;
    public static final int BLOCK_DESTROY_DELAY = 6;
    public static final int MAX_BLOCKS_PER_HARVEST = 6;
    public static final int MAX_LIQUIDS_PER_HARVEST = 32;
    public static final double STRENGTH_MULTIPLIER = 0.00001;
    public static final double G = 6.67384;
    public static final double G2 = G * 2;
    public static final double C = 2.99792458;
    public static final double CC = C * C;

    @SideOnly(Side.CLIENT)
    private GravitationalAnomalySound sound;
    private TimeTracker blockDestroyTimer;
    private long mass;
    PriorityQueue<PositionWrapper> blocks;
    List<AnomalySuppressor> supressors;
    private float suppression;

    private Vec3 blockPos;
    private Vec3 entityPos;
    private Vec3 dir;
    private Vec3 intersectDir;

    //region Constructors
    public TileEntityGravitationalAnomaly() {
        blockDestroyTimer = new TimeTracker();
        this.mass = 2048 + Math.round(Math.random() * 8192);
        supressors = new ArrayList<>();
        blockPos = Vec3.createVectorHelper(0, 0, 0);
        entityPos = Vec3.createVectorHelper(0, 0, 0);
        dir = Vec3.createVectorHelper(0, 0, 0);
        intersectDir = Vec3.createVectorHelper(0, 0, 0);
    }

    public TileEntityGravitationalAnomaly(int mass) {
        this();
        this.mass = mass;
    }
    //endregion

    //region Updates
    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) {
            spawnParticles(worldObj);
            manageSound();
            manageClientEntityGravitation(worldObj);
        }
    }

    @Override
    public void onServerTick(TickEvent.Phase phase, World world) {
        if (worldObj == null)
            return;

        if (phase.equals(TickEvent.Phase.END)) {
            float tmpSuppression = calculateSuppression();
            if (tmpSuppression != suppression) {
                suppression = tmpSuppression;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

            manageEntityGravitation(worldObj, 0);
            manageBlockDestroy(worldObj);
        }
    }
    //endregion

    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world) {
        double radius = (float) getBlockBreakRange();
        Vector3f point = MOMathHelper.randomSpherePoint(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, Vec3.createVectorHelper(radius, radius, radius), world.rand);
        GravitationalAnomalyParticle particle = new GravitationalAnomalyParticle(world, point.x, point.y, point.z, Vec3.createVectorHelper(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f));
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void manageClientEntityGravitation(World world) {
        if (!GRAVITATION)
            return;

        double rangeSq = getMaxRange() + 1;
        rangeSq *= rangeSq;
        blockPos.xCoord = xCoord + 0.5;
        blockPos.yCoord = yCoord + 0.5;
        blockPos.zCoord = zCoord + 0.5;
        entityPos.xCoord = Minecraft.getMinecraft().thePlayer.posX;
        entityPos.yCoord = Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight() / 2;
        entityPos.zCoord = Minecraft.getMinecraft().thePlayer.posZ;

        double distanceSq = entityPos.squareDistanceTo(blockPos);
        if (distanceSq < rangeSq) {
            if ((Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(3) != null && Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(3).getItem() instanceof SpacetimeEqualizer)
                    || Minecraft.getMinecraft().thePlayer.capabilities.disableDamage
                    || AndroidPlayer.get(Minecraft.getMinecraft().thePlayer).isUnlocked(MatterOverdriveBioticStats.equalizer, 0))
                return;

            double acceleration = getAcceleration(distanceSq);
            dir.xCoord = blockPos.xCoord - entityPos.xCoord;
            dir.yCoord = blockPos.yCoord - entityPos.yCoord;
            dir.zCoord = blockPos.zCoord - entityPos.zCoord;
            normalize(dir);
            Minecraft.getMinecraft().thePlayer.addVelocity(dir.xCoord * acceleration, dir.yCoord * acceleration, dir.zCoord * acceleration);
            Minecraft.getMinecraft().thePlayer.velocityChanged = true;
        }
    }

    public void manageEntityGravitation(World world, float ticks) {
        if (!GRAVITATION)
            return;


        double range = getMaxRange() + 1;
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(xCoord - range, yCoord - range, zCoord - range, xCoord + range, yCoord + range, zCoord + range);
        List entities = world.getEntitiesWithinAABB(Entity.class, bb);
        blockPos.xCoord = xCoord + 0.5;
        blockPos.yCoord = yCoord + 0.5;
        blockPos.zCoord = zCoord + 0.5;

        for (Object entityObject : entities) {
            if (entityObject instanceof Entity) {
                Entity entity = (Entity) entityObject;
                if (entity instanceof IGravityEntity) {
                    if (!((IGravityEntity) entity).isAffectedByAnomaly(this)) {
                        continue;
                    }
                }
                entityPos.xCoord = entity.posX;
                entityPos.yCoord = entity.posY;
                entityPos.zCoord = entity.posZ;

                //pos.yCoord += entity.getEyeHeight();
                double distanceSq = entityPos.squareDistanceTo(blockPos);
                double acceleration = getAcceleration(distanceSq);
                double eventHorizon = getEventHorizon();
                dir.xCoord = blockPos.xCoord - entityPos.xCoord;
                dir.yCoord = blockPos.yCoord - entityPos.yCoord;
                dir.zCoord = blockPos.zCoord - entityPos.zCoord;
                normalize(dir);
                dir.xCoord *= acceleration;
                dir.yCoord *= acceleration;
                dir.zCoord *= acceleration;
                if (intersectsAnomaly(entityPos, dir, blockPos, eventHorizon)) {
                    consume(entity);
                }

                if (entityObject instanceof EntityLivingBase) {
                    ItemStack eq = ((EntityLivingBase) entityObject).getEquipmentInSlot(3);
                    if (eq != null && eq.getItem() instanceof SpacetimeEqualizer)
                        continue;
                }

                entity.addVelocity(dir.xCoord, dir.yCoord, dir.zCoord);
            }
        }
    }

    boolean intersectsAnomaly(Vec3 origin, Vec3 dir, Vec3 anomaly, double radius) {
        if (origin.distanceTo(anomaly) <= radius) {
            return true;
        } else {
            intersectDir.xCoord = origin.xCoord - anomaly.xCoord;
            intersectDir.yCoord = origin.yCoord - anomaly.yCoord;
            intersectDir.zCoord = origin.zCoord - anomaly.zCoord;
            double c = intersectDir.lengthVector();
            double v = intersectDir.dotProduct(dir);
            double d = radius * radius - (c * c - v * v);

            return !(d < 0);
        }
    }

    //region Sounds
    @SideOnly(Side.CLIENT)
    public void stopSounds() {
        if (sound != null) {
            sound.stopPlaying();
            FMLClientHandler.instance().getClient().getSoundHandler().stopSound(sound);
            sound = null;
        }
    }

    @SideOnly(Side.CLIENT)
    public void playSounds() {
        if (sound == null) {
            sound = new GravitationalAnomalySound(new ResourceLocation(Reference.MOD_ID + ":" + "windy"), xCoord, yCoord, zCoord, 0.2f, getMaxRange());
            FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
        } else if (!FMLClientHandler.instance().getClient().getSoundHandler().isSoundPlaying(sound)) {
            stopSounds();
            sound = new GravitationalAnomalySound(new ResourceLocation(Reference.MOD_ID + ":" + "windy"), xCoord, yCoord, zCoord, 0.2f, getMaxRange());
            FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
        }
    }

    @SideOnly(Side.CLIENT)
    public void manageSound() {
        if (sound == null) {
            playSounds();
        } else {
            sound.setVolume(Math.min(MAX_VOLUME, getBreakStrength(0, (float) getMaxRange()) * 0.1f));
            sound.setRange(getMaxRange());
        }
    }
    //endregion

    //region Super Events
    @Override
    public void onAdded(World world, int x, int y, int z) {

    }

    @Override
    public void onPlaced(World world, EntityLivingBase entityLiving) {

    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onNeighborBlockChange() {

    }

    @Override
    public void writeToDropItem(ItemStack itemStack) {

    }

    @Override
    public void readFromPlaceItem(ItemStack itemStack) {

    }

    @Override
    public void onScan(World world, double x, double y, double z, EntityPlayer player, ItemStack scanner) {

    }

    public void onChunkUnload() {
        if (worldObj.isRemote) {
            stopSounds();
        }
    }

    @Override
    protected void onAwake(Side side) {

    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeCustomNBT(syncData, MachineNBTCategory.ALL_OPTS, false);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound syncData = pkt.func_148857_g();
        if (syncData != null) {
            readCustomNBT(syncData, MachineNBTCategory.ALL_OPTS);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (worldObj.isRemote) {
            stopSounds();
        }
    }
    //endregion

    //region Events
    private boolean onEntityConsume(Entity entity, boolean pre) {
        if (entity instanceof IGravityEntity) {
            ((IGravityEntity) entity).onEntityConsumed(this);
        }
        if (pre) {
            MinecraftForge.EVENT_BUS.post(new MOEventGravitationalAnomalyConsume.Pre(entity, xCoord, yCoord, zCoord));
        } else {
            MinecraftForge.EVENT_BUS.post(new MOEventGravitationalAnomalyConsume.Post(entity, xCoord, yCoord, zCoord));
        }

        return true;
    }
    //endregion

    public void manageBlockDestroy(World world) {
        if (!BLOCK_DESTRUCTION)
            return;

        int solidCount = 0;
        int liquidCount = 0;
        int range = (int) Math.floor(getBlockBreakRange());
        double distance;
        double eventHorizon = getEventHorizon();
        int blockPosX, blockPosY, blockPosZ;
        float hardness;
        Block block;

        blocks = new PriorityQueue<>(1, new BlockComparitor(xCoord, yCoord, zCoord));

        if (blockDestroyTimer.hasDelayPassed(world, BLOCK_DESTROY_DELAY)) {
            for (int x = -range; x < range; x++) {
                for (int y = -range; y < range; y++) {
                    for (int z = -range; z < range; z++) {
                        blockPosX = xCoord + x;
                        blockPosY = yCoord + y;
                        blockPosZ = zCoord + z;
                        block = world.getBlock(blockPosX, blockPosY, blockPosZ);
                        distance = MOMathHelper.distance(blockPosX, blockPosY, blockPosZ, xCoord, yCoord, zCoord);
                        hardness = block.getBlockHardness(world, blockPosX, blockPosY, blockPosZ);
                        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
                            hardness = 1;
                        }

                        float strength = getBreakStrength((float) distance, range);
                        if (block != null && block != Blocks.air && distance <= range && hardness >= 0 && (distance < eventHorizon || hardness < strength)) {
                            blocks.add(new PositionWrapper(blockPosX, blockPosY, blockPosZ));
                        }
                    }
                }
            }
        }

        for (PositionWrapper position : blocks) {
            block = world.getBlock(position.x, position.y, position.z);

            if (!cleanFlowingLiquids(block, position.x, position.y, position.z)) {
                if (liquidCount < MAX_LIQUIDS_PER_HARVEST) {
                    if (cleanLiquids(block, position.x, position.y, position.z)) {
                        liquidCount++;
                        continue;
                    }
                }
                if (solidCount < MAX_BLOCKS_PER_HARVEST) {
                    try {
                        distance = MOMathHelper.distance(position.x, position.y, position.z, xCoord, yCoord, zCoord);
                        float strength = getBreakStrength((float) distance, range);
                        if (brakeBlock(world, position.x, position.y, position.z, strength, eventHorizon, range)) {
                            solidCount++;
                        }
                    } catch (Exception e) {
                        MOLog.log(Level.ERROR, e, "There was a problem while trying to brake block %s", block);
                    }
                }
            }
        }
    }

    //region Consume Type Handlers
    public void consume(Entity entity) {

        if (!entity.isDead && onEntityConsume(entity, true)) {

            boolean consumedFlag = false;

            if (entity instanceof EntityItem) {
                consumedFlag |= consumeEntityItem((EntityItem) entity);
            } else if (entity instanceof EntityFallingBlock) {
                consumedFlag |= consumeFallingBlock((EntityFallingBlock) entity);
            } else if (entity instanceof EntityLivingBase) {
                consumedFlag |= consumeLivingEntity((EntityLivingBase) entity, getBreakStrength((float) entity.getDistance(xCoord, yCoord, zCoord), (float) getMaxRange()));
            }

            if (consumedFlag) {
                onEntityConsume(entity, false);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    private boolean consumeEntityItem(EntityItem entityItem) {
        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack != null) {
            try {
                mass = Math.addExact(mass, (long) MatterHelper.getMatterAmountFromItem(itemStack) * (long) itemStack.stackSize);
            } catch (ArithmeticException e) {
                return false;
            }

            entityItem.setDead();
            worldObj.removeEntity(entityItem);

            //TODO: made the gravitational anomaly collapse on Antimatter
            if (entityItem.getEntityItem().getItem().equals(Items.nether_star)) {
                collapse();
            }
            return true;
        }
        return false;
    }

    private boolean consumeFallingBlock(EntityFallingBlock fallingBlock) {
        ItemStack itemStack = new ItemStack(fallingBlock.func_145805_f(), 1, fallingBlock.field_145814_a);
        if (itemStack != null) {
            try {
                mass = Math.addExact(mass, (long) MatterHelper.getMatterAmountFromItem(itemStack) * (long) itemStack.stackSize);
            } catch (ArithmeticException e) {
                return false;
            }

            fallingBlock.setDead();
            worldObj.removeEntity(fallingBlock);
            return true;
        }
        return false;
    }

    private boolean consumeLivingEntity(EntityLivingBase entity, float strength) {
        try {
            mass = Math.addExact(mass, (long) Math.min(entity.getHealth(), strength));
        } catch (ArithmeticException e) {
            return false;
        }

        if (entity.getHealth() <= strength && !(entity instanceof EntityPlayer)) {
            entity.setDead();
            worldObj.removeEntity(entity);
        }

        DamageSource damageSource = new DamageSource("blackHole");
        entity.attackEntityFrom(damageSource, strength);
        return true;
    }
    //endregion

    public boolean brakeBlock(World world, int x, int y, int z, float strength, double eventHorizon, int range) {
        Block block = world.getBlock(x, y, z);
        if (block == null)
            return true;

        float hardness = block.getBlockHardness(worldObj, x, y, z);
        double distance = MOMathHelper.distance(x, y, z, xCoord, yCoord, zCoord);
        if (distance <= range && hardness >= 0 && (distance < eventHorizon || hardness < strength)) {
            int meta = worldObj.getBlockMetadata(x, y, z);
            if (BLOCK_ENTITIES) {

                if (FALLING_BLOCKS) {
                    EntityFallingBlock fallingBlock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, block, world.getBlockMetadata(x, y, z));
                    fallingBlock.field_145812_b = 1;
                    fallingBlock.noClip = true;
                    world.spawnEntityInWorld(fallingBlock);
                } else {
                    ItemStack bStack = createStackedBlock(block, meta);
                    if (bStack != null) {
                        EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, bStack);
                        world.spawnEntityInWorld(item);
                    }
                }

                block.breakBlock(world, x, y, z, block, 0);
                worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
                world.setBlock(x, y, z, Blocks.air, 0, 10);
                return true;
            } else {
                int matter = 0;

                if (block.canSilkHarvest(worldObj, null, x, y, z, meta)) {
                    matter += MatterHelper.getMatterAmountFromItem(createStackedBlock(block, meta));
                } else {
                    for (ItemStack stack : block.getDrops(worldObj, x, y, z, meta, 0)) {
                        matter += MatterHelper.getMatterAmountFromItem(stack);
                    }
                }

                worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));

                List<EntityItem> result = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(x - 2, y - 2, z - 2, x + 3, y + 3, z + 3));
                for (EntityItem entityItem : result) {
                    consumeEntityItem(entityItem);
                }

                try {
                    mass = Math.addExact(mass, matter);
                } catch (ArithmeticException e) {
                    return false;
                }

                world.setBlock(x, y, z, Blocks.air, 0, 10);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return true;
            }
        }

        return false;
    }

    //region Helper Methods
    protected ItemStack createStackedBlock(Block block, int meta) {
        if (block != null) {
            Item item = Item.getItemFromBlock(block);
            if (item != null) {
                if (item.getHasSubtypes()) {
                    return new ItemStack(item, 1, meta);
                }
                return new ItemStack(item, 1, 0);
            }
        }
        return null;
    }

    public boolean cleanLiquids(Block block, int x, int y, int z) {
        if (block instanceof IFluidBlock && FORGE_FLUIDS) {
            if (((IFluidBlock) block).canDrain(worldObj, x, y, z)) {
                if (FALLING_BLOCKS) {
                    EntityFallingBlock fallingBlock = new EntityFallingBlock(worldObj, x + 0.5, y + 0.5, z + 0.5, block, worldObj.getBlockMetadata(x, y, z));
                    fallingBlock.field_145812_b = 1;
                    fallingBlock.noClip = true;
                    worldObj.spawnEntityInWorld(fallingBlock);
                }

                ((IFluidBlock) block).drain(worldObj, x, y, z, true);
                return true;
            }

        } else if (block instanceof BlockLiquid && VANILLA_FLUIDS) {
            if (worldObj.setBlock(x, y, z, Blocks.air, 0, 2)) {
                if (FALLING_BLOCKS) {
                    EntityFallingBlock fallingBlock = new EntityFallingBlock(worldObj, x + 0.5, y + 0.5, z + 0.5, block, worldObj.getBlockMetadata(x, y, z));
                    fallingBlock.field_145812_b = 1;
                    fallingBlock.noClip = true;
                    worldObj.spawnEntityInWorld(fallingBlock);
                }
                return true;
            }
        }

        return false;
    }

    public boolean cleanFlowingLiquids(Block block, int x, int y, int z) {
        if (VANILLA_FLUIDS) {
            if (block == Blocks.flowing_water || block == Blocks.flowing_lava) {
                return worldObj.setBlock(x, y, z, Blocks.air, 0, 2);
            }
        }
        return false;
    }

    private Vec3 normalize(Vec3 vec3) {
        double d0 = (double) net.minecraft.util.MathHelper.sqrt_double(vec3.xCoord * vec3.xCoord + vec3.yCoord * vec3.yCoord + vec3.zCoord * vec3.zCoord);
        if (d0 < 1.0E-4D) {
            vec3.xCoord = 0;
            vec3.yCoord = 0;
            vec3.zCoord = 0;
        } else {
            vec3.xCoord = vec3.xCoord / d0;
            vec3.yCoord = vec3.yCoord / d0;
            vec3.zCoord = vec3.zCoord / d0;
        }
        return vec3;
    }
    //endregion

    public void collapse() {
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        worldObj.createExplosion(null, xCoord, yCoord, zCoord, (float) getRealMassUnsuppressed() * 2, true);
    }

    @Override
    public void addInfo(World world, double x, double y, double z, List<String> infos) {
        DecimalFormat format = new DecimalFormat("#.##");
        infos.add("Mass: " + mass);
        infos.add("Pull range: " + format.format(Math.round(getMaxRange())));
        infos.add("Break range: " + format.format(getBlockBreakRange()));
        infos.add("Strength: " + format.format(getBreakStrength()));
        infos.add("Horizon size: " + format.format(getEventHorizon()));
    }

    public void suppress(AnomalySuppressor suppressor) {
        for (AnomalySuppressor s : supressors) {
            if (s.update(suppressor)) {
                return;
            }
        }

        supressors.add(suppressor);
    }

    private float calculateSuppression() {
        float suppression = 1;
        Iterator<AnomalySuppressor> iterator = supressors.iterator();
        while (iterator.hasNext()) {
            AnomalySuppressor s = iterator.next();
            boolean isLoaded = worldObj.getChunkFromBlockCoords(s.getX(), s.getZ()).isChunkLoaded;
            if (!s.isValid() && isLoaded) {
                iterator.remove();
            }
            if (isLoaded) {
                s.tick();
            }
            suppression *= s.getAmount();
        }
        return suppression;
    }

    //region NBT
    @Override
    public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        if (categories.contains(MachineNBTCategory.DATA)) {
            nbt.setLong("Mass", mass);
            nbt.setFloat("Suppression", suppression);
            if (toDisk) {
                NBTTagList suppressors = new NBTTagList();
                for (AnomalySuppressor s : this.supressors) {
                    NBTTagCompound suppressorTag = new NBTTagCompound();
                    s.writeToNBT(suppressorTag);
                    suppressors.appendTag(suppressorTag);
                }
                nbt.setTag("suppressors", suppressors);
            }
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        if (categories.contains(MachineNBTCategory.DATA)) {
            this.supressors.clear();
            mass = nbt.getLong("Mass");
            suppression = nbt.getFloat("Suppression");
            NBTTagList suppressors = nbt.getTagList("suppressors", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < supressors.size(); i++) {
                NBTTagCompound suppressorTag = suppressors.getCompoundTagAt(i);
                AnomalySuppressor s = new AnomalySuppressor(suppressorTag);
                this.supressors.add(s);
            }
        }
    }
    //endregion

    //region Getters and Setters
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Math.max(Math.pow(getMaxRange(), 3), 2048);
    }

    @Override
    public int getX() {
        return xCoord;
    }

    @Override
    public int getY() {
        return yCoord;
    }

    @Override
    public int getZ() {
        return zCoord;
    }

    public double getEventHorizon() {
        return Math.max((G2 * getRealMass()) / CC, 0.5);
    }

    public double getBlockBreakRange() {
        return getMaxRange() / 2;
    }

    public double getMaxRange() {
        return Math.sqrt(getRealMass() * (G / 0.01));
    }

    public double getAcceleration(double distanceSq) {
        return G * (getRealMass() / Math.max(distanceSq, 0.0001f));
    }

    public double getRealMass() {
        return getRealMassUnsuppressed() * suppression;
    }

    public double getRealMassUnsuppressed() {
        return Math.log1p(Math.max(mass, 0) * STRENGTH_MULTIPLIER);
    }

    public float getBreakStrength(float distance, float maxRange) {
        return ((float) getRealMass() * 4 * suppression) * getDistanceFalloff(distance, maxRange);
    }

    public float getDistanceFalloff(float distance, float maxRange) {
        return (1 - (distance / maxRange));
    }

    public float getBreakStrength() {
        return (float) getRealMass() * 4 * suppression;
    }
    //endregion

    //region Sub Classes
    public static class PositionWrapper {
        int x, y, z;

        public PositionWrapper(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class BlockComparitor implements Comparator<PositionWrapper> {
        int posX, posY, posZ;

        public BlockComparitor(int x, int y, int z) {
            posX = x;
            posY = y;
            posZ = z;
        }

        @Override
        public int compare(PositionWrapper o1, PositionWrapper o2) {
            return Double.compare(MOMathHelper.distanceSqured(o1.x, o1.y, o1.z, posX, posY, posZ), MOMathHelper.distanceSqured(o2.x, o2.y, o2.z, posX, posY, posZ));
        }
    }
    //endregion
}
