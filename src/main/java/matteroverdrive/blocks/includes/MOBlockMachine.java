package matteroverdrive.blocks.includes;

import cofh.api.block.IDismantleable;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import matteroverdrive.api.IMOTileEntity;
import matteroverdrive.data.Inventory;
import matteroverdrive.data.inventory.Slot;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.items.includes.MOMachineBlockItem;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;

public abstract class MOBlockMachine extends MOBlockContainer implements IDismantleable, IConfigSubscriber {
    public float volume = 1;
    public boolean hasGui;

    public MOBlockMachine(Material material, String name) {
        super(material, name);
    }

    @Override
    protected void registerBlock() {
        GameRegistry.registerBlock(this, MOMachineBlockItem.class, this.getUnlocalizedName().substring(5));
    }

    public boolean doNormalDrops(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);

        IMOTileEntity entity = (IMOTileEntity) world.getTileEntity(x, y, z);
        if (entity != null) {
            try {
                entity.readFromPlaceItem(itemStack);
            } catch (Exception e) {
                e.printStackTrace();
                FMLLog.log(Level.ERROR, "Could not load settings from placing item", e);
            }

            entity.onPlaced(world, entityLiving);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        //drops inventory
        Inventory inventory = getInventory(world, x, y, z);
        if (inventory != null) {
            MatterHelper.DropInventory(world, inventory, x, y, z);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return MachineHelper.canOpenMachine(world, x, y, z, player, hasGui, getUnlocalizedMessage(0));
    }

    protected String getUnlocalizedMessage(int type) {
        switch (type) {
            case 0:
                return "alert.no_rights";
            default:
                return "alert.no_access_default";
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (MachineHelper.canRemoveMachine(world, player, x, y, z, willHarvest)) {
            return world.setBlockToAir(x, y, z);
        }
        return false;
    }

    public ItemStack getNBTDrop(World world, int x, int y, int z, IMOTileEntity te) {
        int meta = damageDropped(world.getBlockMetadata(x, y, z));
        ItemStack itemStack = new ItemStack(this, 1, meta);
        if (te != null)
            te.writeToDropItem(itemStack);
        return itemStack;
    }

    public boolean hasGui() {
        return hasGui;
    }

    public void setHasGui(boolean hasGui) {
        this.hasGui = hasGui;
    }

    @Override
    public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, World world, int x, int y, int z, boolean returnDrops) {
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack blockItem = getNBTDrop(world, x, y, z, (IMOTileEntity) world.getTileEntity(x, y, z));
        Inventory inventory = getInventory(world, x, y, z);
        items.add(blockItem);

        //remove any items from the machine inventory so that breakBlock doesn't duplicate the items
        if (inventory != null) {
            for (int i1 = 0; i1 < inventory.getSizeInventory(); ++i1) {
                Slot slot = inventory.getSlot(i1);
                ItemStack itemstack = slot.getItem();

                if (itemstack != null) {
                    if (slot.keepOnDismantle()) {
                        slot.setItem(null);
                    }
                }
            }
        }

        Block block = world.getBlock(x, y, z);
        int l = world.getBlockMetadata(x, y, z);
        boolean flag = block.removedByPlayer(world, player, x, y, z, true);
        super.breakBlock(world, x, y, z, block, l);

        if (flag) {
            block.onBlockDestroyedByPlayer(world, x, y, z, l);
        }

        if (!returnDrops) {
            dropBlockAsItem(world, x, y, z, blockItem);
        } else {
            MOInventoryHelper.insertItemStackIntoInventory(player.inventory, blockItem, 0);
        }

        return items;
    }

    protected Inventory getInventory(World world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof MOTileEntityMachine) {
            MOTileEntityMachine machine = (MOTileEntityMachine) world.getTileEntity(x, y, z);
            return machine.getInventoryContainer();
        }
        return null;
    }

    @Override
    public boolean canDismantle(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof MOTileEntityMachine) {
            if (player.capabilities.isCreativeMode || !((MOTileEntityMachine) tileEntity).hasOwner()) {
                return true;
            } else {
                if (((MOTileEntityMachine) tileEntity).getOwner().equals(player.getGameProfile().getId())) {
                    return true;
                } else {
                    if (world.isRemote) {
                        ChatComponentText message = new ChatComponentText(EnumChatFormatting.GOLD + "[Matter Megadrive] " + EnumChatFormatting.RED + MOStringHelper.translateToLocal("alert.no_rights.dismantle").replace("$0", getLocalizedName()));
                        message.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
                        player.addChatMessage(message);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onConfigChanged(ConfigurationHandler config) {
        config.initMachineCategory(getUnlocalizedName());
        volume = (float) config.getMachineDouble(getUnlocalizedName(), "volume", 1, "The volume of the Machine");
    }
}
