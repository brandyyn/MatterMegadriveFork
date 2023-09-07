package matteroverdrive.blocks.includes;

import matteroverdrive.api.matter.IMatterHandler;
import matteroverdrive.init.MatterOverdriveIcons;
import matteroverdrive.tile.MOTileEntityMachineEnergy;
import matteroverdrive.tile.MOTileEntityMachineMatter;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class MOMatterEnergyStorageBlock extends MOBlockMachine {
    private boolean keepsMatter;
    private boolean keepsEnergy;
    protected boolean dropsItself;

    public MOMatterEnergyStorageBlock(Material material, String name, boolean keepsEnergy, boolean keepsMatter) {
        super(material, name);
        this.keepsEnergy = keepsEnergy;
        this.keepsMatter = keepsMatter;
    }

    protected IIcon GetIconBasedOnMatter(IBlockAccess world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(x, y, z);

        if (entity != null && entity instanceof IMatterHandler) {
            if (((IMatterHandler) entity).getMatterStored() > 0) {
                return MatterOverdriveIcons.matter_tank_full;
            }
        }
        return MatterOverdriveIcons.matter_tank_empty;
    }

    @Override
    public void onBlockPlacedBy(World World, int x, int y, int z, EntityLivingBase player, ItemStack item) {
        super.onBlockPlacedBy(World, x, y, z, player, item);
        if (item.hasTagCompound()) {
            TileEntity entity = World.getTileEntity(x, y, z);

            if (entity instanceof MOTileEntityMachineEnergy) {
                if (this.keepsEnergy)
                    ((MOTileEntityMachineEnergy) entity).setEnergyStored(item.getTagCompound().getInteger("Energy"));
            }
            if (entity instanceof MOTileEntityMachineMatter) {
                if (this.keepsMatter)
                    ((MOTileEntityMachineMatter) entity).setMatterStored(item.getTagCompound().getInteger("Matter"));
            }
        }
    }

    /*@Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
        if(dropsItself)
        {
            MOTileEntityMachineMatter tile = (MOTileEntityMachineMatter)world.getTileEntity(x,y,z);

            if (tile != null && !world.isRemote && !world.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
            {
                ItemStack item = new ItemStack(this);

                if(tile.getMatterStored() > 0 && this.keepsMatter)
                {
                    if(!item.hasTagCompound())
                        item.setTagCompound(new NBTTagCompound());

                    item.getTagCompound().setInteger("Matter", tile.getMatterStored());
                }
                if(tile.getEnergyStored(ForgeDirection.DOWN) > 0 && this.keepsEnergy)
                {
                    if(!item.hasTagCompound())
                        item.setTagCompound(new NBTTagCompound());

                    item.getTagCompound().setInteger("Energy", tile.getEnergyStored(ForgeDirection.DOWN));
                    item.getTagCompound().setInteger("MaxEnergy", tile.getMaxEnergyStored(ForgeDirection.DOWN));
                }

                this.dropBlockAsItem(world, x, y, z, item);
            }
        }
        return super.removedByPlayer(world,player,x,y,z);
    }*/

}
