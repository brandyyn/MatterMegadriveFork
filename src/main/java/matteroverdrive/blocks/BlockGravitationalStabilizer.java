package matteroverdrive.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.blocks.includes.MOBlockMachine;
import matteroverdrive.client.render.block.RendererBlockGravitationalStabilizer;
import matteroverdrive.init.MatterOverdriveIcons;
import matteroverdrive.tile.TileEntityMachineGravitationalStabilizer;
import matteroverdrive.util.MOBlockHelper;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGravitationalStabilizer extends MOBlockMachine {
    public BlockGravitationalStabilizer(Material material, String name) {
        super(material, name);
        setHardness(20.0F);
        this.setResistance(10.0f);
        this.setHarvestLevel("pickaxe", 2);
        lightValue = 10;
        setRotationType(MOBlockHelper.RotationType.SIX_WAY);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == meta) {
            return MatterOverdriveIcons.Network_port_square;
        } else if (side == MOBlockHelper.getOppositeSide(meta)) {
            return MatterOverdriveIcons.Monitor_back;
        } else if (side == MOBlockHelper.getLeftSide(meta) || side == MOBlockHelper.getRightSide(meta)) {
            return MatterOverdriveIcons.Vent2;
        }

        return MatterOverdriveIcons.Coil;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMachineGravitationalStabilizer();

    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack item) {
        int l = BlockPistonBase.determineOrientation(world, x, y, z, player);

        if (player.isSneaking()) {
            world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.OPPOSITES[l], 2);
        } else {
            world.setBlockMetadataWithNotify(x, y, z, l, 2);
        }

    }

    @Override
    public int getRenderType() {
        return RendererBlockGravitationalStabilizer.renderID;
    }
}
