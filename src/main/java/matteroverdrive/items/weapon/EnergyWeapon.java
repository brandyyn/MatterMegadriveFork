package matteroverdrive.items.weapon;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.api.events.weapon.MOEventEnergyWeapon;
import matteroverdrive.api.inventory.IEnergyPack;
import matteroverdrive.api.weapon.IWeapon;
import matteroverdrive.api.weapon.IWeaponScope;
import matteroverdrive.api.weapon.WeaponShot;
import matteroverdrive.init.MatterOverdriveEnchantments;
import matteroverdrive.items.includes.MOItemEnergyContainer;
import matteroverdrive.network.packet.bi.PacketFirePlasmaShot;
import matteroverdrive.network.packet.server.PacketReloadEnergyWeapon;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.util.EntityDamageSourcePhaser;
import matteroverdrive.util.MOEnergyHelper;
import matteroverdrive.util.WeaponHelper;
import matteroverdrive.util.animation.MOEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import java.text.DecimalFormat;
import java.util.List;

public abstract class EnergyWeapon extends MOItemEnergyContainer implements IWeapon {

    public static final String CUSTOM_DAMAGE_TAG = "CustomDamage";
    public static final String CUSTOM_ACCURACY_TAG = "CustomAccuracy";
    public static final String CUSTOM_RANGE_TAG = "CustomRange";
    public static final String CUSTOM_SPEED_TAG = "CustomSpeed";
    public static final String CUSTOM_DAMAGE_MULTIPLY_TAG = "CustomDamageMultiply";
    public static final String CUSTOM_ACCURACY_MULTIPLY_TAG = "CustomAccuracyMultiply";
    public static final String CUSTOM_RANGE_MULTIPLY_TAG = "CustomRangeMultiply";
    public static final String CUSTOM_SPEED_MULTIPLY_TAG = "CustomSpeedMultiply";
    private final int defaultRange;
    private DecimalFormat damageFormater = new DecimalFormat("#.##");
    protected boolean leftClickFire;

    public EnergyWeapon(String name, int capacity, int maxReceive, int maxExtract, int defaultRange) {
        super(name, capacity, maxReceive, maxExtract);
        this.defaultRange = defaultRange;
    }

    //region Vanilla Item method overloading
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        ItemStack unpowered = new ItemStack(item);
        ItemStack powered = new ItemStack(item);
        setEnergyStored(powered, getMaxEnergyStored(powered));
        list.add(unpowered);
        list.add(powered);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack p_77661_1_) {
        return EnumAction.bow;
    }

    @Override
    public int getItemStackLimit(ItemStack item) {
        return 1;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int slot, boolean isHolding) {
        if (!world.isRemote) {
            manageCooling(itemStack);
        } else {
            if (entity instanceof EntityPlayer && entity == Minecraft.getMinecraft().thePlayer && isHolding && Minecraft.getMinecraft().currentScreen == null) {
                onShooterClientUpdate(itemStack, world, (EntityPlayer) entity, true);
            }
        }
    }
    //endregion

    //region Tooltips
    @Override
    public void addDetails(ItemStack weapon, EntityPlayer player, List infos) {
        super.addDetails(weapon, player, infos);
        String energyInfo = EnumChatFormatting.DARK_RED + "Power Use: " + MOEnergyHelper.formatEnergy(null, getEnergyUse(weapon) * 20) + "/s";
        float energyMultiply = (float) getEnergyUse(weapon) / (float) getBaseEnergyUse(weapon);
        if (energyMultiply != 1) {
            energyInfo += " (" + DecimalFormat.getPercentInstance().format(energyMultiply) + ")";
        }
        infos.add(energyInfo);
        infos.add("");

        infos.add(addStatWithMultiplyInfo("Damage", damageFormater.format(getWeaponScaledDamage(weapon, player)), getWeaponScaledDamage(weapon, player) / getWeaponBaseDamage(weapon), ""));
        infos.add(addStatWithMultiplyInfo("DPS", damageFormater.format((getWeaponScaledDamage(weapon, player) / getShootCooldown(weapon)) * 20), 1, ""));
        infos.add(addStatWithMultiplyInfo("Speed", (int) (20d / getShootCooldown(weapon) * 60), (double) getBaseShootCooldown(weapon) / (double) getShootCooldown(weapon), " s/m"));
        infos.add(addStatWithMultiplyInfo("Range", getRange(weapon), (double) getRange(weapon) / (double) defaultRange, "b"));
        infos.add(addStatWithMultiplyInfo("Accuracy", "", 1 / (modifyStatFromModules(Reference.WS_ACCURACY, weapon, 1) * getCustomFloatStat(weapon, CUSTOM_ACCURACY_MULTIPLY_TAG, 1)), ""));

        String heatInfo = EnumChatFormatting.DARK_RED + "Heat: ";
        double heatPercent = getHeat(weapon) / getMaxHeat(weapon);
        for (int i = 0; i < 32 * heatPercent; i++) {
            heatInfo += "|";
        }
        infos.add(heatInfo);
        infos.add("");
        addCustomDetails(weapon, player, infos);
        AddModuleDetails(weapon, infos);
    }

    private String addStatWithMultiplyInfo(String statName, Object value, double multiply, String units) {
        String info = String.format("%s: %s%s", statName, EnumChatFormatting.DARK_AQUA, value);
        if (!units.isEmpty()) {
            info += " " + units;
        }
        if (multiply != 1) {
            if (multiply > 1) {
                info += EnumChatFormatting.DARK_GREEN;
            } else {
                info += EnumChatFormatting.DARK_RED;
            }
            info += String.format(" (%s) %s", DecimalFormat.getPercentInstance().format(multiply), EnumChatFormatting.RESET);
        }
        return info;
    }

    private void AddModuleDetails(ItemStack weapon, List infos) {
        ItemStack module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BARREL, weapon);
        if (module != null) {
            /*infos.add(EnumChatFormatting.GRAY + "Barrel:");

            Object statsObject = ((IWeaponModule)module.getItem()).getValue(module);
            if (statsObject instanceof Map)
            {
                for (final Map.Entry<Integer, Double> entry : ((Map<Integer,Double>) statsObject).entrySet())
                {
                    if (entry.getKey() != Reference.WS_DAMAGE && entry.getKey() != Reference.WS_AMMO) {
                        infos.add("    " + MOStringHelper.weaponStatToInfo(entry.getKey(), entry.getValue()));
                    }
                }
            }

            infos.add("");*/
        }
    }
    //endregion

    //region client only

    /**
     * Used to manage the sending of weapons tick to the server.
     * This is tied to {@link #sendShootTickToServer(World, WeaponShot, Vec3, Vec3)}.
     * Used to mainly send weapon ticks without weapon shot information, aka. weapon firing.
     *
     * @param world the shooter's world.
     */
    @SideOnly(Side.CLIENT)
    protected void manageClientServerTicks(World world) {
        ClientProxy.instance().getClientWeaponHandler().sendWeaponTickToServer(world, null);
    }

    /**
     * A utility function for sending a weapon tick with a given weapon shot to the server.
     * This is part of the latency compensation system for Phaser bolts.
     *
     * @param world      the shooter's world.
     * @param weaponShot the weapon shot being fired. Can be null.
     * @param dir        the direction of the shot.
     * @param pos        the starting position of the shot.
     */
    @SideOnly(Side.CLIENT)
    protected void sendShootTickToServer(World world, WeaponShot weaponShot, Vec3 dir, Vec3 pos) {
        PacketFirePlasmaShot packetFirePlasmaShot = new PacketFirePlasmaShot(Minecraft.getMinecraft().thePlayer.getEntityId(), pos, dir, weaponShot);
        ClientProxy.instance().getClientWeaponHandler().sendWeaponTickToServer(world, packetFirePlasmaShot);
    }

    @SideOnly(Side.CLIENT)
    public void addShootDelay(ItemStack weaponStack) {
        ClientProxy.instance().getClientWeaponHandler().addShootDelay(this, weaponStack);
    }

    @SideOnly(Side.CLIENT)
    public boolean hasShootDelayPassed() {
        return ClientProxy.instance().getClientWeaponHandler().shootDelayPassed(this);
    }

    /**
     * This method is called each weapon update tick,
     * when the player is holding the weapon and is not in any GUI screen.
     * This is manly done for convenience.
     *
     * @param weapon         the weapon stack.
     * @param world          the shooter world.
     * @param entityPlayer   the player shooter.
     * @param sendServerTick should a server weapon tick be sent. This is tied up to {@link #manageClientServerTicks(World)}.
     */
    @SideOnly(Side.CLIENT)
    public void onShooterClientUpdate(ItemStack weapon, World world, EntityPlayer entityPlayer, boolean sendServerTick) {
        if (sendServerTick) {
            manageClientServerTicks(world);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean onLeftClick(ItemStack weapon, EntityPlayer entityPlayer) {
        return leftClickFire;
    }
    //endregion

    //region Reloading, Heating and Cooling managment
    public void chargeFromEnergyPack(ItemStack weapon, EntityPlayer player) {
        if (!player.worldObj.isRemote) {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof IEnergyPack && player.inventory.mainInventory[i].stackSize > 0) {
                    player.inventory.mainInventory[i].stackSize--;
                    setEnergyStored(weapon, Math.min(getEnergyStored(weapon) + ((IEnergyPack) player.inventory.mainInventory[i].getItem()).getEnergyAmount(player.inventory.mainInventory[i]), getMaxEnergyStored(weapon)));
                    player.inventory.inventoryChanged = true;
                    player.worldObj.playSoundAtEntity(player, Reference.MOD_ID + ":" + "reload", 0.7f + itemRand.nextFloat() * 0.2f, 0.9f + itemRand.nextFloat() * 0.2f);
                    if (player.inventory.mainInventory[i].stackSize <= 0) {
                        player.inventory.mainInventory[i] = null;
                    }
                    return;
                }
            }
        } else {
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack != null && stack.getItem() instanceof IEnergyPack && stack.stackSize > 0) {
                    ClientProxy.instance().getClientWeaponHandler().addReloadDelay(this, 40);
                    MatterOverdrive.packetPipeline.sendToServer(new PacketReloadEnergyWeapon());
                    return;
                }
            }
        }
    }

    protected void manageOverheat(ItemStack itemStack, World world, EntityLivingBase shooter) {
        if (getHeat(itemStack) >= getMaxHeat(itemStack)) {
            if (!MinecraftForge.EVENT_BUS.post(new MOEventEnergyWeapon.Overheat(itemStack, shooter))) {
                setOverheated(itemStack, true);
                world.playSoundAtEntity(shooter, Reference.MOD_ID + ":" + "overheat", 1F, 1f);
                world.playSoundAtEntity(shooter, Reference.MOD_ID + ":" + "overheat_alarm", 1, 1);
            }
        }
    }

    protected void manageCooling(ItemStack itemStack) {
        float heat = getHeat(itemStack);
        if (heat > 0) {
            float easing = MOEasing.Quart.easeOut(heat / getMaxHeat(itemStack), 0, 4, 1);
            float newHeat = heat - easing;
            if (newHeat < 0.001f) {
                newHeat = 0;
            }
            setHeat(itemStack, Math.max(0, newHeat));
        }

        if (isOverheated(itemStack)) {
            if (getHeat(itemStack) < 2) {
                setOverheated(itemStack, false);
            }
        }
    }
    //endregion

    //region Abstract Functions
    @SideOnly(Side.CLIENT)
    protected abstract void addCustomDetails(ItemStack weapon, EntityPlayer player, List infos);

    protected abstract int getBaseEnergyUse(ItemStack item);

    protected abstract int getBaseMaxHeat(ItemStack item);

    public abstract float getWeaponBaseDamage(ItemStack weapon);

    public abstract float getWeaponBaseAccuracy(ItemStack weapon, boolean zoomed);

    public abstract boolean canFire(ItemStack itemStack, World world, EntityLivingBase shooter);

    /**
     * Returns the speed on the Phaser Blot or any other projectile shot by the weapon.
     * This is different than the {@link #getBaseShootCooldown(ItemStack)}.
     *
     * @param weapon  the weapon.
     * @param shooter the shooter.
     * @return the speed of the "bullet".
     */
    public abstract float getShotSpeed(ItemStack weapon, EntityLivingBase shooter);

    public abstract int getBaseShootCooldown(ItemStack weapon);

    public abstract float getBaseZoom(ItemStack weapon, EntityLivingBase shooter);

    /**
     * Called when a weapon is fired with a single Weapon Shot.
     * This is different then the {@link #onServerFire(ItemStack, EntityLivingBase, WeaponShot, Vec3, Vec3, int)} in that it's called only on the client side.
     *
     * @param weapon   the weapon.
     * @param shooter  the shooter.
     * @param position the starting position of the shot.
     * @param dir      the direction of the shot.
     * @param shot     the Weapon shot info of the shot.
     */
    @SideOnly(Side.CLIENT)
    public abstract void onClientShot(ItemStack weapon, EntityLivingBase shooter, Vec3 position, Vec3 dir, WeaponShot shot);

    /**
     * Called when a Phaser Bolt or another projectile has hit something.
     * This method is only called on the client side.
     *
     * @param hit    the projective hit information.
     * @param weapon the weapon.
     * @param world  the world.
     * @param amount the amount of "hit" there was by the projectile. This is mainly used to control the amount of particles spawned on hit.
     */
    @SideOnly(Side.CLIENT)
    public abstract void onProjectileHit(MovingObjectPosition hit, ItemStack weapon, World world, float amount);
    //endregion

    //region Energy Functions
    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

        ItemStack energy_module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BATTERY, container);
        if (energy_module != null && MOEnergyHelper.isEnergyContainerItem(energy_module)) {
            IEnergyContainerItem e = ((IEnergyContainerItem) energy_module.getItem());
            int energy = e.receiveEnergy(energy_module, maxReceive, simulate);
            if (!simulate)
                WeaponHelper.setModuleAtSlot(Reference.MODULE_BATTERY, container, energy_module);
            return energy;
        } else {
            return super.receiveEnergy(container, maxReceive, simulate);
        }
    }

    //Returns 0 to disable extraction from outside sources
    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return 0;
    }

    public int extractEnergyCustom(ItemStack weapon, int maxExtract, boolean simulate) {
        ItemStack energy_module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BATTERY, weapon);
        if (energy_module != null && MOEnergyHelper.isEnergyContainerItem(energy_module)) {
            IEnergyContainerItem e = ((IEnergyContainerItem) energy_module.getItem());
            int energy = e.extractEnergy(energy_module, maxReceive, simulate);
            if (!simulate)
                WeaponHelper.setModuleAtSlot(Reference.MODULE_BATTERY, weapon, energy_module);
            return energy;
        } else {
            return super.extractEnergy(weapon, maxReceive, simulate);
        }
    }

    @Override
    protected void setEnergyStored(ItemStack container, int amount) {
        ItemStack energy_module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BATTERY, container);
        if (energy_module != null && MOEnergyHelper.isEnergyContainerItem(energy_module)) {
            MOEnergyHelper.setDefaultEnergyTag(energy_module, amount);
            WeaponHelper.setModuleAtSlot(Reference.MODULE_BATTERY, container, energy_module);
        } else {
            super.setEnergyStored(container, amount);
        }

    }

    public void rechargeFully(ItemStack container) {
        setEnergyStored(container, getMaxEnergyStored(container));
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        ItemStack energy_module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BATTERY, container);
        if (energy_module != null && MOEnergyHelper.isEnergyContainerItem(energy_module)) {
            IEnergyContainerItem e = ((IEnergyContainerItem) energy_module.getItem());
            return e.getEnergyStored(energy_module);
        } else {
            return super.getEnergyStored(container);
        }
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        ItemStack energy_module = WeaponHelper.getModuleAtSlot(Reference.MODULE_BATTERY, container);
        if (energy_module != null && MOEnergyHelper.isEnergyContainerItem(energy_module)) {
            IEnergyContainerItem e = ((IEnergyContainerItem) energy_module.getItem());
            return e.getMaxEnergyStored(energy_module);
        } else {
            return capacity;
        }
    }

    protected boolean DrainEnergy(ItemStack item, float ticks, boolean simulate) {
        int amount = (int) (getEnergyUse(item) * ticks);
        int hasEnergy = getEnergyStored(item);
        if (hasEnergy >= amount) {
            while (amount > 0) {
                if (extractEnergyCustom(item, amount, true) > 0) {
                    amount -= extractEnergyCustom(item, amount, simulate);
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    //endregion

    //region Getters and setters
    public int getRange(ItemStack weapon) {
        int range = defaultRange;
        range = Math.round(modifyStatFromModules(Reference.WS_RANGE, weapon, range));
        range *= getCustomIntStat(weapon, CUSTOM_RANGE_MULTIPLY_TAG, 1);
        return range;
    }

    public int getShootCooldown(ItemStack weapon) {
        int shootCooldown = getCustomIntStat(weapon, CUSTOM_SPEED_TAG, getBaseShootCooldown(weapon));
        shootCooldown = (int) modifyStatFromModules(Reference.WS_FIRE_RATE, weapon, shootCooldown);
        shootCooldown *= getCustomFloatStat(weapon, CUSTOM_SPEED_MULTIPLY_TAG, 1);
        return shootCooldown;
    }

    public WeaponShot createShot(ItemStack weapon, EntityLivingBase shooter, boolean zoomed) {
        return new WeaponShot(itemRand.nextInt(), getWeaponScaledDamage(weapon, shooter), getAccuracy(weapon, shooter, zoomed), WeaponHelper.getColor(weapon), getRange(weapon));
    }

    public float modifyStatFromModules(int statID, ItemStack weapon, float original) {
        return WeaponHelper.modifyStat(statID, weapon, original);
    }

    @Override
    public boolean hasDetails(ItemStack itemStack) {
        return true;
    }

    public float getWeaponScaledDamage(ItemStack weapon, EntityLivingBase shooter) {
        float damage = getCustomFloatStat(weapon, CUSTOM_DAMAGE_TAG, getWeaponBaseDamage(weapon));
        damage = modifyStatFromModules(Reference.WS_DAMAGE, weapon, damage);
        damage += damage * EnchantmentHelper.getEnchantmentLevel(MatterOverdriveEnchantments.overclock.effectId, weapon) * 0.04f;
        damage *= getCustomFloatStat(weapon, CUSTOM_DAMAGE_MULTIPLY_TAG, 1);
        damage += shooter.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).getAttributeValue();
        return damage;
    }

    public DamageSource getDamageSource(ItemStack weapon, EntityPlayer player) {
        DamageSource damageInfo = new EntityDamageSourcePhaser(player);
        if (WeaponHelper.hasStat(Reference.WS_FIRE_DAMAGE, weapon)) {
            damageInfo.setFireDamage();
        } else if (WeaponHelper.hasStat(Reference.WS_HEAL, weapon)) {
            damageInfo.setMagicDamage();
        }

        if (WeaponHelper.hasStat(Reference.WS_EXPLOSION_DAMAGE, weapon)) {
            damageInfo.setExplosion();
        }
        return damageInfo;
    }

    public int getEnergyUse(ItemStack weapon) {
        float energyUse = modifyStatFromModules(Reference.WS_AMMO, weapon, getBaseEnergyUse(weapon));
        energyUse -= energyUse * EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, weapon) * 0.04f;
        return Math.max((int) energyUse, 0);
    }

    public void addHeat(ItemStack itemStack, int amount) {
        if (itemStack.hasTagCompound()) {
            setHeat(itemStack, getHeat(itemStack) + amount);
        }
    }

    public void setHeat(ItemStack item, float amount) {
        if (item.hasTagCompound()) {
            item.getTagCompound().setFloat("heat", Math.min(amount, getMaxHeat(item) + 1));
        }
    }

    @Override
    public float getHeat(ItemStack item) {
        if (item.hasTagCompound()) {
            return item.getTagCompound().getFloat("heat");
        }
        return 0;
    }

    @Override
    public float getMaxHeat(ItemStack weapon) {
        return modifyStatFromModules(Reference.WS_MAX_HEAT, weapon, getBaseMaxHeat(weapon));
    }

    public boolean isOverheated(ItemStack weapon) {
        if (weapon.hasTagCompound()) {
            return weapon.getTagCompound().getBoolean("Overheated");
        }
        return false;
    }

    protected void setOverheated(ItemStack weapon, boolean overheated) {
        if (weapon.hasTagCompound()) {
            weapon.getTagCompound().setBoolean("Overheated", overheated);
        }
    }

    @Override
    public int getAmmo(ItemStack weapon) {
        return getEnergyStored(weapon);
    }

    public float getAccuracy(ItemStack weapon, EntityLivingBase shooter, boolean zoomed) {
        float accuracy = getWeaponBaseAccuracy(weapon, zoomed);
        accuracy = getCustomFloatStat(weapon, CUSTOM_ACCURACY_TAG, accuracy);
        accuracy += (float) Vec3.createVectorHelper(shooter.motionX, shooter.motionY * 0.1, shooter.motionZ).lengthVector() * 10;
        accuracy *= shooter.isSneaking() ? 0.6f : 1;
        accuracy = modifyStatFromModules(Reference.WS_ACCURACY, weapon, accuracy);
        if (WeaponHelper.hasModule(Reference.MODULE_SIGHTS, weapon)) {
            ItemStack sights = WeaponHelper.getModuleAtSlot(Reference.MODULE_SIGHTS, weapon);
            if (sights.getItem() instanceof IWeaponScope) {
                accuracy = ((IWeaponScope) sights.getItem()).getAccuracyModify(sights, weapon, zoomed, accuracy);
            }
        }
        accuracy *= getCustomFloatStat(weapon, CUSTOM_ACCURACY_MULTIPLY_TAG, 1);
        return accuracy;
    }

    public void removeAllCustomStats(ItemStack weapon) {
        if (weapon.hasTagCompound()) {
            weapon.getTagCompound().removeTag(CUSTOM_DAMAGE_TAG);
            weapon.getTagCompound().removeTag(CUSTOM_ACCURACY_TAG);
        }
    }

    public float getCustomFloatStat(ItemStack weapon, String name, float def) {
        if (weapon.hasTagCompound() && weapon.getTagCompound().hasKey(name, Constants.NBT.TAG_FLOAT)) {
            return weapon.getTagCompound().getFloat(name);
        }
        return def;
    }

    public int getCustomIntStat(ItemStack weapon, String name, int def) {
        if (weapon.hasTagCompound() && weapon.getTagCompound().hasKey(name, Constants.NBT.TAG_INT)) {
            return weapon.getTagCompound().getInteger(name);
        }
        return def;
    }

    @Override
    public int getMaxAmmo(ItemStack weapon) {
        return getMaxEnergyStored(weapon);
    }

    public boolean needsRecharge(ItemStack weapon) {
        return !DrainEnergy(weapon, getShootCooldown(weapon), true);
    }

    @Override
    public int getItemEnchantability() {
        return ToolMaterial.IRON.getEnchantability();
    }

    @Override
    public boolean isItemTool(ItemStack p_77616_1_) {
        return true;
    }

    @Override
    public float getZoomMultiply(EntityPlayer entityPlayer, ItemStack weapon) {
        if (WeaponHelper.hasModule(Reference.MODULE_SIGHTS, weapon)) {
            ItemStack sights = WeaponHelper.getModuleAtSlot(Reference.MODULE_SIGHTS, weapon);
            if (sights.getItem() instanceof IWeaponScope) {
                return ((IWeaponScope) sights.getItem()).getZoomAmount(sights, weapon);
            }
        }
        return getBaseZoom(weapon, entityPlayer);
    }
    //endregion
}
