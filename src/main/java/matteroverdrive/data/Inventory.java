package matteroverdrive.data;

import matteroverdrive.data.inventory.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Inventory implements IInventory {
    List<Slot> slots;
    String name;
    IUsableCondition usableCondition;

    //region Constructors
    public Inventory(String name) {
        this(name, new ArrayList<>());
    }

    public Inventory(String name, Collection<Slot> slots) {
        this(name, slots, null);
    }

    public Inventory(String name, Collection<Slot> slots, IUsableCondition usableCondition) {
        this.slots = new ArrayList<>(slots);
        this.name = name;
        this.usableCondition = usableCondition;
    }


    //endregion

    public int AddSlot(Slot slot) {
        if (slots.add(slot)) {
            slot.setId(slots.size() - 1);
            return slots.size() - 1;
        }
        return 0;
    }

    public void setUsableCondition(IUsableCondition condition) {
        this.usableCondition = condition;
    }

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");
            if (nbttagcompound1.hasKey("id", Constants.NBT.TAG_SHORT)) {
                setInventorySlotContents(b0, ItemStack.loadItemStackFromNBT(nbttagcompound1));
            } else {
                setInventorySlotContents(b0, null);
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound, boolean toDisk) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i) {
            writeSlotToNBT(nbttaglist, i, toDisk);
        }

        if (nbttaglist.tagCount() > 0) {
            compound.setTag("Items", nbttaglist);
        }
    }

    protected void writeSlotToNBT(NBTTagList nbttaglist, int slotId, boolean toDisk) {
        Slot slot = getSlot(slotId);
        if (slot != null) {
            if (toDisk && slot.getItem() != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) slotId);
                if (slot.getItem() != null) {
                    slot.getItem().writeToNBT(nbttagcompound1);
                }
                nbttaglist.appendTag(nbttagcompound1);
            } else if (!toDisk && slot.sendsToClient()) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) slotId);
                if (slot.getItem() != null)
                    slot.getItem().writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return slots.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots.get(slot).getItem();
    }

    @Override
    public ItemStack decrStackSize(int slotId, int size) {
        Slot slot = getSlot(slotId);
        if (slot != null && slot.getItem() != null) {
            ItemStack itemstack;

            if (slot.getItem().stackSize <= size) {
                itemstack = slot.getItem();
                slot.setItem(null);

                return itemstack;
            } else {
                itemstack = slot.getItem().splitStack(size);

                if (slot.getItem().stackSize == 0) {
                    slot.setItem(null);
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (this.slots.get(slot) != null) {
            ItemStack itemstack = getSlot(slot).getItem();
            this.slots.set(slot, null);
            return itemstack;
        }

        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack item) {
        getSlot(slot).setItem(item);

        if (item != null && item.stackSize > this.getInventoryStackLimit()) {
            item.stackSize = this.getInventoryStackLimit();
        }
    }

    public void addItem(ItemStack itemStack) {
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = getSlot(i);
            if (slot.isValidForSlot(itemStack)) {
                if (slot.getItem() == null) {
                    slot.setItem(itemStack);
                    return;
                } else if (ItemStack.areItemStacksEqual(slot.getItem(), itemStack) && slot.getItem().stackSize < slot.getItem().getMaxStackSize()) {
                    int newStackSize = Math.min(slot.getItem().stackSize + itemStack.stackSize, slot.getItem().getMaxStackSize());
                    int leftStackSize = slot.getItem().stackSize + itemStack.stackSize - newStackSize;
                    slot.getItem().stackSize = newStackSize;
                    if (leftStackSize <= 0)
                        return;

                    itemStack.stackSize = newStackSize;
                }
            }
        }
    }

    public void clearItems() {
        for (Slot slot : slots) {
            slot.setItem(null);
        }
    }

    @Override
    public String getInventoryName() {
        return this.name;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return name != null && !name.isEmpty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;

    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack item) {
        if (slotID >= 0 && slotID < getSizeInventory() && getSlot(slotID) != null) {
            Slot slot = getSlot(slotID);
            if (slot.getItem() != null) {
                if (slot.getItem().stackSize <= slot.getMaxStackSize()) {
                    return slot.isValidForSlot(item);
                } else {
                    return false;
                }
            }
            return slot.isValidForSlot(item);
        }
        return true;
    }

    public Slot getSlot(int slotID) {
        return slots.get(slotID);
    }

    public int getLastSlotId() {
        return slots.size() - 1;
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
