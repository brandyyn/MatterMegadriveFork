package matteroverdrive.data;

import matteroverdrive.data.inventory.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Collection;

public class TileEntityInventory extends Inventory {
    TileEntity entity;

    public TileEntityInventory(TileEntity entity, String name) {
        this(entity, name, new ArrayList<>());
    }

    public TileEntityInventory(TileEntity entity, String name, Collection<Slot> slots) {
        this(entity, name, slots, null);
    }

    public TileEntityInventory(TileEntity entity, String name, Collection<Slot> slots, IUsableCondition usableCondition) {
        super(name, slots, usableCondition);
        this.entity = entity;
    }


    @Override
    public void markDirty() {
        if (this.entity != null) {
            this.entity.markDirty();
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (usableCondition != null) {
            return usableCondition.usableByPlayer(player);
        }
        return entity.getWorldObj().getTileEntity(entity.xCoord, entity.yCoord, entity.zCoord) == entity && player.getDistanceSq((double) entity.xCoord + 0.5D, (double) entity.yCoord + 0.5D, (double) entity.zCoord + 0.5D) <= 64.0D;
    }
}
