package matteroverdrive.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.data.Inventory;
import matteroverdrive.data.inventory.InscriberSlot;
import matteroverdrive.data.inventory.RemoveOnlySlot;
import matteroverdrive.data.recipes.InscriberRecipe;
import matteroverdrive.handler.recipes.InscriberRecipes;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.util.math.MOMathHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.EnumSet;

public class TileEntityInscriber extends MOTileEntityMachineEnergy {
    @SideOnly(Side.CLIENT)
    private float nextHeadX, nextHeadY;
    @SideOnly(Side.CLIENT)
    private float lastHeadX, lastHeadY;
    @SideOnly(Side.CLIENT)
    private float headAnimationTime;
    private int inscribeTime;

    public static int MAIN_INPUT_SLOT_ID, SEC_INPUT_SLOT_ID, OUTPUT_SLOT_ID;
    private InscriberRecipe cachedRecipe;

    public TileEntityInscriber() {
        super(4);
        energyStorage.setCapacity(512000);
        energyStorage.setMaxTransfer(256);
        playerSlotsHotbar = true;
        playerSlotsMain = true;
    }

    @Override
    protected void RegisterSlots(Inventory inventory) {
        MAIN_INPUT_SLOT_ID = inventory.AddSlot(new InscriberSlot(true, false).setSendToClient(true));
        SEC_INPUT_SLOT_ID = inventory.AddSlot(new InscriberSlot(true, true));
        OUTPUT_SLOT_ID = inventory.AddSlot(new RemoveOnlySlot(false).setSendToClient(true));
        super.RegisterSlots(inventory);
    }

    protected void manageInscription() {
        if (!worldObj.isRemote) {
            if (this.isInscribing()) {
                if (this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick()) {
                    this.inscribeTime++;
                    energyStorage.modifyEnergyStored(-getEnergyDrainPerTick());
                    UpdateClientPower();

                    if (this.inscribeTime >= getSpeed()) {
                        this.inscribeTime = 0;
                        this.inscribeItem();
                    }
                }
            }
        }

        if (!this.isInscribing()) {
            this.inscribeTime = 0;
        }
    }

    public boolean canPutInOutput() {
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT_ID);
        return outputStack == null;
    }

    public void inscribeItem() {
        if (cachedRecipe != null && canPutInOutput()) {
            ItemStack outputSlot = inventory.getStackInSlot(OUTPUT_SLOT_ID);
            if (outputSlot != null) {
                outputSlot.stackSize++;
            } else {
                inventory.setInventorySlotContents(OUTPUT_SLOT_ID, cachedRecipe.getCraftingResult(inventory.getStackInSlot(MAIN_INPUT_SLOT_ID), inventory.getStackInSlot(SEC_INPUT_SLOT_ID)));
            }

            inventory.decrStackSize(MAIN_INPUT_SLOT_ID, 1);
            inventory.decrStackSize(SEC_INPUT_SLOT_ID, 1);

            calculateRecipe();
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        super.writeCustomNBT(nbt, categories, toDisk);
        if (categories.contains(MachineNBTCategory.DATA)) {
            nbt.setInteger("inscribeTime", inscribeTime);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        super.readCustomNBT(nbt, categories);
        if (categories.contains(MachineNBTCategory.DATA)) {
            inscribeTime = nbt.getInteger("inscribeTime");
        }
    }

    @Override
    public boolean getServerActive() {
        return isInscribing() && this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick();
    }

    public int getEnergyDrainPerTick() {
        int maxEnergy = getEnergyDrainMax();
        int speed = getSpeed();
        if (speed > 0) {
            return maxEnergy / speed;
        }
        return 0;
    }

    public int getEnergyDrainMax() {
        if (cachedRecipe != null) {
            return (int) (cachedRecipe.getEnergy() * getUpgradeMultiply(UpgradeTypes.PowerUsage));
        }
        return 0;
    }

    public int getSpeed() {
        if (cachedRecipe != null) {
            return (int) (cachedRecipe.getTime() * getUpgradeMultiply(UpgradeTypes.Speed));
        }
        return 0;
    }

    public boolean isInscribing() {
        return cachedRecipe != null && canPutInOutput();
    }

    @Override
    public String getSound() {
        return "machine";
    }

    @Override
    public boolean hasSound() {
        return true;
    }

    @Override
    public float soundVolume() {
        return 1;
    }

    @Override
    protected void onActiveChange() {

    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote && isActive()) {
            handleHeadAnimation();
        }
        manageInscription();
    }

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
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{MAIN_INPUT_SLOT_ID, SEC_INPUT_SLOT_ID, OUTPUT_SLOT_ID};
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack item, int side) {
        return slot == OUTPUT_SLOT_ID;
    }

    @Override
    public boolean isAffectedByUpgrade(UpgradeTypes type) {
        return type.equals(UpgradeTypes.PowerUsage) || type.equals(UpgradeTypes.Speed) || type.equals(UpgradeTypes.PowerStorage) || type.equals(UpgradeTypes.PowerTransfer);
    }

    @Override
    protected void onAwake(Side side) {
        calculateRecipe();
    }

    @Override
    public float getProgress() {
        float speed = (float) getSpeed();
        if (speed > 0) {
            return (float) (inscribeTime) / speed;
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    protected void handleHeadAnimation() {
        if (headAnimationTime >= 1) {
            lastHeadX = nextHeadX;
            lastHeadY = nextHeadY;
            nextHeadX = MathHelper.clamp_float((float) random.nextGaussian(), -1, 1);
            nextHeadY = MathHelper.clamp_float((float) random.nextGaussian(), -1, 1);
            headAnimationTime = 0;
        }

        headAnimationTime += 0.05f;
        //MatterOverdrive.log.info("Time: " + headAnimationTime);
    }

    @SideOnly(Side.CLIENT)
    public float geatHeadX() {
        return MOMathHelper.Lerp(lastHeadX, nextHeadX, headAnimationTime);
    }

    @SideOnly(Side.CLIENT)
    public float geatHeadY() {
        return MOMathHelper.Lerp(lastHeadY, nextHeadY, headAnimationTime);
    }

    public void calculateRecipe() {
        ItemStack mainStack = inventory.getStackInSlot(MAIN_INPUT_SLOT_ID);
        ItemStack secStack = inventory.getStackInSlot(SEC_INPUT_SLOT_ID);
        if (mainStack != null && secStack != null) {
            cachedRecipe = InscriberRecipes.getRecipe(mainStack, secStack);
            return;
        }
        cachedRecipe = null;
    }

    //region Inventory
    @Override
    public ItemStack decrStackSize(int slot, int size) {
        ItemStack stack = super.decrStackSize(slot, size);
        calculateRecipe();
        return stack;
    }

    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        super.setInventorySlotContents(slot, itemStack);
        calculateRecipe();
    }
    //endregion
}
